package ohm.java;

public abstract class AbstractNode implements Node {
	final int matchLength;

	public int getMatchLength() {
		return matchLength;
	}

	public AbstractNode(int matchLength) {
		super();
		this.matchLength = matchLength;
	}

	@Override
	public String toString() {
		return "Node($1)".formatted(matchLength);
	}
}
