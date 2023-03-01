package net.stlutz.ohm;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class SemanticsBlueprint {
	private final Class<? extends Semantics> semanticsClass;
	private final Grammar grammar;
	private final Constructor<? extends Semantics> constructor;
	private final OperationBlueprint[] operationBlueprints;

	private SemanticsBlueprint(Class<? extends Semantics> semanticsClass, Grammar grammar,
			Constructor<? extends Semantics> constructor, OperationBlueprint[] operationBlueprints) {
		super();
		this.semanticsClass = semanticsClass;
		this.grammar = grammar;
		this.constructor = constructor;
		this.operationBlueprints = operationBlueprints;
	}

	static SemanticsBlueprint create(Class<? extends Semantics> semanticsClass) {
		return create(semanticsClass, null);
	}

	static SemanticsBlueprint create(Class<? extends Semantics> semanticsClass, Grammar grammar) {
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

		Constructor<? extends Semantics> constructor;
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
			throw new OhmException("Failed to create semantics blueprint of '%s'. No operations could be found"
					.formatted(semanticsClass.getCanonicalName()));
		}

		return new SemanticsBlueprint(semanticsClass, grammar, constructor, operationBlueprints);
	}

	public Semantics on(MatchResult matchResult) {
		if (matchResult.failed()) {
			throw new OhmException("Cannot instantiate semantics '%s' for a match result that failed."
					.formatted(semanticsClass.getCanonicalName()));
		}
		return on(matchResult.getRootNode());
	}

	Semantics on(Node rootNode) {

		Semantics semantics;
		try {
			semantics = constructor.newInstance();
		} catch (InvocationTargetException e) {
			throw new OhmException("Failed to instantiate semantics '%s'. Exception thrown during constructor."
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
				: semantics.getOperation(SemanticActions.defaultName);
		semantics.rootNode = rootNode;
		semantics.grammar = grammar;
		semantics.initialize();
		semantics.initializeOperations();
		return semantics;
	}

	private SemanticActions[] createOperations(Semantics semantics) {
		SemanticActions[] operations = new SemanticActions[operationBlueprints.length];

		for (int i = 0; i < operations.length; i++) {
			operations[i] = operationBlueprints[i].make(semantics);
		}

		return operations;
	}

	private static OperationBlueprint[] gatherOperationBlueprints(Class<? extends Semantics> semanticsClass) {
		Map<String, Class<? extends SemanticActions>> operationClasses = gatherOperationClasses(semanticsClass);
		OperationBlueprint[] operationBlueprints = new OperationBlueprint[operationClasses.size()];

		int i = 0;
		for (Class<? extends SemanticActions> opClass : operationClasses.values()) {
			operationBlueprints[i++] = OperationBlueprint.create(opClass);
		}

		return operationBlueprints;
	}

	private static Map<String, Class<? extends SemanticActions>> gatherOperationClasses(
			Class<? extends Semantics> semanticsClass) {
		Map<String, Class<? extends SemanticActions>> nameToOpClass = new HashMap<>();

		// TODO: could throw SecurityException
		for (Class<?> cls : semanticsClass.getClasses()) {
			// only include subclasses of SemanticActions
			if (!SemanticActions.class.isAssignableFrom(cls)) {
				continue;
			}

			@SuppressWarnings("unchecked") // safe due to the check above
			Class<? extends SemanticActions> opClass = (Class<? extends SemanticActions>) cls;
			String opName = SemanticActions.getName(opClass);

			// always get the most specialized operation class of a given name
			nameToOpClass.merge(opName, opClass, (classA, classB) -> {
				if (classA.isAssignableFrom(classB)) {
					return classB;
				} else if (classB.isAssignableFrom(classA)) {
					return classA;
				} else {
					if (opName.equals(SemanticActions.defaultName)) {
						throw new OhmException("Semantics '%s' contains more than one unnamed operation: '%s' and '%s'."
								.formatted(semanticsClass.getCanonicalName(), classA.getCanonicalName(),
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
