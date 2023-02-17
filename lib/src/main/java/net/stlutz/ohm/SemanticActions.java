package net.stlutz.ohm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A SemanticActions represents a function to be applied to a concrete syntax
 * tree (CST) -- it's very similar to a Visitor
 * (http://en.wikipedia.org/wiki/Visitor_pattern). An operation is executed by
 * recursively walking the CST, and at each node, invoking the matching semantic
 * action from `actionMap`.
 *
 */
public class SemanticActions {
	protected Node self = null;
	Map<String, Method> actionMap;

	public static class SpecialActionNames {
		public static final String nonterminal = "_nonterminal";
		public static final String terminal = "_terminal";
		public static final String iteration = "_iter";
	}

	public static final Class<? extends Node> NodeClass = Node.class;
	private static final Map<Class<? extends SemanticActions>, Map<String, Method>> knownActionMaps = new HashMap<>();

	public SemanticActions() {
		super();
		this.actionMap = getActionMap(getClass());
	}

	private static Map<String, Method> getActionMap(Class<? extends SemanticActions> myClass) {
		Map<String, Method> actionMap = knownActionMaps.get(myClass);
		if (actionMap == null) {
			actionMap = gatherActionMap(myClass);
			knownActionMaps.put(myClass, actionMap);
		}
		return actionMap;
	}

	private static Map<String, Method> gatherActionMap(Class<? extends SemanticActions> myClass) {
		Map<String, Method> actionMap = gatherLocalActionMap(myClass);
		if (myClass.equals(SemanticActions.class)) {
			return actionMap;
		}

		// TODO: I'm pretty sure this should be completely fine due to the abort
		// condition above, but maybe think about it again later. Also, what's the worst
		// that could happen here?
		@SuppressWarnings("unchecked")
		Class<? extends SemanticActions> superClass = (Class<? extends SemanticActions>) myClass.getSuperclass();

		Map<String, Method> superActionMap = getActionMap(superClass);
		superActionMap.forEach((name, action) -> {
			actionMap.putIfAbsent(name, action);
		});
		return actionMap;
	}

	private static Map<String, Method> gatherLocalActionMap(Class<? extends SemanticActions> myClass) {
		Map<String, Method> actionMap = new HashMap<>();

		// TODO: could throw SecurityException -> what then?
		for (Method method : myClass.getDeclaredMethods()) {
			for (Action annotation : method.getDeclaredAnnotationsByType(Action.class)) {
				String name = annotation.value();
				if (name.isEmpty()) {
					name = method.getName();
				}

				if (actionMap.containsKey(name)) {
					throw new OhmException("Rule '%s' has multiple actions defined".formatted(name));
				}

				// verify parameters
				Class<?>[] parameterTypes = method.getParameterTypes();
				int numNonVarParams = method.getParameterCount();
				if (method.isVarArgs()) {
					numNonVarParams--;
				}
				for (int i = 0; i < numNonVarParams; i++) {
					Class<?> pType = parameterTypes[i];
					if (!NodeClass.isAssignableFrom(pType)) {
						throw new OhmException(
								"Action '%s' expects parameters that are not '%s'".formatted(name, NodeClass));
					}
				}
				if (method.isVarArgs()) {
					if (!NodeClass.isAssignableFrom(parameterTypes[numNonVarParams].getComponentType())) {
						throw new OhmException("Action '%s' expects vararg parameter that is not '%s'".formatted(name,
								NodeClass.arrayType()));
					}
				}
				// TODO: verify against grammar's rule definitions, probably not here though

				actionMap.put(name, method);
			}
		}

		return actionMap;
	}

	Method getActionMethod(Node node) {
		String actionName = node.ctorName();
		Method method = actionMap.get(actionName);
		if (method != null) {
			return method;
		}

		if (node.isNonterminal()) {
			method = actionMap.get(SpecialActionNames.nonterminal);
		}

		return method;
	}

	Object executeActionMethod(Method method, Node node) {
		Node previousSelf = self;

		Object result = null;
		try {
			self = node;
			if (method.isVarArgs()) {
				result = method.invoke(this, (Object) node.getChildren());
			} else {
				result = method.invoke(this, (Object[]) node.getChildren());
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO: actual error message
			// case: numChildren != num parameters
			throw new OhmException("Failed to execute action");
		} finally {
			self = previousSelf;
		}

		return result;
	}

	public Object apply(Node node) {
		Method method = getActionMethod(node);
		if (method == null) {
			throw new OhmException("Missing semantic action for '%s'".formatted(node.ctorName()));
		}

		return executeActionMethod(method, node);
	}

	// TODO: Ensure this does not override default actions defined in super semantic
	@Action(SpecialActionNames.nonterminal)
	public Object defaultNonterminalAction(Node... children) {
		// This CST node corresponds to a non-terminal in the grammar. The fact that we
		// got here means that the action map doesn't have an action for this particular
		// non-terminal. As a convenience, if this node only has one child, we just
		// return the result of applying this operation to the child node.
		if (children.length != 1) {
			// TODO: This should probably be a different exception type
			// TODO: Better error message
			throw new OhmException("Missing semantic action");
		}
		return apply(children[0]);
	}
}
