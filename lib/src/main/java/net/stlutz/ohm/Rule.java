package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

public interface Rule {
    public enum Operation {
        DEFINE, EXTEND, OVERRIDE,
    }
    
    /**
     * Returns a parsing expression representing the body of this rule.
     *
     * @return This rule's parsing expression.
     */
    PExpr getBody();
    
    Operation getOperation();
    
    /**
     * Returns the arity of this rule. Arity indicates how many parse nodes an application of this rule would produce. Never negative.
     *
     * @return This rule's arity.
     */
    int getArity();
    
    // TODO: link definition of identifiers
    
    /**
     * Returns the parameter names defined for this rule. Parameter names are guaranteed to be identifiers.
     *
     * @return An array of parameter names.
     */
    String[] getFormals();
    
    /**
     * Returns a string describing the role of this rule.
     *
     * @return A description string, or {@code null} if this rule has no description.
     */
    String getDescription();
    
    /**
     * Returns whether this rule is a definition (i.e. declared using {@code =}).
     *
     * @return {@code true} if this rule is a definition.
     */
    default boolean isDefinition() {
        return getOperation() == Operation.DEFINE;
    }
    
    /**
     * Returns whether this rule is an override (i.e. declared using {@code :=}).
     *
     * @return {@code true} if this rule is an override.
     */
    default boolean isOverride() {
        return getOperation() == Operation.OVERRIDE;
    }
    
    /**
     * Returns whether this rule is an extension (i.e. declared using {@code +=}).
     *
     * @return {@code true} if this rule is an extension.
     */
    default boolean isExtension() {
        return getOperation() == Operation.EXTEND;
    }
}
