package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

class RuleDefinition {
    String name;
    SourceInterval sourceInterval;
    String description;
    PExpr body;
    Rule.Operation operation = Rule.Operation.DEFINE;
    String[] formals = new String[0];
    
    RuleDefinition(String name) {
        super();
        this.name = name;
    }
    
    /**
     * Optional.
     */
    public RuleDefinition sourceInterval(SourceInterval sourceInterval) {
        this.sourceInterval = sourceInterval;
        return this;
    }
    
    /**
     * Optional.
     */
    public RuleDefinition description(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Required. Must not be null.
     */
    public RuleDefinition body(PExpr body) {
        this.body = body;
        return this;
    }
    
    /**
     * Required (satisfied by constructor). Must not be null.
     */
    public RuleDefinition name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Default.
     */
    public RuleDefinition define() {
        operation = Rule.Operation.DEFINE;
        return this;
    }
    
    public RuleDefinition extend() {
        operation = Rule.Operation.EXTEND;
        return this;
    }
    
    public RuleDefinition override() {
        operation = Rule.Operation.OVERRIDE;
        return this;
    }
    
    /**
     * Optional.
     */
    public RuleDefinition formals(String... formals) {
        this.formals = formals;
        return this;
    }
    
    boolean isDefinition() {
        return operation == Rule.Operation.DEFINE;
    }
    
    boolean isExtension() {
        return operation == Rule.Operation.EXTEND;
    }
    
    boolean isOverride() {
        return operation == Rule.Operation.OVERRIDE;
    }
}
