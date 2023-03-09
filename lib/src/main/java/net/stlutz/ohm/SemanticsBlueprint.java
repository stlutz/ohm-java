package net.stlutz.ohm;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class SemanticsBlueprint<T extends Semantics> {
  private final Class<T> semanticsClass;
  private final Grammar grammar;
  private final Constructor<T> constructor;
  private final OperationBlueprint[] operationBlueprints;

  private SemanticsBlueprint(Class<T> semanticsClass, Grammar grammar, Constructor<T> constructor,
      OperationBlueprint[] operationBlueprints) {
    super();
    this.semanticsClass = semanticsClass;
    this.grammar = grammar;
    this.constructor = constructor;
    this.operationBlueprints = operationBlueprints;
  }

  static <T extends Semantics> SemanticsBlueprint<T> create(Class<T> semanticsClass) {
    return create(semanticsClass, null);
  }

  static <T extends Semantics> SemanticsBlueprint<T> create(Class<T> semanticsClass,
      Grammar grammar) {
    int modifiers = semanticsClass.getModifiers();
    if (Modifier.isAbstract(modifiers)) {
      throw new OhmException(
          "Failed to create semantics blueprint of '%s'. Semantics classes must not be abstract."
              .formatted(semanticsClass.getCanonicalName()));
    }
    if (semanticsClass.isMemberClass() && !Modifier.isStatic(modifiers)) {
      throw new OhmException(
          "Failed to create semantics blueprint of '%s'. Nested semantics classes must be static."
              .formatted(semanticsClass.getCanonicalName()));
    }

    Constructor<T> constructor;
    try {
      constructor = semanticsClass.getConstructor();
    } catch (SecurityException e) {
      throw new OhmException(
          "Failed to create semantics blueprint of '%s'. Security exception on constructor access."
              .formatted(semanticsClass.getCanonicalName()));
    } catch (NoSuchMethodException e) {
      throw new OhmException(
          "Failed to create semantics blueprint of '%s'. No public zero-arg constructor was found. Either declare it public explicitly or change the class's access modifier to public."
              .formatted(semanticsClass.getCanonicalName()));
    }

    OperationBlueprint[] operationBlueprints = gatherOperationBlueprints(semanticsClass);
    if (operationBlueprints.length == 0) {
      throw new OhmException(
          "Failed to create semantics blueprint of '%s'. No operations could be found"
              .formatted(semanticsClass.getCanonicalName()));
    }

    return new SemanticsBlueprint<T>(semanticsClass, grammar, constructor, operationBlueprints);
  }

  public T on(MatchResult matchResult) {
    if (matchResult.failed()) {
      throw new OhmException("Cannot instantiate semantics '%s' for a match result that failed."
          .formatted(semanticsClass.getCanonicalName()));
    }
    return on(matchResult.getRootNode());
  }

  T on(Node rootNode) {
    T result = instantiate();
    result.rootNode = rootNode;
    return result;
  }

  T instantiate() {

    T semantics;
    try {
      semantics = constructor.newInstance();
    } catch (InvocationTargetException e) {
      throw new OhmException(
          "Failed to instantiate semantics '%s'. Exception thrown during constructor."
              .formatted(semanticsClass.getCanonicalName()));
    } catch (InstantiationException e) {
      // should not be possible (class is checked to not be abstract during creation)
      throw new OhmException("Cannot instantiate semantics '%s'. The semantics class is abstract."
          .formatted(semanticsClass.getCanonicalName()));
    } catch (IllegalArgumentException e) {
      // should not be possible
      throw new OhmException("Cannot instantiate semantics '%s'. Illegal arguments."
          .formatted(semanticsClass.getCanonicalName()));
    } catch (IllegalAccessException e) {
      // should not be possible
      throw new OhmException("Cannot instantiate semantics '%s'. Constructor is inaccessible."
          .formatted(semanticsClass.getCanonicalName()));
    }

    semantics.operations = createOperations(semantics);
    semantics.defaultOperation = semantics.operations.length == 1 ? semantics.operations[0]
        : semantics.getOperation(Operation.defaultName);
    semantics.grammar = grammar;
    semantics.initialize();
    semantics.initializeOperations();
    return semantics;
  }

  private Operation[] createOperations(Semantics semantics) {
    Operation[] operations = new Operation[operationBlueprints.length];

    for (int i = 0; i < operations.length; i++) {
      operations[i] = operationBlueprints[i].make(semantics);
    }

    return operations;
  }

  private static OperationBlueprint[] gatherOperationBlueprints(
      Class<? extends Semantics> semanticsClass) {
    Map<String, Class<? extends Operation>> operationClasses =
        gatherOperationClasses(semanticsClass);
    OperationBlueprint[] operationBlueprints = new OperationBlueprint[operationClasses.size()];

    int i = 0;
    for (Class<? extends Operation> opClass : operationClasses.values()) {
      operationBlueprints[i++] = OperationBlueprint.create(opClass);
    }

    return operationBlueprints;
  }

  private static Map<String, Class<? extends Operation>> gatherOperationClasses(
      Class<? extends Semantics> semanticsClass) {
    Map<String, Class<? extends Operation>> nameToOpClass = new HashMap<>();

    // TODO: could throw SecurityException
    for (Class<?> cls : semanticsClass.getClasses()) {
      // only include subclasses of Operation
      if (!Operation.class.isAssignableFrom(cls)) {
        continue;
      }

      @SuppressWarnings("unchecked") // safe due to the check above
      Class<? extends Operation> opClass = (Class<? extends Operation>) cls;
      String opName = Operation.getName(opClass);

      // always get the most specialized operation class of a given name
      nameToOpClass.merge(opName, opClass, (classA, classB) -> {
        if (classA.isAssignableFrom(classB)) {
          return classB;
        } else if (classB.isAssignableFrom(classA)) {
          return classA;
        } else {
          if (opName.equals(Operation.defaultName)) {
            throw new OhmException(
                "Semantics '%s' contains more than one unnamed operation: '%s' and '%s'.".formatted(
                    semanticsClass.getCanonicalName(), classA.getCanonicalName(),
                    classB.getCanonicalName()));
          } else {
            throw new OhmException(
                "Semantics '%s' has two unrelated operations of the same name '%s': '%s' and '%s'."
                    .formatted(semanticsClass.getCanonicalName(), opName, classA.getCanonicalName(),
                        classB.getCanonicalName()));
          }
        }
      });
    }

    return nameToOpClass;
  }
}
