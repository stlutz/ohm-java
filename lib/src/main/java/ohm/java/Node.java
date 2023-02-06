package ohm.java;

public interface Node {
	abstract String ctorName();

	abstract Node[] getChildren();

	abstract int getMatchLength();

	default int numChildren() {
		return getChildren().length;
	}

	default Node childAt(int index) {
		return getChildren()[index];
	}

	default int indexOfChild(Node childToFind) {
		Node[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			Node child = children[i];
			if (child == childToFind) {
				return i;
			}
		}
		return -1;
	}

	default boolean hasChildren() {
		return numChildren() > 0;
	}

	default boolean hasNoChildren() {
		return !hasChildren();
	}

	default Node onlyChild() {
		if (numChildren() != 1) {
			throw new OhmException(
					String.format("Cannot get only child of node %s. It does not have only 1 child", toString()));
		}
		return getChildren()[0];
	}

	default Node firstChild() {
		if (hasNoChildren()) {
			throw new OhmException(String.format("Cannot get first child of node %s. It has no children.", toString()));
		}
		return childAt(0);
	}

	default Node lastChild() {
		if (hasNoChildren()) {
			throw new OhmException(String.format("Cannot get last child of node %s. It has no children.", toString()));
		}
		return childAt(numChildren() - 1);
	}

	default Node childBefore(Node child) {
		int childIndex = indexOfChild(child);
		if (childIndex < 0) {
			throw new OhmException(
					String.format("Cannot get child before. %s is not a child of %s.", child.toString(), toString()));
		} else if (childIndex == 0) {
			throw new OhmException(
					String.format("Cannot get child before %s in %s. It is first.", child.toString(), toString()));
		}
		return childAt(childIndex - 1);
	}

	default Node childAfter(Node child) {
		int childIndex = indexOfChild(child);
		if (childIndex < 0) {
			throw new OhmException(
					String.format("Cannot get child after. %s is not a child of %s.", child.toString(), toString()));
		} else if (childIndex == numChildren() - 1) {
			throw new OhmException(
					String.format("Cannot get child after %s in %s. It is last.", child.toString(), toString()));
		}
		return childAt(childIndex + 1);
	}

	default boolean isIteration() {
		return false;
	}

	default boolean isNonterminal() {
		return false;
	}

	default boolean isTerminal() {
		return false;
	}

	default boolean isOptional() {
		return false;
	}
}
