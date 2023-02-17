package net.stlutz.ohm;

import net.stlutz.ohm.SemanticActions.SpecialActionNames;

public abstract class MockNode implements Node {
	public static IterNode Iter(Node... children) {
		return new IterNode(children);
	}

	public static NonterminalNode Nonterminal(String ruleName, Node... children) {
		return new NonterminalNode(ruleName, children);
	}

	public static NonterminalNode Nonterminal(String ruleName) {
		return new NonterminalNode(ruleName, new Node[0]);
	}

	public static TerminalNode Terminal() {
		return new TerminalNode();
	}

	static class IterNode extends MockNode {
		Node[] children;

		IterNode(Node[] children) {
			super();
			this.children = children;
		}

		@Override
		public String ctorName() {
			return SpecialActionNames.iteration;
		}

		@Override
		public Node[] getChildren() {
			return children;
		}

		@Override
		public boolean isIteration() {
			return true;
		}
	}

	static class NonterminalNode extends MockNode {
		String ruleName;
		Node[] children;

		NonterminalNode(String ruleName, Node[] children) {
			super();
			this.ruleName = ruleName;
			this.children = children;
		}

		@Override
		public String ctorName() {
			return ruleName;
		}

		@Override
		public Node[] getChildren() {
			return children;
		}

		@Override
		public boolean isNonterminal() {
			return true;
		}

		@Override
		public boolean isSyntactic() {
			return Util.isSyntactic(ruleName);
		}

		@Override
		public boolean isLexical() {
			return Util.isLexical(ruleName);
		}
	}

	static class TerminalNode extends MockNode {
		@Override
		public String ctorName() {
			return SpecialActionNames.terminal;
		}

		@Override
		public Node[] getChildren() {
			return new Node[0];
		}

		@Override
		public boolean isTerminal() {
			return true;
		}
	}

	@Override
	public SourceInterval getSource() {
		return null;
	}

	@Override
	public boolean isIteration() {
		return false;
	}

	@Override
	public boolean isTerminal() {
		return false;
	}

	@Override
	public boolean isNonterminal() {
		return false;
	}

	@Override
	public boolean isSyntactic() {
		return false;
	}

	@Override
	public boolean isLexical() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

}
