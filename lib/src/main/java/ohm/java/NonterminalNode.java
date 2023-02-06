package ohm.java;

public class NonterminalNode extends AbstractNode {
	private final String ruleName;
	private final Node[] children;
	private final int[] childOffsets;

	public NonterminalNode(int matchLength, String ruleName, Node[] children, int[] childOffsets) {
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
	public Node[] getChildren() {
		return children;
	}

	@Override
	public boolean isNonterminal() {
		return true;
	}

	public boolean isLexical() {
		return Util.isLexical(ruleName);
	}

	public boolean isSyntactic() {
		return Util.isSyntactic(ruleName);
	}
}
