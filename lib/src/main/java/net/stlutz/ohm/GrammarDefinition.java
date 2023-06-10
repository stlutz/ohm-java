package net.stlutz.ohm;

import java.util.ArrayList;
import java.util.List;

class GrammarDefinition {
    String name;
    SourceInterval sourceInterval;
    String superGrammarName;
    String defaultStartRuleName;
    boolean isBuiltIn = false;
    List<RuleDefinition> rules = new ArrayList<>();
    
    GrammarDefinition(String name) {
        super();
        this.name = name;
    }
    
    public GrammarDefinition sourceInterval(SourceInterval sourceInterval) {
        this.sourceInterval = sourceInterval;
        return this;
    }
    
    public GrammarDefinition name(String name) {
        this.name = name;
        return this;
    }
    
    public GrammarDefinition extend(String superGrammarName) {
        this.superGrammarName = superGrammarName;
        return this;
    }
    
    public GrammarDefinition defaultStartRule(String ruleName) {
        this.defaultStartRuleName = ruleName;
        return this;
    }
    
    GrammarDefinition builtIn() {
        isBuiltIn = true;
        return this;
    }
    
    public RuleDefinition newRule(String ruleName) {
        RuleDefinition rule = new RuleDefinition(ruleName);
        rules.add(rule);
        return rule;
    }
}
