package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Apply;

public class MatchResult {
    private final Matcher matcher;
    private final String input;
    private final Apply startApplication;
    private final ParseNode cst;
    private final int cstOffset;
    
    public MatchResult(Matcher matcher, String input, Apply startApplication, ParseNode cst,
                       int cstOffset) {
        super();
        this.matcher = matcher;
        this.input = input;
        this.startApplication = startApplication;
        this.cst = cst;
        this.cstOffset = cstOffset;
    }
    
    public Matcher getMatcher() {
        return matcher;
    }
    
    public String getInput() {
        return input;
    }
    
    public Apply getStartApplication() {
        return startApplication;
    }
    
    public SourceInterval getSourceInterval() {
        return new SourceInterval(input, cstOffset, input.length());
    }
    
    public Node getRootNode() {
        return new NodeWrapper(cst, getSourceInterval());
    }
    
    public ParseNode getCST() {
        return cst;
    }
    
    public int getCstOffset() {
        return cstOffset;
    }
    
    public boolean succeeded() {
        return cst != null;
    }
    
    public boolean failed() {
        return cst == null;
    }
}
