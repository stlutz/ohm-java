package net.stlutz.ohm;

public interface Node {
  /**
   * Returns the name of grammar rule that created this CST node.
   */
  abstract String ctorName();

  /**
   * Returns a `SourceInterval` representing the portion of the input that was consumed by this CST
   * node.
   */
  abstract SourceInterval getSource();

  /**
   * Returns the contents of the input stream consumed by this CST node.
   */
  default String sourceString() {
    return getSource().getContents();
  }

  /**
   * Returns an array containing the children of this CST node.
   */
  abstract Node[] getChildren();

  /**
   * Returns the number of children of this CST node.
   */
  default int numChildren() {
    return getChildren().length;
  }

  default Node childAt(int index) {
    return getChildren()[index];
  }

  default int indexOfChild(Node childToFind) {
    for (int i = 0; i < numChildren(); i++) {
      if (childAt(i) == childToFind) {
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
      throw new OhmException(String
          .format("Cannot get only child of node %s. It does not have only 1 child", toString()));
    }
    return childAt(0);
  }

  default Node firstChild() {
    if (hasNoChildren()) {
      throw new OhmException(
          String.format("Cannot get first child of node %s. It has no children.", toString()));
    }
    return childAt(0);
  }

  default Node lastChild() {
    if (hasNoChildren()) {
      throw new OhmException(
          String.format("Cannot get last child of node %s. It has no children.", toString()));
    }
    return childAt(numChildren() - 1);
  }

  default Node childBefore(Node child) {
    int childIndex = indexOfChild(child);
    if (childIndex < 0) {
      throw new OhmException(String.format("Cannot get child before. %s is not a child of %s.",
          child.toString(), toString()));
    } else if (childIndex == 0) {
      throw new OhmException(String.format("Cannot get child before %s in %s. It is first.",
          child.toString(), toString()));
    }
    return childAt(childIndex - 1);
  }

  default Node childAfter(Node child) {
    int childIndex = indexOfChild(child);
    if (childIndex < 0) {
      throw new OhmException(String.format("Cannot get child after. %s is not a child of %s.",
          child.toString(), toString()));
    } else if (childIndex == numChildren() - 1) {
      throw new OhmException(String.format("Cannot get child after %s in %s. It is last.",
          child.toString(), toString()));
    }
    return childAt(childIndex + 1);
  }

  /**
   * Returns `true` if the receiver node corresponds to an iteration expression, i.e., a Kleene-*,
   * Kleene-+, or an optional. Returns `false` otherwise.
   */
  abstract boolean isIteration();

  /**
   * Returns `true` if the receiver node is a terminal node, `false` otherwise.
   */
  abstract boolean isTerminal();

  /**
   * Returns `true` if the receiver node is a nonterminal node, `false` otherwise.
   */
  abstract boolean isNonterminal();

  /**
   * Returns `true` if the receiver node is a nonterminal node corresponding to a syntactic rule,
   * `false` otherwise.
   */
  abstract boolean isSyntactic();

  /**
   * Returns `true` if the receiver node is a nonterminal node corresponding to a lexical rule,
   * `false` otherwise.
   */
  abstract boolean isLexical();

  /**
   * Returns `true` if the receiver node is an iterator node having either one or no child (?
   * operator), `false` otherwise.
   */
  abstract boolean isOptional();
}
