package ohm.java;

public class IterationNode extends AbstractNode {
	private final boolean optional;
	private final Node[] children;
	private final int[] childOffsets;

	public IterationNode(int matchLength, Node[] children, int[] childOffsets, boolean optional) {
		super(matchLength);
		this.children = children;
		this.childOffsets = childOffsets;
		this.optional = optional;
	}

	@Override
	public String ctorName() {
		return "_iter";
	}

	@Override
	public Node[] getChildren() {
		return children;
	}

	@Override
	public boolean isIteration() {
		return true;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

}
