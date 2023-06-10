package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

public class RuleImpl implements Rule {
    final Operation operation;
    final PExpr body;
    final String[] formals;
    final String description;
    final SourceInterval source;
    
    public RuleImpl(PExpr body, String[] formals, String description, SourceInterval source, Operation operation) {
        super();
        this.operation = operation;
        this.body = body;
        this.formals = formals;
        this.description = description;
        this.source = source;
    }
    
    RuleImpl(Rule toCopy) {
        this(toCopy.getBody(), toCopy.getFormals(), toCopy.getDescription(), null, toCopy.getOperation());
    }
    
    public static RuleImpl copyOf(Rule rule) {
        return new RuleImpl(rule);
    }
    
    @Override
    public Operation getOperation() {
        return operation;
    }
    
    @Override
    public int getArity() {
        return body.getArity();
    }
    
    @Override
    public String[] getFormals() {
        return formals;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public PExpr getBody() {
        return body;
    }
    
    public SourceInterval getSource() {
        return source;
    }
}
