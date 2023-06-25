package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

public class ConstructedRule implements Rule {
    final Operation operation;
    final PExpr body;
    final String[] formals;
    final String description;
    final SourceInterval source;
    
    public ConstructedRule(PExpr body, String[] formals, String description, SourceInterval source, Operation operation) {
        super();
        this.operation = operation;
        this.body = body;
        this.formals = formals;
        this.description = description;
        this.source = source;
    }
    
    ConstructedRule(Rule toCopy) {
        this(toCopy.getBody(), toCopy.getFormals(), toCopy.getDescription(), null, toCopy.getOperation());
    }
    
    public static ConstructedRule copyOf(Rule rule) {
        return new ConstructedRule(rule);
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
