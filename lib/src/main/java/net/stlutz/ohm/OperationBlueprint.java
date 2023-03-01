package net.stlutz.ohm;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

class OperationBlueprint {
	private final String name;
	private final Map<String, SemanticAction> actionMap;
	private final Class<? extends Operation> operationClass;
	private final Constructor<? extends Operation> operationConstructor;
	private final boolean isStandalone;

	public static final Class<? extends Node> NodeClass = Node.class;

	private OperationBlueprint(String name, Map<String, SemanticAction> actionMap,
			Class<? extends Operation> operationClass,
			Constructor<? extends Operation> operationConstructor, boolean isStandalone) {
		super();
		this.name = name;
		this.actionMap = actionMap;
		this.operationClass = operationClass;
		this.operationConstructor = operationConstructor;
		this.isStandalone = isStandalone;
	}

	static OperationBlueprint create(Class<? extends Operation> operationClass) {
		int modifiers = operationClass.getModifiers();
		if (Modifier.isAbstract(modifiers)) {
			throw new OhmException("Defining operations as abstract classes is not allowed");
		}

		boolean isStandalone = !operationClass.isMemberClass() || Modifier.isStatic(modifiers);
		Class<?> declaringClass = operationClass.getDeclaringClass();

		if (!isStandalone) {
			if (declaringClass == null) {
				throw new OhmException("Defining operations as local or anonymous classes is not allowed");
			} else if (!Semantics.class.isAssignableFrom(declaringClass)) {
				throw new OhmException(
						"Declaring class of an operation must be of type " + Semantics.class.getSimpleName());
			}
		}

		Constructor<? extends Operation> constructor;
		try {
			if (isStandalone) {
				constructor = operationClass.getConstructor();
			} else {
				constructor = operationClass.getConstructor(declaringClass);
			}
		} catch (SecurityException e) {
			throw new OhmException("Security exception during operation blueprint creation.");
		} catch (NoSuchMethodException e) {
			throw new OhmException(
					"Failed to create operation blueprint of '%s'. No public zero-arg constructor was found. Either declare it public explicitly or change the class's access modifier to public."
							.formatted(operationClass.getSimpleName()));
		}

		Map<String, SemanticAction> actionMap = gatherActionMap(operationClass);
		String name = Operation.getName(operationClass);

		return new OperationBlueprint(name, actionMap, operationClass, constructor, isStandalone);
	}

	Operation make() {
		if (!isStandalone) {
			throw new OhmException("Cannot create this operation blueprint standalone");
		}
		return make(null);
	}

	Operation make(Semantics semantics) {
		Operation result;

		try {
			if (isStandalone) {
				result = operationConstructor.newInstance();
			} else {
				result = operationConstructor.newInstance(semantics);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e1) {
			throw new OhmException("Failed to instantiate operation");
		}

		result.name = name;
		result.actionMap = actionMap;
		return result;
	}

	private static Map<String, SemanticAction> gatherActionMap(Class<? extends Operation> operationClass) {
		Map<String, SemanticAction> actionMap = new HashMap<>();

		// TODO: could throw SecurityException -> what then?
		for (Method method : operationClass.getMethods()) {
			Action[] annotations = method.getDeclaredAnnotationsByType(Action.class);
			for (Action annotation : annotations) {
				String annotatedName = annotation.value();
				String ruleName = annotatedName.isEmpty() ? method.getName() : annotatedName;
				SemanticAction action = SemanticAction.fromMethod(ruleName, method);

				actionMap.merge(ruleName, action, (actionA, actionB) -> {
					// which action is the more specialized one?
					int comp = actionA.compareTo(actionB);
					if (comp == 0) {
						throw new OhmException("Rule '%s' has multiple actions defined".formatted(ruleName));
					} else if (comp < 0) {
						return actionB;
					} else {
						return actionA;
					}
				});
			}
		}

		return actionMap;
	}

	private static void validateActionMap(Map<String, SemanticAction> actionMap, Grammar grammar) {
		actionMap.forEach((ruleName, action) -> {
			action.validateAgainstGrammar(grammar);
		});
	}
}
