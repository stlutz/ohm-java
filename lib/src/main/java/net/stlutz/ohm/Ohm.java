package net.stlutz.ohm;

import java.util.Collections;
import java.util.List;

import static net.stlutz.ohm.pexprs.PExpr.alt;
import static net.stlutz.ohm.pexprs.PExpr.any;
import static net.stlutz.ohm.pexprs.PExpr.apply;
import static net.stlutz.ohm.pexprs.PExpr.end;
import static net.stlutz.ohm.pexprs.PExpr.lookahead;
import static net.stlutz.ohm.pexprs.PExpr.not;
import static net.stlutz.ohm.pexprs.PExpr.opt;
import static net.stlutz.ohm.pexprs.PExpr.range;
import static net.stlutz.ohm.pexprs.PExpr.seq;
import static net.stlutz.ohm.pexprs.PExpr.star;
import static net.stlutz.ohm.pexprs.PExpr.terminal;

public final class Ohm {
    static final Grammar OhmGrammar = buildOhmGrammar();
    static final BuildGrammar buildGrammarSemantics =
        OhmGrammar.createSemanticsBlueprint(BuildGrammar.class).instantiate();
    
    private Ohm() {} // do not instantiate
    
    public static Grammar grammar(String source) {
        // TODO: assert
        return grammars(source, null).get(0);
    }
    
    public static List<Grammar> grammars(String source) {
        return grammars(source, null);
    }
    
    public static List<Grammar> grammars(String source, Namespace namespace) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        var matchResult = OhmGrammar.match(source);
        if (matchResult.failed()) {
            // TODO: actual syntax error
            throw new OhmException("Syntax error in grammar source");
        }
        
        return Collections.unmodifiableList(buildGrammarSemantics.buildGrammars(matchResult.getRootNode(), namespace));
    }
    
    static Grammar buildOhmGrammar() {
        ConstructedGrammarBuilder builder = new ConstructedGrammarBuilder();
        GrammarDefinition grammar = builder.newGrammar("Ohm");
        grammar.defaultStartRule("Grammars");
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
}
