package ohm.java;

import static ohm.java.MockNode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ohm.java.SemanticActions.SpecialActionNames;

class OperationTest {
	SemanticActions op;
	Map<String, Method> actionMap;

	@Test
	void testNonterminalAlwaysHasDefaultAction() {
		op = new SemanticActions();
		assertTrue(op.actionMap.containsKey(SpecialActionNames.nonterminal));
	}

	class ActionWithMultipleNames extends SemanticActions {
		@Action("Exp")
		@Action("AddExp")
		public String defaultExp() {
			return "defaultExp";
		}
	}

	@Test
	void testActionWithMultipleNames()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		op = new ActionWithMultipleNames();

		assertEquals("defaultExp", (String) op.actionMap.get("Exp").invoke(op));
		assertEquals("defaultExp", (String) op.actionMap.get("AddExp").invoke(op));
	}

	class NonVarArgsAction extends SemanticActions {
		@Action("AddExp_plus")
		public String AddExp_plus(Node node) {
			return "AddExp_plus";
		}
	}

	@Test
	void testNonVarArgsAction() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		op = new NonVarArgsAction();

		assertEquals("AddExp_plus", (String) op.actionMap.get("AddExp_plus").invoke(op, (Object) null));
	}

	class VarArgsAction extends SemanticActions {
		@Action
		public String AddExp_minus(Node... nodes) {
			return "AddExp_minus";
		}
	}

	@Test
	void testVarArgsAction() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		op = new VarArgsAction();

		assertEquals("AddExp_minus", (String) op.actionMap.get("AddExp_minus").invoke(op, (Object) new Node[0]));
	}

	class DuplicateActionOperation extends SemanticActions {
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
		assertThrows(OhmException.class, () -> new DuplicateActionOperation());
	}

	class WrongParameterOperation extends SemanticActions {
		@Action
		public String Exp(int index) {
			return "Exp";
		}
	}

	@Test
	void testWrongParameterType() {
		assertThrows(OhmException.class, () -> new WrongParameterOperation());
	}

	class WrongVarArgsParameterOperation extends SemanticActions {
		@Action
		public String Exp(int... indices) {
			return "Exp";
		}
	}

	@Test
	void testWrongVarArgsParameterType() {
		assertThrows(OhmException.class, () -> new WrongVarArgsParameterOperation());
	}

	class SimpleOperation extends SemanticActions {
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
		SemanticActions op = new SimpleOperation();

		assertEquals("Rule B", op.apply(Nonterminal("RuleB")));
		assertEquals("Rule B", op.apply(Nonterminal("RuleA", Nonterminal("RuleB"))));
		assertEquals("Rule C", op.apply(Nonterminal("RuleA", Nonterminal("RuleC", Nonterminal("RuleB")))));
	}

	@Test
	void testApplyDefaultNonterminalSpecialAction() {
		SemanticActions op = new SimpleOperation();
		assertEquals("Rule B", op.apply(Nonterminal("RuleX", Nonterminal("RuleB"))));
	}

	class SpecialActions extends SemanticActions {
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
		SemanticActions op = new SpecialActions();

		assertEquals("Rule A", op.apply(Nonterminal("RuleA")));
		assertEquals("default action", op.apply(Nonterminal("RuleX")));
		assertEquals("default action", op.apply(Terminal()));
		assertEquals("default action", op.apply(Iter()));
	}

}
