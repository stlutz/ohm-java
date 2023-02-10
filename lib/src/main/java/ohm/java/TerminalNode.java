package ohm.java;

public class TerminalNode extends ParseNode {
	private static final ParseNode[] defaultChildren = new ParseNode[0];
	private static final int[] defaultChildOffsets = new int[0];

	public static TerminalNode get(int matchLength) {
		// TODO: introduce cache up to length 20
		return new TerminalNode(matchLength);
	}

	private TerminalNode(int matchLength) {
		super(matchLength);
	}

	@Override
	public ParseNode[] getChildren() {
		return defaultChildren;
	}

	@Override
	public int[] getChildOffsets() {
		return defaultChildOffsets;
	}

	@Override
	public String ctorName() {
		return "_terminal";
	}

	@Override
	public int numChildren() {
		return 0;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public String toString() {
		return "Terminal($1)".formatted(matchLength);
	}

}
