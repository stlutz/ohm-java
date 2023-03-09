package net.stlutz.ohm;

public class IterationNode extends ParseNode {
  private final boolean optional;
  private final ParseNode[] children;
  private final int[] childOffsets;

  public IterationNode(int matchLength, ParseNode[] children, int[] childOffsets,
      boolean optional) {
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
  public ParseNode[] getChildren() {
    return children;
  }

  @Override
  public int[] getChildOffsets() {
    return childOffsets;
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
