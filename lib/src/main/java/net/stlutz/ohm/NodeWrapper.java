package net.stlutz.ohm;

public class NodeWrapper implements Node {
    protected final ParseNode node;
    protected final SourceInterval sourceInterval;
    /**
     * The interval that the childOffsets of `node` are relative to. It should be the source of the
     * closest Nonterminal node.
     */
    protected final SourceInterval baseInterval;
    protected NodeWrapper[] wrappedChildren;
    
    public NodeWrapper(ParseNode node, SourceInterval sourceInterval, SourceInterval baseInterval) {
        super();
        this.node = node;
        this.sourceInterval = sourceInterval;
        this.baseInterval = baseInterval;
    }
    
    public NodeWrapper(ParseNode node, SourceInterval sourceInterval) {
        this(node, sourceInterval, sourceInterval);
    }
    
    @Override
    public String ctorName() {
        return node.ctorName();
    }
    
    @Override
    public SourceInterval getSource() {
        return sourceInterval;
    }
    
    @Override
    public int numChildren() {
        return node.numChildren();
    }
    
    protected NodeWrapper wrap(ParseNode node, SourceInterval sourceInterval,
                               SourceInterval baseInterval) {
        return new NodeWrapper(node, sourceInterval, baseInterval);
    }
    
    /**
     * Returns the wrapper of the child node at the specified `childIndex`.
     */
    protected NodeWrapper wrap(int childIndex) {
        ParseNode childNode = node.getChildren()[childIndex];
        int offset = node.getChildOffsets()[childIndex];
        
        SourceInterval source = baseInterval.subInterval(offset, childNode.matchLength);
        SourceInterval base = childNode.isNonterminal() ? source : baseInterval;
        return wrap(childNode, source, base);
    }
    
    protected void ensureWrappedChildren() {
        if (wrappedChildren == null) {
            wrappedChildren = new NodeWrapper[node.numChildren()];
        }
    }
    
    /**
     * Returns the wrapper of the specified child node. Child wrappers are created lazily and cached
     * in the parent wrapper's `wrappedChildren` instance variable.
     */
    @Override
    public Node childAt(int index) {
        if (index < 0 || index >= numChildren()) {
            throw new IndexOutOfBoundsException(index);
        }
        ensureWrappedChildren();
        
        NodeWrapper childWrapper = wrappedChildren[index];
        if (childWrapper == null) {
            childWrapper = wrappedChildren[index] = wrap(index);
        }
        
        return childWrapper;
    }
    
    /**
     * Returns an array containing the wrappers of all of the children of the node associated with
     * this wrapper.
     */
    @Override
    public Node[] getChildren() {
        ensureWrappedChildren();
        for (int i = 0; i < wrappedChildren.length; i++) {
            if (wrappedChildren[i] == null) {
                wrappedChildren[i] = wrap(i);
            }
        }
        
        return wrappedChildren;
    }
    
    public boolean isIteration() {
        return node.isIteration();
    }
    
    public boolean isTerminal() {
        return node.isTerminal();
    }
    
    public boolean isNonterminal() {
        return node.isNonterminal();
    }
    
    public boolean isSyntactic() {
        return node.isSyntactic();
    }
    
    public boolean isLexical() {
        return node.isLexical();
    }
    
    public boolean isOptional() {
        return node.isOptional();
    }
}
