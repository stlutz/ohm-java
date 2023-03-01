package net.stlutz.ohm;

import net.stlutz.ohm.Operation.SpecialActionNames;

public abstract class MockNode implements Node {
	public static IterNode Iter(Node... children) {
		return new IterNode(children);
	}

	public static NonterminalNode Nonterminal(String ruleName, Node... children) {
		return new NonterminalNode(ruleName, children);
	}

//	public static NonterminalNode Nonterminal(String ruleName) {
//		return new NonterminalNode(ruleName, new Node[0]);
//	}

	public static TerminalNode Terminal() {
		return new TerminalNode("");
	}

	public static TerminalNode Terminal(String contents) {
		return new TerminalNode(contents);
	}

	public static NonterminalNode number(int n) {
		String numStr = Integer.toString(n);
		NonterminalNode[] digits = new NonterminalNode[numStr.length()];
		for (int i = 0; i < digits.length; i++) {
			digits[i] = digit(numStr.charAt(i));
		}
		return Nonterminal("number", Nonterminal("number_whole", Iter(digits)));
	}

	public static NonterminalNode digit(char digit) {
		assert Character.isDigit(digit);
		return Nonterminal("digit", Terminal(String.valueOf(digit)));
	}

	public static NonterminalNode letter(char letter) {
		assert Character.isLetter(letter);
		return Nonterminal("letter",
				Nonterminal(Character.isLowerCase(letter) ? "lower" : "upper", Terminal(String.valueOf(letter))));
	}

	public static NonterminalNode alnum(char alnum) {
		assert Character.isLetterOrDigit(alnum);
		return Nonterminal("alnum", Character.isDigit(alnum) ? digit(alnum) : letter(alnum));
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
		final String contents;

		TerminalNode(String contents) {
			super();
			this.contents = contents;
		}

		@Override
		public String sourceString() {
			return contents;
		}

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
	public String sourceString() {
		// this works for iter nodes only as long as they are not interleaved (e.g.
		// (digit letter)+ would break)
		String result = "";
		for (Node child : getChildren()) {
			result += child.sourceString();
		}
		return result;
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
