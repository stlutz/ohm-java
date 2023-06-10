package net.stlutz.ohm;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Semantics represent a function to be applied to a concrete syntax tree (CST) -- it's very
 * similar to a Visitor (http://en.wikipedia.org/wiki/Visitor_pattern). An operation is executed by
 * recursively walking the CST, and at each node, invoking the matching semantic action from
 * `actionMap`.
 */
public class Semantics {
    public static final class SpecialActionNames {
        public static final String nonterminal = "_nonterminal";
        public static final String terminal = "_terminal";
        public static final String iteration = "_iter";
        
        public static boolean includes(String name) {
            return name.equals(nonterminal) || name.equals(terminal) || name.equals(iteration);
        }
    }
    
    Grammar grammar;
    Map<String, SemanticAction> actionMap;
    Node rootNode;
    protected Node self = null;
    
    public Grammar getGrammar() {
        return grammar;
    }
    
    public Node getRootNode() {
        return rootNode;
    }
    
    protected void initialize() {
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
            Throwable cause = e;
            var wrapper = new OhmException(cause);
            wrapper.setStackTrace(cause.getStackTrace());
            throw wrapper;
            // throw new OhmException("Failed to execute action. This should never happen.", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            var wrapper = new OhmException(cause);
            wrapper.setStackTrace(cause.getStackTrace());
            throw wrapper;
        } finally {
            self = previousSelf;
        }
        
        return result;
    }
    
    public Object apply() {
        return apply(rootNode);
    }
    
    public Object apply(Node node) {
        SemanticAction action = getAction(node);
        if (action == null) {
            throw new OhmException("Missing semantic action for '%s'".formatted(node.ctorName()));
        }
        
        return executeAction(action, node);
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
