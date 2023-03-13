package net.stlutz.ohm;

import java.util.*;
import net.stlutz.ohm.pexprs.PExpr;

public class GrammarBuilder {
  final Namespace namespace;
  List<GrammarDefinition> grammars = new ArrayList<>();

  public GrammarBuilder() {
    super();
    this.namespace = Namespace.create();
  }

  public GrammarBuilder(Namespace namespace) {
    super();
    this.namespace = namespace != null ? namespace : Namespace.create();
  }

  public Namespace getNamespace() {
    return namespace;
  }

  public GrammarDefinition newGrammar(String grammarName) {
    GrammarDefinition grammar = new GrammarDefinition(grammarName);
    grammars.add(grammar);
    return grammar;
  }

  public Grammar buildGrammar() {
    if (grammars.isEmpty()) {
      throw new OhmException("No grammars defined.");
    } else if (grammars.size() > 1) {
      throw new OhmException("More than one grammar defined.");
    } else {
      return buildGrammars().get(0);
    }
  }

  public List<Grammar> buildGrammars() {
    Deque<GrammarDefinition> grammarsToBuild = new ArrayDeque<>(grammars);
    List<Grammar> builtGrammars = new ArrayList<>();

    boolean isMakingProgress = true;
    while (isMakingProgress) {
      isMakingProgress = false;
      for (int i = 0; i < grammarsToBuild.size(); i++) {
        GrammarDefinition gDef = grammarsToBuild.removeFirst();
        if (namespace.has(gDef.name)) {
          builtGrammars.forEach(grammar -> {
            if (grammar.getName().equals(gDef.name)) {
              throw new OhmException(
                  "The grammar '%s' was declared multiple times.".formatted(gDef.name));
            }
          });
          if (namespace.get(gDef.name).isBuiltIn()) {
            throw new OhmException(
                "The grammar '%s' is built-in and cannot be overridden.".formatted(gDef.name));
          } else {
            throw new OhmException(
                "The grammar '%s' was already declared in the provided namespace."
                    .formatted(gDef.name));
          }
        }
        if (gDef.superGrammarName == null || namespace.has(gDef.superGrammarName)) {
          Grammar grammar = buildGrammar(gDef);
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

  private Grammar buildGrammar(GrammarDefinition def) {
    Grammar superGrammar = namespace.get(def.superGrammarName);
    if (superGrammar == null && !def.isBuiltIn) {
      superGrammar = namespace.get("BuiltInRules");
    }
    Map<String, Rule> rules = buildRules(def, superGrammar);
    String defaultRuleName = getDefaultStartRuleName(def, superGrammar);
    return new Grammar(def.name, superGrammar, rules, defaultRuleName, def.isBuiltIn);
  }

  private String getDefaultStartRuleName(GrammarDefinition def, Grammar superGrammar) {
    if (def.defaultStartRuleName != null) {
      return def.defaultStartRuleName;
    } else if (superGrammar != null && !superGrammar.isBuiltIn()) {
      return superGrammar.defaultStartRule;
    } else if (!def.rules.isEmpty()) {
      return def.rules.get(0).name;
    } else {
      return null;
    }
  }

  private Map<String, Rule> buildRules(GrammarDefinition gDef, Grammar superGrammar) {
    Map<String, Rule> rules = new HashMap<>();

    for (RuleDefinition rDef : gDef.rules) {
      if (rules.containsKey(rDef.name)) {
        throw new OhmException("Duplicate declaration for rule '%s'".formatted(rDef.name));
      }
      Rule rule = buildRule(rDef, superGrammar);
      rules.put(rDef.name, rule);
    }

    if (superGrammar != null) {
      superGrammar.rules.forEach((name, rule) -> {
        rules.putIfAbsent(name, Rule.copyOf(rule));
      });
    }

    return rules;
  }

  private Rule buildRule(RuleDefinition def, Grammar superGrammar) {
    PExpr body = def.body;

    Collection<String> duplicateParameterNames = Util.getDuplicates(def.formals);
    if (!duplicateParameterNames.isEmpty()) {
      throw new OhmException("Duplicate parameter names in rule '%s': %s".formatted(def.name,
          String.join(", ", duplicateParameterNames)));
    }

    Rule superRule = superGrammar != null ? superGrammar.getRule(def.name) : null;

    if (!def.isDefinition()) {
      // TODO: insert override / extend in err msg depending on operation enum
      if (superRule == null) {
        if (superGrammar == null) {
          throw new OhmException("Cannot %s rule '%s'. No super grammar was specified."
              .formatted("override", def.name));
        } else {
          throw new OhmException(
              "Cannot %s rule '%s'. No rule of the same name was found in a super grammar."
                  .formatted("override", def.name));
        }
      }
      String[] superRuleFormals = superRule.getFormals();
      if (superRuleFormals.length != def.formals.length) {
        throw new OhmException(
            "Cannot %s rule '%s': Got %d parameters, but super rule has %d parameters."
                .formatted("override", def.formals.length, superRuleFormals.length));
      }

      if (def.isExtension()) {
        body = PExpr.extend(superRule.getBody(), body);
      } else { // isOverride() implied
        body.resolveSplice(superRule.getBody());
      }
    } else {
      if (superRule != null) {
        throw new OhmException(
            "Rule '%s' was already declared in super grammar and must be explicitly overridden."
                .formatted(def.name));
      }
    }

    body.introduceParams(def.formals);
    return new Rule(body, def.formals, def.description, def.sourceInterval, def.operation);
  }
}
