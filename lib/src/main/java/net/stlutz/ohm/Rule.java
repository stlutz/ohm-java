package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

import java.util.List;

public interface Rule {
    enum Operation {
        DEFINE, EXTEND, OVERRIDE,
    }
    
    /**
     * @return the name of this rule.
     */
    String getName();
    
    /**
     * @return a parsing expression representing the body of this rule.
     */
    PExpr getBody();
    
    Operation getOperation();
    
    /**
     * @return the arity of this rule. Arity indicates how many parse nodes an application of this rule would produce. Never negative.
     */
    int getArity();
    
    // TODO: link definition of identifiers
    
    /**
     * @return the parameter names defined for this rule. Parameter names are guaranteed to be identifiers.
     */
    List<String> getFormals();
    
    /**
     * @return a string describing the role of this rule, or {@code null} if this rule has no description.
     */
    String getDescription();
    
    /**
     * @return a string describing the role of this rule. Never null.
     */
    String getEffectiveDescription();
    
    /**
     * @return the source of this rule. Never null.
     */
    SourceInterval getSource();
    
    /**
     * @return {@code true} if this rule is a definition (i.e. declared using {@code =}).
     */
    default boolean isDefinition() {
        return getOperation() == Operation.DEFINE;
    }
    
    /**
     * @return {@code true} if this rule is an override (i.e. declared using {@code :=}).
     */
    default boolean isOverride() {
        return getOperation() == Operation.OVERRIDE;
    }
    
    /**
     * @return {@code true} if this rule is an extension (i.e. declared using {@code +=}).
     */
    default boolean isExtension() {
        return getOperation() == Operation.EXTEND;
    }
}
