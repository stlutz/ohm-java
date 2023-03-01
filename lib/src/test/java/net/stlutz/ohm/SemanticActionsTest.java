package net.stlutz.ohm;

import static net.stlutz.ohm.MockNode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.stlutz.ohm.SemanticActions.SpecialActionNames;

class SemanticActionsTest {
	SemanticActions op;
	Map<String, Method> actionMap;

	static SemanticActions make(Class<? extends SemanticActions> opClass) {
		return OperationBlueprint.create(opClass).make();
	}

	@Test
	void testNonterminalAlwaysHasDefaultAction() {
		op = make(SemanticActions.class);
		assertTrue(op.hasAction(SpecialActionNames.nonterminal));
		assertFalse(op.hasAction(SpecialActionNames.terminal));
		assertFalse(op.hasAction(SpecialActionNames.iteration));
	}

	public static class ActionWithMultipleNames extends SemanticActions {
		@Action("Exp")
		@Action("AddExp")
		public String defaultExp() {
			return "defaultExp";
		}
	}

	@Test
	void testActionWithMultipleNames() {
		op = make(ActionWithMultipleNames.class);
		assertTrue(op.hasAction("Exp"));
		assertTrue(op.hasAction("AddExp"));
	}

	public static class NonVarArgsAction extends SemanticActions {
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
		op = make(NonVarArgsAction.class);

		assertTrue(op.hasAction("Rule0"));
		assertTrue(op.hasAction("Rule1"));
		assertTrue(op.hasAction("Rule2"));
	}

	public static class VarArgsAction extends SemanticActions {
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
		op = make(VarArgsAction.class);

		assertTrue(op.hasAction("RuleA"));
		assertTrue(op.hasAction("RuleB"));
	}

	public static class SimpleOperation extends SemanticActions {
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
		SemanticActions op = make(SimpleOperation.class);

		assertEquals("Rule B", op.apply(Nonterminal("RuleB")));
		assertEquals("Rule B", op.apply(Nonterminal("RuleA", Nonterminal("RuleB"))));
		assertEquals("Rule C", op.apply(Nonterminal("RuleA", Nonterminal("RuleC", Nonterminal("RuleB")))));
	}

	@Test
	void testApplyDefaultNonterminalSpecialAction() {
		SemanticActions op = make(SimpleOperation.class);
		assertEquals("Rule B", op.apply(Nonterminal("RuleX", Nonterminal("RuleB"))));
	}

	public static class ExtensionOperation extends SimpleOperation {
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
		SemanticActions op = make(ExtensionOperation.class);

		assertEquals("new Rule B", op.apply(Nonterminal("RuleB")));
		assertEquals("better new Rule B", op.apply(Nonterminal("RuleA", Nonterminal("RuleB"))));
		assertEquals("better Rule C", op.apply(Nonterminal("RuleA", Nonterminal("RuleC", Nonterminal("RuleB")))));
	}

	public static class SpecialActions extends SemanticActions {
		@Action("RuleA")
		public String RuleA() {
			return "Rule A";
		}

		@Action(SpecialActionNames.iteration)
		@Action(SpecialActionNames.terminal)
		@Action(SpecialActionNames.nonterminal)
		public String defaultAction(Node... children) {
			return "default action";
		}
	}

	@Test
	void testApplySpecialActions() {
		SemanticActions op = make(SpecialActions.class);

		assertEquals("Rule A", op.apply(Nonterminal("RuleA")));
		assertEquals("default action", op.apply(Nonterminal("RuleX")));
		assertEquals("default action", op.apply(Terminal()));
		assertEquals("default action", op.apply(Iter()));
	}

	public static class DuplicateActionOperation extends SemanticActions {
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
		assertThrows(OhmException.class, () -> make(DuplicateActionOperation.class));
	}

	public static class WrongParameterOperation extends SemanticActions {
		@Action
		public String Exp(int index) {
			return "Exp";
		}
	}

	@Test
	void testWrongParameterType() {
		assertThrows(OhmException.class, () -> make(WrongParameterOperation.class));
	}

	public static class WrongVarArgsParameterOperation extends SemanticActions {
		@Action
		public String Exp(int... indices) {
			return "Exp";
		}
	}

	@Test
	void testWrongVarArgsParameterType() {
		assertThrows(OhmException.class, () -> make(WrongVarArgsParameterOperation.class));
	}

	public static class MixedVarArgsParameterOperation extends SemanticActions {
		@Action
		public String Exp(Node first, Node... rest) {
			return "Exp";
		}
	}

	@Test
	void testMixedVarArgsParameterType() {
		assertThrows(OhmException.class, () -> make(MixedVarArgsParameterOperation.class));
	}

	public static class DuplicateActionInSuperOperation extends DuplicateActionOperation {
		@Action
		public String Rule() {
			return "Rule";
		}
	}

	@Test
	void testDuplicateActionInSuperOperation() {
		assertThrows(OhmException.class, () -> make(DuplicateActionInSuperOperation.class));
	}

	public static class NoDefaultConstructorOperation extends SemanticActions {
		NoDefaultConstructorOperation(boolean isBetter) {
			super();
		}
	}

	@Test
	void testNoDefaultConstructor() {
		assertThrows(OhmException.class, () -> make(NoDefaultConstructorOperation.class));
	}

	public static class MySemantics extends Semantics {
		public class InnerOperation extends SemanticActions {
			@Action
			public String Rule() {
				return "Rule";
			}
		}
	}

	@Test
	void testInnerOperation() {
		op = make(MySemantics.InnerOperation.class);

		assertTrue(op.hasAction("Rule"));
	}
}
