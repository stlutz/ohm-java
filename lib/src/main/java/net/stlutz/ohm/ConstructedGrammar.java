package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Apply;
import net.stlutz.ohm.pexprs.CaseInsensitiveTerminal;
import net.stlutz.ohm.pexprs.Param;

import java.util.Map;

import static net.stlutz.ohm.pexprs.PExpr.alt;
import static net.stlutz.ohm.pexprs.PExpr.any;
import static net.stlutz.ohm.pexprs.PExpr.apply;
import static net.stlutz.ohm.pexprs.PExpr.end;
import static net.stlutz.ohm.pexprs.PExpr.none;
import static net.stlutz.ohm.pexprs.PExpr.param;
import static net.stlutz.ohm.pexprs.PExpr.range;
import static net.stlutz.ohm.pexprs.PExpr.seq;
import static net.stlutz.ohm.pexprs.PExpr.star;
import static net.stlutz.ohm.pexprs.PExpr.unicodeChar;

public class ConstructedGrammar extends AbstractGrammar {
    static final Grammar BuiltInRules = buildBuiltInRules();
    
    ConstructedGrammar(String name, Grammar superGrammar, Map<String, ConstructedRule> rules, String defaultStartRule,
                       boolean isBuiltIn) {
        super(name, superGrammar, rules, defaultStartRule, isBuiltIn);
    }
    
    static ConstructedGrammar buildBuiltInRules() {
        ConstructedGrammarBuilder builder = new ConstructedGrammarBuilder();
        GrammarDefinition grammar = builder.newGrammar("BuiltInRules").builtIn();
        // proto built-in
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
        // built-in
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
        return builder.buildGrammar();
    }
    
    @Override
    public Matcher getMatcher(String input) {
        return new Matcher(this, input);
    }
    
    @Override
    public MatchResult match(String input, String startRule) {
        return getMatcher(input).match(startRule);
    }
    
    @Override
    public <T extends Semantics> SemanticsBlueprint<T> createSemanticsBlueprint(
        Class<T> semanticsClass) {
        return SemanticsBlueprint.create(semanticsClass, this);
    }
    
    @Override
    public Apply parseApplication(String ruleName) {
        if (ruleName.contains("<")) {
            // TODO
            throw new OhmException("parameterized applications are not implemented yet");
        }
        
        if (!hasRule(ruleName)) {
            throw new OhmException("'%s' is not a rule in grammar '%s'".formatted(ruleName, name));
        }
        
        return new Apply(ruleName);
    }
}
