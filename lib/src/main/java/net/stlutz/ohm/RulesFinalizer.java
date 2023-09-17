package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Apply;
import net.stlutz.ohm.pexprs.BasePExprVisitor;

import java.util.Map;

public class RulesFinalizer extends BasePExprVisitor<Void> {
    private final Map<String, ? extends Rule> rules;
    
    private RulesFinalizer(Map<String, ? extends Rule> rules) {
        this.rules = rules;
    }
    
    public static void finalize(Map<String, ? extends Rule> rules) {
        RulesFinalizer finalizer = new RulesFinalizer(rules);
        for (Rule rule : rules.values()) {
            finalizer.visit(rule.getBody());
        }
    }
    
    @Override
    public Void visitApply(Apply expr) {
        Rule rule = rules.get(expr.getRuleName());
        if (rule == null) {
            // TODO: collect errors
            return null;
        }
        expr.setRule(rule);
        return null;
    }
}
