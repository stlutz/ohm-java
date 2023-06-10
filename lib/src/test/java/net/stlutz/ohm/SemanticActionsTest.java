package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static net.stlutz.ohm.MockNode.Iter;
import static net.stlutz.ohm.MockNode.Nonterminal;
import static net.stlutz.ohm.MockNode.Terminal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticActionsTest {
    Semantics semantics;
    Map<String, Method> actionMap;
    
    static Semantics make(Class<? extends Semantics> semanticsClass) {
        return SemanticsBlueprint.create(semanticsClass).instantiate();
    }
    
    @Test
    void testNonterminalAlwaysHasDefaultAction() {
        semantics = make(Semantics.class);
        assertTrue(semantics.hasAction(Semantics.SpecialActionNames.nonterminal));
        assertFalse(semantics.hasAction(Semantics.SpecialActionNames.terminal));
        assertFalse(semantics.hasAction(Semantics.SpecialActionNames.iteration));
    }
    
    public static class ActionWithMultipleNames extends Semantics {
        @Action("Exp")
        @Action("AddExp")
        public String defaultExp() {
            return "defaultExp";
        }
    }
    
    @Test
    void testActionWithMultipleNames() {
        semantics = make(ActionWithMultipleNames.class);
        assertTrue(semantics.hasAction("Exp"));
        assertTrue(semantics.hasAction("AddExp"));
    }
    
    public static class NonVarArgsAction extends Semantics {
        @Action("Rule0")
        public String Rule0() {
            return "Rule0";
        }
        
        @Action("Rule1")
        public String Rule1(Node node) {
            return "Rule1";
        }
        
        @Action("Rule2")
        public String Rule2(Node node1, Node node2) {
            return "Rule2";
        }
    }
    
    @Test
    void testNonVarArgsAction() {
        semantics = make(NonVarArgsAction.class);
        
        assertTrue(semantics.hasAction("Rule0"));
        assertTrue(semantics.hasAction("Rule1"));
        assertTrue(semantics.hasAction("Rule2"));
    }
    
    public static class VarArgsAction extends Semantics {
        @Action
        public String RuleA(Node... nodes) {
            return "RuleA";
        }
        
        @Action
        public String RuleB(Node[] nodes) {
            return "RuleB";
        }
    }
    
    @Test
    void testVarArgsAction() {
        semantics = make(VarArgsAction.class);
        
        assertTrue(semantics.hasAction("RuleA"));
        assertTrue(semantics.hasAction("RuleB"));
    }
    
    public static class SimpleSemantics extends Semantics {
        @Action("RuleA")
        public String RuleA(Node node) {
            return String.class.cast(apply(node));
        }
        
        @Action("RuleB")
        public String RuleB() {
            return "Rule B";
        }
        
        @Action("RuleC")
        public String RuleC(Node... node) {
            return "Rule C";
        }
    }
    
    @Test
    void testApply() {
        semantics = make(SimpleSemantics.class);
        
        assertEquals("Rule B", semantics.apply(Nonterminal("RuleB")));
        assertEquals("Rule B", semantics.apply(Nonterminal("RuleA", Nonterminal("RuleB"))));
        assertEquals("Rule C",
                semantics.apply(Nonterminal("RuleA", Nonterminal("RuleC", Nonterminal("RuleB")))));
    }
    
    @Test
    void testApplyDefaultNonterminalSpecialAction() {
        semantics = make(SimpleSemantics.class);
        assertEquals("Rule B", semantics.apply(Nonterminal("RuleX", Nonterminal("RuleB"))));
    }
    
    public static class ExtensionSemantics extends SimpleSemantics {
        @Action("RuleA")
        public String RuleA(Node node) {
            return "better " + super.RuleA(node);
        }
        
        @Action("RuleB")
        public String newRuleB() {
            return "new Rule B";
        }
    }
    
    @Test
    void testApplyExtension() {
        semantics = make(ExtensionSemantics.class);
        
        assertEquals("new Rule B", semantics.apply(Nonterminal("RuleB")));
        assertEquals("better new Rule B", semantics.apply(Nonterminal("RuleA", Nonterminal("RuleB"))));
        assertEquals("better Rule C",
                semantics.apply(Nonterminal("RuleA", Nonterminal("RuleC", Nonterminal("RuleB")))));
    }
    
    public static class SpecialActions extends Semantics {
        @Action("RuleA")
        public String RuleA() {
            return "Rule A";
        }
        
        @Action(Semantics.SpecialActionNames.iteration)
        @Action(Semantics.SpecialActionNames.terminal)
        @Action(Semantics.SpecialActionNames.nonterminal)
        public String defaultAction(Node... children) {
            return "default action";
        }
    }
    
    @Test
    void testApplySpecialActions() {
        semantics = make(SpecialActions.class);
        
        assertEquals("Rule A", semantics.apply(Nonterminal("RuleA")));
        assertEquals("default action", semantics.apply(Nonterminal("RuleX")));
        assertEquals("default action", semantics.apply(Terminal()));
        assertEquals("default action", semantics.apply(Iter()));
    }
    
    public static class DuplicateActionSemantics extends Semantics {
        @Action("Exp")
        public String Exp() {
            return "Exp";
        }
        
        @Action("Exp")
        public String AddExp() {
            return "AddExp";
        }
    }
    
    @Test
    void testDuplicateActions() {
        assertThrows(OhmException.class, () -> make(DuplicateActionSemantics.class));
    }
    
    public static class WrongParameterSemantics extends Semantics {
        @Action
        public String Exp(int index) {
            return "Exp";
        }
    }
    
    @Test
    void testWrongParameterType() {
        assertThrows(OhmException.class, () -> make(WrongParameterSemantics.class));
    }
    
    public static class WrongVarArgsParameterSemantics extends Semantics {
        @Action
        public String Exp(int... indices) {
            return "Exp";
        }
    }
    
    @Test
    void testWrongVarArgsParameterType() {
        assertThrows(OhmException.class, () -> make(WrongVarArgsParameterSemantics.class));
    }
    
    public static class MixedVarArgsParameterSemantics extends Semantics {
        @Action
        public String Exp(Node first, Node... rest) {
            return "Exp";
        }
    }
    
    @Test
    void testMixedVarArgsParameterType() {
        assertThrows(OhmException.class, () -> make(MixedVarArgsParameterSemantics.class));
    }
    
    public static class DuplicateActionInSuperSemantics extends DuplicateActionSemantics {
        @Action
        public String Rule() {
            return "Rule";
        }
    }
    
    @Test
    void testDuplicateActionInSuperSemantics() {
        assertThrows(OhmException.class, () -> make(DuplicateActionInSuperSemantics.class));
    }
    
    public static class NoDefaultConstructorSemantics extends Semantics {
        NoDefaultConstructorSemantics(boolean isBetter) {
            super();
        }
    }
    
    @Test
    void testNoDefaultConstructor() {
        assertThrows(OhmException.class, () -> make(NoDefaultConstructorSemantics.class));
    }
    
    public static class MySemantics extends Semantics {
        public static class InnerSemantics extends Semantics {
            @Action
            public String Rule() {
                return "Rule";
            }
        }
    }
    
    @Test
    void testInnerSemantics() {
        semantics = make(MySemantics.InnerSemantics.class);
        
        assertTrue(semantics.hasAction("Rule"));
    }
}
