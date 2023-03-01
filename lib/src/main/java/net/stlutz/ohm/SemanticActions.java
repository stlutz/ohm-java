package net.stlutz.ohm;

import java.lang.reflect.InvocationTargetException;
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
	String name;
	Map<String, SemanticAction> actionMap;

	public static final class SpecialActionNames {
		public static final String nonterminal = "_nonterminal";
		public static final String terminal = "_terminal";
		public static final String iteration = "_iter";

		public static boolean includes(String name) {
			return name.equals(nonterminal) || name.equals(terminal) || name.equals(iteration);
		}
	}

	public static final String defaultName = "__default__";

	/**
	 * Returns either
	 * <ul>
	 * <li>the name given to {@code myClass} via an {@code @Named} annotation
	 * (e.g. {@code "foobar"} for {@code @Named("foobar")},</li>
	 * <li>or a default name if no annotation is present</li>
	 * </ul>
	 */
	static String getName(Class<? extends SemanticActions> opClass) {
		Named annotation = opClass.getAnnotation(Named.class);
		if (annotation != null) {
			String annotatedName = annotation.value();
			if (annotatedName.equals(defaultName)) {
				throw new OhmException("Named '%s' was named '%s', which is a reserved name."
						.formatted(opClass.getCanonicalName(), defaultName));
			}
			return annotatedName;
		} else {
			return defaultName;
		}
	}

	public String getName() {
		return name;
	}

	boolean hasAction(String actionName) {
		return actionMap.containsKey(actionName);
	}

	SemanticAction getAction(Node node) {
		String actionName = node.ctorName();
		SemanticAction action = actionMap.get(actionName);
		if (action != null) {
			return action;
		}

		if (node.isNonterminal()) {
			action = actionMap.get(SpecialActionNames.nonterminal);
		}

		return action;
	}

	Object executeAction(SemanticAction action, Node node) {
		Node previousSelf = self;

		Object result = null;
		try {
			self = node;
			result = action.invoke(this, node);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new OhmException("Failed to execute action. This should never happen.");
		} catch (InvocationTargetException e) {
			// TODO: we should be throwing an unchecked exception wrapping the original
			throw new OhmException("There was an error executing the action '%s'.".formatted(action.name));
		} finally {
			self = previousSelf;
		}

		return result;
	}

	public Object apply(Node node) {
		SemanticAction action = getAction(node);
		if (action == null) {
			throw new OhmException("Missing semantic action for '%s'".formatted(node.ctorName()));
		}

		return executeAction(action, node);
	}

	protected void initialize() {
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
