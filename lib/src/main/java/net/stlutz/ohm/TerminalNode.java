package net.stlutz.ohm;

public class TerminalNode extends ParseNode {
    private static final ParseNode[] defaultChildren = new ParseNode[0];
    private static final int[] defaultChildOffsets = new int[0];
    
    private static final int numCachedInstances = 101;
    private static final TerminalNode[] cachedInstances = new TerminalNode[numCachedInstances];
    
    static {
        for (int i = 0; i < numCachedInstances; i++) {
            cachedInstances[i] = new TerminalNode(i);
        }
    }
    
    public static TerminalNode get(int matchLength) {
        if (matchLength < numCachedInstances) {
            return cachedInstances[matchLength];
        }
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
