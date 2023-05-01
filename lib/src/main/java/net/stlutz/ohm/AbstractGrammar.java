package net.stlutz.ohm;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractGrammar implements Grammar {
    final String name;
    final Grammar superGrammar;
    final boolean isBuiltIn;
    String defaultStartRule;
    final Map<String, RuleImpl> rules;

    public AbstractGrammar(String name, Grammar superGrammar, Map<String, RuleImpl> rules, String defaultStartRule, boolean isBuiltIn) {
        super();
        this.name = name;
        this.superGrammar = superGrammar;
        this.rules = rules;
        this.defaultStartRule = defaultStartRule;
        this.isBuiltIn = isBuiltIn;
    }

    @Override
    public String getDefaultStartRule() {
        return defaultStartRule;
    }

    @Override
    public void setDefaultStartRule(String defaultStartRule) {
        if (defaultStartRule == null) {
            throw new IllegalArgumentException("Cannot set the default start rule to null");
        } else if (!hasRule(defaultStartRule)) {
            throw new OhmException("Invalid default start rule: '%s' is not a rule in grammar '%s'"
                    .formatted(defaultStartRule, name));
        }
        this.defaultStartRule = defaultStartRule;
    }

    @Override
    public MatchResult match(String input) {
        if (defaultStartRule == null) {
            throw new OhmException("Grammar '%s' has no default start rule.".formatted(getName()));
        }

        return match(input, defaultStartRule);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Grammar getSuperGrammar() {
        return superGrammar;
    }

    @Override
    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    @Override
    public RuleImpl getRule(String ruleName) {
        return rules.get(ruleName);
    }

    @Override
    public boolean hasRule(String ruleName) {
        return rules.containsKey(ruleName);
    }

    @Override
    public Map<String, Rule> getRules() {
        return Collections.unmodifiableMap(rules);
    }
}
