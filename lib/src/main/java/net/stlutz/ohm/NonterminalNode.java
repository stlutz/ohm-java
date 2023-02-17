package net.stlutz.ohm;

public class NonterminalNode extends ParseNode {
	private final String ruleName;
	private final ParseNode[] children;
	private final int[] childOffsets;

	public NonterminalNode(int matchLength, String ruleName, ParseNode[] children, int[] childOffsets) {
		super(matchLength);
		this.ruleName = ruleName;
		this.children = children;
		this.childOffsets = childOffsets;
	}

	@Override
	public String ctorName() {
		return ruleName;
	}

	@Override
	public ParseNode[] getChildren() {
		return children;
	}

	@Override
	public int[] getChildOffsets() {
		return childOffsets;
	}

	@Override
	public boolean isNonterminal() {
		return true;
	}

	@Override
	public boolean isLexical() {
		return Util.isLexical(ruleName);
	}

	@Override
	public boolean isSyntactic() {
		return Util.isSyntactic(ruleName);
	}
}
