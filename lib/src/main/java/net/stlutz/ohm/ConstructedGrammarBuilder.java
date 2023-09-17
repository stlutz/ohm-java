package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for {@link ConstructedGrammar}s.
 */
public class ConstructedGrammarBuilder {
    final Namespace namespace;
    List<GrammarDefinition> grammars = new ArrayList<>();
    
    public ConstructedGrammarBuilder() {
        this(new Namespace());
    }
    
    public ConstructedGrammarBuilder(Namespace namespace) {
        this.namespace = namespace != null ? namespace : new Namespace();
    }
    
    public Namespace getNamespace() {
        return namespace;
    }
    
    public void clear() {
        grammars.clear();
    }
    
    public GrammarDefinition newGrammar(String grammarName) {
        GrammarDefinition grammar = new GrammarDefinition(grammarName);
        grammars.add(grammar);
        return grammar;
    }
    
    public ConstructedGrammar buildGrammar() {
        if (grammars.isEmpty()) {
            throw new OhmException("No grammars defined.");
        } else if (grammars.size() > 1) {
            throw new OhmException("More than one grammar defined.");
        } else {
            return buildGrammars().get(0);
        }
    }
    
    public List<ConstructedGrammar> buildGrammars() {
        Deque<GrammarDefinition> grammarsToBuild = new ArrayDeque<>(grammars);
        List<ConstructedGrammar> builtGrammars = new ArrayList<>();
        
        boolean isMakingProgress = true;
        while (isMakingProgress) {
            isMakingProgress = false;
            for (int i = 0; i < grammarsToBuild.size(); i++) {
                GrammarDefinition gDef = grammarsToBuild.removeFirst();
                if (gDef.name == null) {
                    throw new OhmException("Grammar name must not be null.");
                }
                if (namespace.containsGrammarNamed(gDef.name)) {
                    builtGrammars.forEach(grammar -> {
                        if (grammar.getName().equals(gDef.name)) {
                            throw new OhmException(
                                "The grammar '%s' was declared multiple times.".formatted(gDef.name));
                        }
                    });
                    if (namespace.getGrammarNamed(gDef.name).isBuiltIn()) {
                        // TODO: this case cannot be reached
                        throw new OhmException(
                            "The grammar '%s' is built-in and cannot be overridden.".formatted(gDef.name));
                    } else {
                        throw new OhmException(
                            "The grammar '%s' was already declared in the provided namespace."
                                .formatted(gDef.name));
                    }
                }
                if (gDef.superGrammarName == null || namespace.containsGrammarNamed(gDef.superGrammarName)) {
                    ConstructedGrammar grammar = buildGrammar(gDef);
                    builtGrammars.add(grammar);
                    namespace.add(grammar);
                    isMakingProgress = true;
                } else {
                    grammarsToBuild.addLast(gDef);
                }
            }
            if (grammarsToBuild.isEmpty()) {
                return builtGrammars;
            }
        }
        // TODO: differentiate between the two and give concrete offending grammars
        throw new OhmException(
            "Either the specified super grammar was not found in the given namespace or grammar inheritance is circular.");
    }
    
    private ConstructedGrammar buildGrammar(GrammarDefinition def) {
        Grammar superGrammar = namespace.getGrammarNamed(def.superGrammarName);
        if (superGrammar == null && !def.isBuiltIn) {
            // TODO: should we allow overriding the BuiltInRules entirely?
            superGrammar = ConstructedGrammar.BuiltInRules;
        }
        Map<String, ConstructedRule> rules = buildRules(def, superGrammar);
        String defaultRuleName = getDefaultStartRuleName(def, superGrammar);
        return new ConstructedGrammar(def.name, superGrammar, rules, defaultRuleName, def.isBuiltIn);
    }
    
    private String getDefaultStartRuleName(GrammarDefinition def, Grammar superGrammar) {
        if (def.defaultStartRuleName != null) {
            return def.defaultStartRuleName;
        }
        
        if (superGrammar != null && !superGrammar.isBuiltIn() && superGrammar.getDefaultStartRule() != null) {
            return superGrammar.getDefaultStartRule();
        }
        
        if (!def.rules.isEmpty()) {
            return def.rules.get(0).name;
        }
        
        return null;
    }
    
    private Map<String, ConstructedRule> buildRules(GrammarDefinition gDef, Grammar superGrammar) {
        Map<String, ConstructedRule> rules = new HashMap<>();
        
        for (RuleDefinition rDef : gDef.rules) {
            if (rDef.name == null) {
                throw new OhmException("Rule name must not be null.");
            }
            if (isForbiddenRuleName(rDef.name)) {
                throw new OhmException("Rule name '%s' is forbidden".formatted(rDef.name));
            }
            if (rules.containsKey(rDef.name)) {
                throw new OhmException("Duplicate declaration for rule '%s'".formatted(rDef.name));
            }
            ConstructedRule rule = buildRule(rDef, superGrammar);
            rules.put(rDef.name, rule);
        }
        
        if (superGrammar != null) {
            superGrammar.getRules().forEach((name, rule) -> {
                rules.putIfAbsent(name, ConstructedRule.copyOf(rule));
            });
        }
        
        RulesFinalizer.finalize(rules);
        return rules;
    }
    
    private ConstructedRule buildRule(RuleDefinition def, Grammar superGrammar) {
        PExpr body = def.body;
        if (body == null) {
            throw new OhmException("Rule body must not be null.");
        }
        String name = def.name;
        if (name == null) {
            throw new OhmException("Rule name must not be null.");
        }
        String description = def.description;
        
        Collection<String> duplicateParameterNames = Util.getDuplicates(def.formals);
        if (!duplicateParameterNames.isEmpty()) {
            throw new OhmException("Duplicate parameter names in rule '%s': %s".formatted(name,
                String.join(", ", duplicateParameterNames)));
        }
        
        Rule superRule = superGrammar != null ? superGrammar.getRule(name) : null;
        
        if (!def.isDefinition()) {
            if (superRule == null) {
                throw new OhmException(
                    "Cannot %s rule '%s'. No rule of the same name was found in a super grammar."
                        .formatted(def.isOverride() ? "override" : "extend", name));
            }
            List<String> superRuleFormals = superRule.getFormals();
            if (superRuleFormals.size() != def.formals.size()) {
                throw new OhmException(
                    "Cannot %s rule '%s': Got %d parameters, but super rule has %d parameters."
                        .formatted(def.isOverride() ? "override" : "extend", name, def.formals.size(), superRuleFormals.size()));
            }
            
            if (def.isExtension()) {
                body = PExpr.extend(superRule.getBody(), body);
            } else { // isOverride() implied
                body.resolveSplice(superRule.getBody());
            }
            
            if (description != null) {
                description = superRule.getDescription();
            }
        } else {
            if (superRule != null) {
                throw new OhmException(
                    "Rule '%s' was already declared in super grammar and must be explicitly overridden."
                        .formatted(name));
            }
        }
        
        return new ConstructedRule(name, body, def.formals, description, def.sourceInterval, def.operation);
    }
    
    private boolean isForbiddenRuleName(String ruleName) {
        return Semantics.SpecialActionNames.includes(ruleName);
    }
}
