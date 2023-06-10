package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Alt;
import net.stlutz.ohm.pexprs.Apply;
import net.stlutz.ohm.pexprs.Lex;
import net.stlutz.ohm.pexprs.Lookahead;
import net.stlutz.ohm.pexprs.Not;
import net.stlutz.ohm.pexprs.Opt;
import net.stlutz.ohm.pexprs.PExpr;
import net.stlutz.ohm.pexprs.Plus;
import net.stlutz.ohm.pexprs.Range;
import net.stlutz.ohm.pexprs.Seq;
import net.stlutz.ohm.pexprs.Star;
import net.stlutz.ohm.pexprs.Terminal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestGrammarBuilding {
    
    private PExpr buildExpr(String startRule, String source) {
        var matchResult = ConstructedGrammar.OhmGrammar.match(source, startRule);
        assertTrue(matchResult.succeeded());
        return Ohm.buildGrammarSemantics.buildPExpr(matchResult.getRootNode());
    }
    
    <T extends PExpr> T buildAndValidateExpr(Class<T> exprClass, String ruleName, String source) {
        var expr = buildExpr(ruleName, source);
        assertEquals(source, expr.getSource().getContents());
        assertInstanceOf(exprClass, expr);
        return exprClass.cast(expr);
    }
    
    @Test
    void testBuildTerminal() {
        var expr = buildAndValidateExpr(Terminal.class, "Base_terminal", "\"hello world\"");
        assertEquals("hello world", expr.getString());
        expr = buildAndValidateExpr(Terminal.class, "Base_terminal",
                "\"\\\\\\\"\\'\\b\\n\\r\\t\\u{61}\\u0062\\x63\"");
        assertEquals("\\\"'\b\n\r\tabc", expr.getString());
    }
    
    @Test
    void testBuildRange() {
        var expr = buildAndValidateExpr(Range.class, "Base_range", "\"a\"..\"z\"");
        assertEquals("a".codePointAt(0), expr.from);
        assertEquals("z".codePointAt(0), expr.to);
    }
    
    @Test
    void testBuildApplication() {
        var expr = buildAndValidateExpr(Apply.class, "Base_application", "ruleA");
        assertEquals("ruleA", expr.getRuleName());
    }
    
    @Test
    void testBuildLex() {
        buildAndValidateExpr(Lex.class, "Lex", "#\"hello world\"");
    }
    
    @Test
    void testBuildLookahead() {
        buildAndValidateExpr(Lookahead.class, "Pred_lookahead", "&\"hello world\"");
    }
    
    @Test
    void testBuildNot() {
        buildAndValidateExpr(Not.class, "Pred_not", "~\"hello world\"");
    }
    
    @Test
    void testBuildOpt() {
        buildAndValidateExpr(Opt.class, "Iter_opt", "\"hello world\"?");
    }
    
    @Test
    void testBuildPlus() {
        buildAndValidateExpr(Plus.class, "Iter_plus", "\"hello world\"+");
    }
    
    @Test
    void testBuildStar() {
        buildAndValidateExpr(Star.class, "Iter_star", "\"hello world\"*");
    }
    
    @Test
    void testBuildSeq() {
        var expr = buildAndValidateExpr(Seq.class, "Seq", "\"hello\" \"world\"");
        assertEquals("\"hello\"", expr.getTerms()[0].getSource().getContents());
        assertEquals("\"world\"", expr.getTerms()[1].getSource().getContents());
    }
    
    @Test
    void testBuildAlt() {
        var expr = buildAndValidateExpr(Alt.class, "Alt", "\"hello\" | \"world\"");
        assertEquals("\"hello\"", expr.getTerms()[0].getSource().getContents());
        assertEquals("\"world\"", expr.getTerms()[1].getSource().getContents());
    }
    
    @Test
    void testBuildApplicationWithParams() {
        buildExpr("Base_application", "ruleA<ruleB>");
    }
}
