package net.stlutz.ohm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class SemanticAction {
    final String name;
    final Method method;

    private SemanticAction(String name, Method method) {
        super();
        this.name = name;
        this.method = method;
    }

    static boolean isExactlyNode(Class<?> type) {
        return Node.class.isAssignableFrom(type) && type.isAssignableFrom(Node.class);
    }

    static SemanticAction fromMethod(String actionName, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.isVarArgs() && parameterTypes.length >= 2) {
            // foobar(Node a, Node... rest)
            throw new OhmException("Action method '%s' is varargs but has more than one parameter"
                    .formatted(method.getName()));
        } else if (parameterTypes.length == 1 && parameterTypes[0].isArray()) {
            // foobar(Node[] children)
            // foobar(Node... children)
            if (!isExactlyNode(parameterTypes[0].getComponentType())) {
                throw new OhmException("Action method '%s' has vararg parameter that is not '%s'"
                        .formatted(method.getName(), Node.class.arrayType().getCanonicalName()));
            }
            return new VarArgsSemanticAction(actionName, method);
        } else {
            // foobar(Node a, Node b)
            // foobar()
            for (Class<?> parameterType : parameterTypes) {
                if (!isExactlyNode(parameterType)) {
                    throw new OhmException("Action method '%s' expects parameters that are not '%s'"
                            .formatted(method.getName(), Node.class.getCanonicalName()));
                }
            }
            return new SemanticAction(actionName, method);
        }
    }

    public Object invoke(Semantics semantics, Node self)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(semantics, (Object[]) self.getChildren());
    }

    int compareTo(SemanticAction otherAction) {
        return compare(this, otherAction);
    }

    void validateAgainstGrammar(Grammar grammar) {
        Rule rule = grammar.getRule(name);
        if (rule == null) {
            return;
        }

        int expectedArity = name.equals(Semantics.SpecialActionNames.terminal) ? 0 : rule.getArity();
        int actualArity = method.getParameterCount();
        if (expectedArity != actualArity) {
            throw new OhmException("Rule '%s' expects an action with '%d' arguments, but got '%d'"
                    .formatted(name, expectedArity, actualArity));
        }
    }

    /**
     * Returns an integer value
     * <ul>
     * <li>< 0, if {@code actionA} is less specific than {@code actionB},</li>
     * <li>= 0, if {@code actionA} and {@code actionB} are declared in the same class,</li>
     * <li>> 0, if {@code actionA} is more specific than {@code actionB}</li>
     * </ul>
     */
    static int compare(SemanticAction actionA, SemanticAction actionB) {
        Class<?> typeA = actionA.method.getDeclaringClass();
        Class<?> typeB = actionB.method.getDeclaringClass();
        // which action is the more specific one?
        if (typeB == typeA) {
            return 0;
        } else if (typeA.isAssignableFrom(typeB)) {
            // actionB is more specific than actionA
            return -1;
        } else {
            // should imply: typeB.isAssignableFrom(typeA)
            // actionA is more specific than actionB
            // TODO: this doesn't take interfaces into account
            return 1;
        }
    }

    private static class VarArgsSemanticAction extends SemanticAction {
        VarArgsSemanticAction(String name, Method method) {
            super(name, method);
        }

        @Override
        public Object invoke(Semantics semantics, Node self)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return method.invoke(semantics, (Object) self.getChildren());
        }

        @Override
        void validateAgainstGrammar(Grammar grammar) {
            // do nothing
        }
    }
}
