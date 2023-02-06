package ohm.java;

public class TerminalNode extends AbstractNode {
	private static final Node[] children;

	public static TerminalNode get(int matchLength) {
		// TODO: introduce cache up to length 20
		return new TerminalNode(matchLength);
	}

	private TerminalNode(int matchLength) {
		super(matchLength);
	}

	static {
		children = new Node[0];
	}

	@Override
	public String ctorName() {
		return "_terminal";
	}

	@Override
	public Node[] getChildren() {
		return children;
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
