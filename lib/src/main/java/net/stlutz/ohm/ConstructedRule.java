package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

import java.util.List;

public class ConstructedRule implements Rule {
    final String name;
    final Operation operation;
    final PExpr body;
    final List<String> formals;
    final String description;
    final String effectiveDescription;
    final SourceInterval source;
    
    public ConstructedRule(String name, PExpr body, List<String> formals, String description, SourceInterval source, Operation operation) {
        this.name = name;
        this.operation = operation;
        this.body = body;
        this.formals = formals;
        this.description = description;
        // TODO: "an" for vowels?
        this.effectiveDescription = description != null ? description : "a " + name;
        this.source = source;
    }
    
    ConstructedRule(Rule toCopy) {
        this.name = toCopy.getName();
        this.operation = toCopy.getOperation();
        this.body = toCopy.getBody();
        this.formals = toCopy.getFormals();
        this.description = toCopy.getDescription();
        this.effectiveDescription = toCopy.getEffectiveDescription();
        this.source = toCopy.getSource();
    }
    
    public static ConstructedRule copyOf(Rule rule) {
        return new ConstructedRule(rule);
    }
    
    @Override public String getName() {
        return name;
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
    public List<String> getFormals() {
        return formals;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override public String getEffectiveDescription() {
        return effectiveDescription;
    }
    
    @Override
    public PExpr getBody() {
        return body;
    }
    
    @Override
    public SourceInterval getSource() {
        return source;
    }
}
