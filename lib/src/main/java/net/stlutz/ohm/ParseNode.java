package net.stlutz.ohm;

public abstract class ParseNode {
  final int matchLength;

  public int getMatchLength() {
    return matchLength;
  }

  public ParseNode(int matchLength) {
    super();
    this.matchLength = matchLength;
  }

  public abstract String ctorName();

  public abstract ParseNode[] getChildren();

  public abstract int[] getChildOffsets();

  public String toString() {
    return "Node($1)".formatted(matchLength);
  }

  public boolean hasChildren() {
    return numChildren() > 0;
  }

  public int numChildren() {
    return getChildren().length;
  }

  public boolean isIteration() {
    return false;
  }

  public boolean isTerminal() {
    return false;
  }

  public boolean isNonterminal() {
    return false;
  }

  public boolean isSyntactic() {
    return false;
  }

  public boolean isLexical() {
    return false;
  }

  public boolean isOptional() {
    return false;
  }
}
