package net.stlutz.ohm;

import static net.stlutz.ohm.pexprs.PExpr.*;
import java.util.Map;
import net.stlutz.ohm.pexprs.*;

public class Grammar {
  static final Namespace DefaultNamespace = buildDefaultNamespace();
  static final Grammar ProtoBuiltInRules = DefaultNamespace.get("ProtoBuiltInRules");
  static final Grammar BuiltInRules = DefaultNamespace.get("BuiltInRules");
  static final Grammar OhmGrammar = buildOhmGrammar();

  final String name;
  final Grammar superGrammar;
  final Map<String, Rule> rules;
  String defaultStartRule;
  final boolean isBuiltIn;

  Grammar(String name, Grammar superGrammar, Map<String, Rule> rules, String defaultStartRule,
      boolean isBuiltIn) {
    super();
    this.name = name;
    this.superGrammar = superGrammar;
    this.rules = rules;
    this.defaultStartRule = defaultStartRule;
    this.isBuiltIn = isBuiltIn;
  }

  public String getDefaultStartRule() {
    return defaultStartRule;
  }

  public void setDefaultStartRule(String defaultStartRule) {
    if (defaultStartRule == null) {
      throw new OhmException("Cannot set the default start rule to null");
    } else if (!hasRule(defaultStartRule)) {
      throw new OhmException("Invalid default start rule: '%s' is not a rule in grammar '%s'"
          .formatted(defaultStartRule, name));
    }
    this.defaultStartRule = defaultStartRule;
  }

  public String getName() {
    return name;
  }

  public Grammar getSuperGrammar() {
    return superGrammar;
  }

  static Namespace buildDefaultNamespace() {
    GrammarBuilder builder = new GrammarBuilder(Namespace.empty());
    GrammarDefinition grammar = builder.newGrammar("ProtoBuiltInRules");
    grammar.newRule("any").description("any character").body(any());
    grammar.newRule("end").description("end of input").body(end());
    grammar.newRule("caseInsensitive").formals("str")
        .body(new CaseInsensitiveTerminal(new Param(0)));
    grammar.newRule("lower").description("a lowercase letter").body(unicodeChar("Ll"));
    grammar.newRule("upper").description("an uppercase letter").body(unicodeChar("Lu"));
    grammar.newRule("unicodeLtmo").description("a Unicode character in Lt, Lm, or Lo")
        .body(unicodeChar("Ltmo"));
    grammar.newRule("spaces").description("zero or more spaces").body(star(apply("space")));
    grammar.newRule("space").description("a space").body(range(0x00, " ".codePointAt(0)));

    grammar = builder.newGrammar("BuiltInRules").extend("ProtoBuiltInRules");
    grammar.newRule("alnum").description("an alpha-numeric character")
        .body(alt(apply("letter"), apply("digit")));
    grammar.newRule("letter").description("a letter")
        .body(alt(apply("lower"), apply("upper"), apply("unicodeLtmo")));
    grammar.newRule("digit").description("a digit").body(range("0", "9"));
    grammar.newRule("hexDigit").description("a hexadecimal digit")
        .body(alt(apply("digit"), range("a", "f"), range("A", "F")));
    // TODO: maybe have func param(String name) -> apply(name)
    grammar.newRule("ListOf").formals("elem", "sep").body(
        alt(apply("NonemptyListOf", param(0), param(1)), apply("EmptyListOf", param(0), param(1))));
    grammar.newRule("NonemptyListOf").formals("elem", "sep")
        .body(seq(param(0), star(seq(param(1), param(0)))));
    grammar.newRule("EmptyListOf").formals("elem", "sep").body(none());
    grammar.newRule("listOf").formals("elem", "sep").body(
        alt(apply("nonemptyListOf", param(0), param(1)), apply("emptyListOf", param(0), param(1))));
    grammar.newRule("nonemptyListOf").formals("elem", "sep")
        .body(seq(param(0), star(seq(param(1), param(0)))));
    grammar.newRule("emptyListOf").formals("elem", "sep").body(none());
    grammar.newRule("applySyntactic").formals("app").body(param(0));
    builder.buildGrammars();
    return builder.getNamespace();
  }

  static Grammar buildOhmGrammar() {
    GrammarBuilder builder = new GrammarBuilder();
    GrammarDefinition grammar = builder.newGrammar("Ohm");
    grammar.newRule("Grammars").body(star(apply("Grammar")));
    grammar.newRule("Grammar").body(seq(apply("ident"), opt(apply("SuperGrammar")), terminal("{"),
        star(apply("Rule")), terminal("}")));
    grammar.newRule("SuperGrammar").body(seq(terminal("<:"), apply("ident")));
    // TODO: handle inline rule declarations explicitly?
    grammar.newRule("Rule")
        .body(alt(apply("Rule_define"), apply("Rule_override"), apply("Rule_extend")));
    grammar.newRule("Rule_define").body(seq(apply("ident"), opt(apply("Formals")),
        opt(apply("ruleDescr")), terminal("="), apply("RuleBody")));
    grammar.newRule("Rule_override").body(
        seq(apply("ident"), opt(apply("Formals")), terminal(":="), apply("OverrideRuleBody")));
    grammar.newRule("Rule_extend")
        .body(seq(apply("ident"), opt(apply("Formals")), terminal("+="), apply("RuleBody")));
    grammar.newRule("RuleBody").body(
        seq(opt(terminal("|")), apply("NonemptyListOf", apply("TopLevelTerm"), terminal("|"))));
    grammar.newRule("TopLevelTerm").body(alt(apply("TopLevelTerm_inline"), apply("Seq")));
    grammar.newRule("TopLevelTerm_inline").body(seq(apply("Seq"), apply("caseName")));
    grammar.newRule("OverrideRuleBody").body(seq(opt(terminal("|")),
        apply("NonemptyListOf", apply("OverrideTopLevelTerm"), terminal("|"))));
    grammar.newRule("OverrideTopLevelTerm")
        .body(alt(apply("OverrideTopLevelTerm_superSplice"), apply("TopLevelTerm")));
    grammar.newRule("OverrideTopLevelTerm_superSplice").body(terminal("..."));
    grammar.newRule("Formals")
        .body(seq(terminal("<"), apply("ListOf", apply("ident"), terminal(",")), terminal(">")));
    grammar.newRule("Params")
        .body(seq(terminal("<"), apply("ListOf", apply("Seq"), terminal(",")), terminal(">")));
    grammar.newRule("Alt").body(apply("NonemptyListOf", apply("Seq"), terminal("|")));
    grammar.newRule("Seq").body(star(apply("Iter")));
    grammar.newRule("Iter")
        .body(alt(apply("Iter_star"), apply("Iter_plus"), apply("Iter_opt"), apply("Pred")));
    grammar.newRule("Iter_star").body(seq(apply("Pred"), terminal("*")));
    grammar.newRule("Iter_plus").body(seq(apply("Pred"), terminal("+")));
    grammar.newRule("Iter_opt").body(seq(apply("Pred"), terminal("?")));
    grammar.newRule("Pred").body(alt(apply("Pred_not"), apply("Pred_lookahead"), apply("Lex")));
    grammar.newRule("Pred_not").body(seq(terminal("~"), apply("Lex")));
    grammar.newRule("Pred_lookahead").body(seq(terminal("&"), apply("Lex")));
    grammar.newRule("Lex").body(alt(apply("Lex_lex"), apply("Base")));
    grammar.newRule("Lex_lex").body(seq(terminal("#"), apply("Base")));
    grammar.newRule("Base").body(alt(apply("Base_application"), apply("Base_range"),
        apply("Base_terminal"), apply("Base_paren")));
    grammar.newRule("Base_application").body(seq(apply("ident"), opt(apply("Params")),
        not(alt(seq(opt(apply("ruleDescr")), terminal("=")), terminal(":="), terminal("+=")))));
    grammar.newRule("Base_range")
        .body(seq(apply("oneCharTerminal"), terminal(".."), apply("oneCharTerminal")));
    grammar.newRule("Base_terminal").body(apply("terminal"));
    grammar.newRule("Base_paren").body(seq(terminal("("), apply("Alt"), terminal(")")));
    grammar.newRule("ruleDescr").description("a rule description")
        .body(seq(terminal("("), apply("ruleDescrText"), terminal(")")));
    grammar.newRule("ruleDescrText").body(star(seq(not(terminal(")")), any())));
    grammar.newRule("caseName")
        .body(seq(terminal("--"), star(seq(not(terminal("\n")), apply("space"))), apply("name"),
            star(seq(not(terminal("\n")), apply("space"))),
            alt(terminal("\n"), lookahead(terminal("}")))));
    grammar.newRule("name").description("a name")
        .body(seq(apply("nameFirst"), star(apply("nameRest"))));
    grammar.newRule("nameFirst").body(alt(terminal("_"), apply("letter")));
    grammar.newRule("nameRest").body(alt(terminal("_"), apply("alnum")));
    grammar.newRule("ident").description("an identifier").body(apply("name"));
    grammar.newRule("terminal")
        .body(seq(terminal("\""), star(apply("terminalChar")), terminal("\"")));
    grammar.newRule("oneCharTerminal")
        .body(seq(terminal("\""), apply("terminalChar"), terminal("\"")));
    grammar.newRule("terminalChar").body(alt(apply("escapeChar"),
        seq(not(terminal("\\")), not(terminal("\"")), not(terminal("\n")), range(0x0, 0x10ffff))));
    grammar.newRule("escapeChar").description("an escape sequence")
        .body(alt(apply("escapeChar_backslash"), apply("escapeChar_doubleQuote"),
            apply("escapeChar_singleQuote"), apply("escapeChar_backspace"),
            apply("escapeChar_lineFeed"), apply("escapeChar_carriageReturn"),
            apply("escapeChar_tab"), apply("escapeChar_unicodeCodePoint"),
            apply("escapeChar_unicodeEscape"), apply("escapeChar_hexEscape")));
    grammar.newRule("escapeChar_backslash").body(terminal("\\\\"));
    grammar.newRule("escapeChar_doubleQuote").body(terminal("\\\""));
    grammar.newRule("escapeChar_singleQuote").body(terminal("\\\'"));
    grammar.newRule("escapeChar_backspace").body(terminal("\\b"));
    grammar.newRule("escapeChar_lineFeed").body(terminal("\\n"));
    grammar.newRule("escapeChar_carriageReturn").body(terminal("\\r"));
    grammar.newRule("escapeChar_tab").body(terminal("\\t"));
    grammar.newRule("escapeChar_unicodeCodePoint")
        .body(seq(terminal("\\u{"), apply("hexDigit"), opt(apply("hexDigit")),
            opt(apply("hexDigit")), opt(apply("hexDigit")), opt(apply("hexDigit")),
            opt(apply("hexDigit")), terminal("}")));
    grammar.newRule("escapeChar_unicodeEscape").body(seq(terminal("\\u"), apply("hexDigit"),
        apply("hexDigit"), apply("hexDigit"), apply("hexDigit")));
    grammar.newRule("escapeChar_hexEscape")
        .body(seq(terminal("\\x"), apply("hexDigit"), apply("hexDigit")));
    grammar.newRule("space").extend().body(apply("comment"));
    grammar.newRule("comment").body(alt(apply("comment_singleLine"), apply("comment_multiLine")));
    grammar.newRule("comment_singleLine").body(seq(terminal("//"),
        star(seq(not(terminal("\n")), any())), lookahead(alt(terminal("\n"), end()))));
    grammar.newRule("comment_multiLine")
        .body(seq(terminal("/*"), star(seq(not(terminal("*/")), any())), terminal("*/")));
    grammar.newRule("tokens").body(star(apply("token")));
    grammar.newRule("token").body(alt(apply("caseName"), apply("comment"), apply("ident"),
        apply("operator"), apply("punctuation"), apply("terminal"), apply("any")));
    grammar.newRule("operator").body(alt(terminal("<:"), terminal("="), terminal(":="),
        terminal("+="), terminal("*"), terminal("+"), terminal("?"), terminal("~"), terminal("&")));
    grammar.newRule("punctuation")
        .body(alt(terminal("<"), terminal(">"), terminal(","), terminal("--")));

    return builder.buildGrammar();
  }

  public static Grammar parse(String grammar) {
    // TODO
    return null;
  }

  public Rule getRule(String ruleName) {
    return rules.get(ruleName);
  }

  public boolean hasRule(String ruleName) {
    return rules.containsKey(ruleName);
  }

  public Matcher getMatcher(String input) {
    return new Matcher(this, input);
  }

  public MatchResult match(String input) {
    if (defaultStartRule == null) {
      throw new OhmException("Grammar has no default start rule.");
    }

    return match(input, defaultStartRule);
  }

  public MatchResult match(String input, String startRule) {
    return getMatcher(input).match(startRule);
  }

  public <T extends Semantics> SemanticsBlueprint<T> createSemanticsBlueprint(
      Class<T> semanticsClass) {
    return SemanticsBlueprint.create(semanticsClass, this);
  }

  Apply parseApplication(String ruleName) {
    if (ruleName.contains("<")) {
      // TODO
      throw new OhmException("parameterized applications are not implemented yet");
    }

    if (!hasRule(ruleName)) {
      throw new OhmException("'%s' is not a rule in grammar '%s'".formatted(ruleName, name));
    }

    return new Apply(ruleName);
  }

  public boolean isBuiltIn() {
    return isBuiltIn;
  }
}
