package net.stlutz.ohm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class SemanticsBlueprint<T extends Semantics> {
    // blueprint state
    private final Class<T> semanticsClass;
    private final Constructor<T> constructor;
    private final Class<?> enclosingClass;
    
    // instance state
    private final Map<String, SemanticAction> actionMap;
    private final Grammar grammar;
    
    
    private SemanticsBlueprint(Class<T> semanticsClass, Grammar grammar, Constructor<T> constructor, Map<String, SemanticAction> actionMap, Class<?> enclosingClass) {
        super();
        this.semanticsClass = semanticsClass;
        this.constructor = constructor;
        this.enclosingClass = enclosingClass;
        
        this.grammar = grammar;
        this.actionMap = actionMap;
    }
    
    // TODO: only for tests
    static <T extends Semantics> SemanticsBlueprint<T> create(Class<T> semanticsClass) {
        return create(semanticsClass, null);
    }
    
    static <T extends Semantics> SemanticsBlueprint<T> create(Class<T> semanticsClass,
                                                              Grammar grammar) {
        int modifiers = semanticsClass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            // TODO: refactor out common error message prefix
            throw new OhmException(
                    "Failed to create semantics blueprint of '%s'. Semantics classes must not be abstract to be instantiated."
                            .formatted(semanticsClass.getCanonicalName()));
        }
        
        boolean isStandalone = !semanticsClass.isMemberClass() || Modifier.isStatic(modifiers);
        Class<?> declaringClass = semanticsClass.getDeclaringClass();
        Class<?> enclosingClass = isStandalone ? null : declaringClass;
        
        if (!isStandalone && declaringClass == null) {
            throw new OhmException(
                    "Failed to create semantics blueprint of '%s'. Defining semantics as local or anonymous classes is not allowed."
                            .formatted(semanticsClass.getCanonicalName()));
        }
        
        Constructor<T> constructor;
        try {
            if (isStandalone) {
                constructor = semanticsClass.getConstructor();
            } else {
                constructor = semanticsClass.getConstructor(declaringClass);
            }
        } catch (SecurityException e) {
            throw new OhmException(
                    "Failed to create semantics blueprint of '%s'. Security exception on constructor access."
                            .formatted(semanticsClass.getCanonicalName()), e);
        } catch (NoSuchMethodException e) {
            throw new OhmException(
                    "Failed to create semantics blueprint of '%s'. No public zero-arg constructor was found. Either declare it public explicitly or change the class's access modifier to public."
                            .formatted(semanticsClass.getCanonicalName()), e);
        }
        
        Map<String, SemanticAction> actionMap = gatherActionMap(semanticsClass);
        
        return new SemanticsBlueprint<T>(semanticsClass, grammar, constructor, actionMap, enclosingClass);
    }
    
    public T on(MatchResult matchResult) {
        if (matchResult.failed()) {
            throw new OhmException("Cannot instantiate semantics '%s' for a match result that failed."
                    .formatted(semanticsClass.getCanonicalName()));
        }
        return on(matchResult.getRootNode());
    }
    
    T on(Node rootNode) {
        // TODO: why do we do this? just let people instantiate it themselves
        T result = instantiate();
        result.rootNode = rootNode;
        return result;
    }
    
    T instantiate() {
        if (enclosingClass != null) {
            throw new OhmException("Cannot instantiate this semantics blueprint without the enclosing instance. Use #instatiate(Object) instead or make your semantics class static.");
        }
        return instantiate(null);
    }
    
    T instantiate(Object enclosingInstance) {
        T semantics;
        try {
            if (enclosingClass == null) {
                semantics = constructor.newInstance();
            } else {
                if (!enclosingClass.isInstance(enclosingInstance)) {
                    throw new OhmException(
                            "Failed to instantiate semantics '%s'. Provided enclosing instance is not an instance of the enclosing class (%s)."
                                    .formatted(semanticsClass.getCanonicalName(), enclosingClass.getCanonicalName()));
                }
                semantics = constructor.newInstance(enclosingInstance);
            }
        } catch (InvocationTargetException e) {
            throw new OhmException(
                    "Failed to instantiate semantics '%s'. Exception thrown during constructor."
                            .formatted(semanticsClass.getCanonicalName()), e);
        } catch (InstantiationException e) {
            // should not be possible (class is checked to not be abstract during creation)
            throw new OhmException("Cannot instantiate semantics '%s'. The semantics class is abstract."
                    .formatted(semanticsClass.getCanonicalName()), e);
        } catch (IllegalArgumentException e) {
            // should not be possible
            throw new OhmException("Cannot instantiate semantics '%s'. Illegal arguments."
                    .formatted(semanticsClass.getCanonicalName()), e);
        } catch (IllegalAccessException e) {
            // should not be possible
            throw new OhmException("Cannot instantiate semantics '%s'. Constructor is inaccessible."
                    .formatted(semanticsClass.getCanonicalName()), e);
        }
        
        semantics.grammar = grammar;
        semantics.actionMap = actionMap;
        semantics.initialize();
        return semantics;
    }
    
    private static Map<String, SemanticAction> gatherActionMap(
            Class<? extends Semantics> semanticsClass) {
        Map<String, SemanticAction> actionMap = new HashMap<>();
        
        // TODO: could throw SecurityException -> what then?
        for (Method method : semanticsClass.getMethods()) {
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
    
    // TODO: actually use this
    private static void validateActionMap(Map<String, SemanticAction> actionMap, Grammar grammar) {
        actionMap.forEach((ruleName, action) -> {
            action.validateAgainstGrammar(grammar);
        });
    }
}
