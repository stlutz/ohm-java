package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.Grammar;
import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;
import net.stlutz.ohm.Matcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicEvalTest {
    private MatchState matchState;
    
    private static MatchState getMatchState(String input, Apply startExpr) {
        Grammar grammar = null;
        Matcher matcher = new Matcher(grammar, input);
        
        return new MatchState(matcher, startExpr);
    }
    
    private MatchState newMatchState(String input, int offset) {
        matchState = getMatchState(input, new Apply("rule"));
        matchState.getInputStream().setPosition(offset);
        return matchState;
    }
    
    void checkEval(String input, int offset, PExpr expr, int matchLength) {
        newMatchState(input, offset);
        InputStream inputStream = matchState.getInputStream();
        int arity = expr.getArity();
        int numBindingsBefore = matchState.numBindings();
        boolean succeeded = expr.eval(matchState);
        int newNumBindings = matchState.numBindings() - numBindingsBefore;
        
        assertEquals(matchLength >= 0, succeeded);
        if (succeeded) {
            assertEquals(arity, newNumBindings);
            assertEquals(matchLength + offset, inputStream.getPosition());
        }
    }
    
    @Test
    void testTerminalEval() {
        checkEval("Hello", 0, new Terminal("Hello"), 5);
        checkEval(" Hello", 0, new Terminal("Hello"), -1);
        checkEval(" Hello", 1, new Terminal("Hello"), 5);
        checkEval("hello", 0, new Terminal("Hello"), -1);
    }
    
    @Test
    void testCaseInsensitiveTerminalEval() {
        checkEval("Hello", 0, new CaseInsensitiveTerminal(new Terminal("Hello")), 5);
        checkEval(" Hello", 0, new CaseInsensitiveTerminal(new Terminal("Hello")), -1);
        checkEval(" heLlO", 1, new CaseInsensitiveTerminal(new Terminal("Hello")), 5);
    }
    
    @Test
    void testRangeEval() {
        checkEval("Hello", 0, new Range("o", "o"), -1);
        checkEval("Hello", 4, new Range("o", "o"), 1);
        for (int i = 0; i < 5; i++) {
            checkEval("Hello", i, new Range("A", "z"), 1);
        }
        checkEval("👍", 0, new Range("👍", "👍"), 2);
        checkEval(" 👍", 0, new Range("👍", "👍"), -1);
    }
    
    @Test
    void testAnyEval() {
        for (int i = 0; i < 5; i++) {
            checkEval("Hello", i, Any.getInstance(), 1);
        }
        // TODO: Should Any consume a whole codepoint?
        checkEval("👍", 0, Any.getInstance(), 1);
    }
    
    @Test
    void testEndEval() {
        for (int i = 0; i < 5; i++) {
            checkEval("Hello", i, End.getInstance(), -1);
        }
        checkEval("Hello", 6, End.getInstance(), 0);
    }
    
    @Test
    void testUnicodeCharEval() {
        for (int i = 0; i < 5; i++) {
            checkEval("Hello", i, new UnicodeChar("L"), 1);
        }
        checkEval("Hello", 0, new UnicodeChar("Ll"), -1);
        checkEval("Hello", 1, new UnicodeChar("Lu"), -1);
    }
    
    @Test
    void testPlusEval() {
        checkEval("Hello World", 0, new Plus(new Range("A", "z")), 5);
        checkEval("Hello World", 4, new Plus(new Range("A", "z")), 1);
        checkEval("Hello World", 5, new Plus(new Range("A", "z")), -1);
    }
    
    @Test
    void testStarEval() {
        checkEval("Hello World", 0, new Star(new Range("A", "z")), 5);
        checkEval("Hello World", 4, new Star(new Range("A", "z")), 1);
        checkEval("Hello World", 5, new Star(new Range("A", "z")), 0);
    }
    
    @Test
    void testOptEval() {
        checkEval("Hello World", 0, new Opt(new Range("A", "z")), 1);
        checkEval("Hello World", 4, new Opt(new Range("A", "z")), 1);
        checkEval("Hello World", 5, new Opt(new Range("A", "z")), 0);
    }
    
    @Test
    void testSeqEval() {
        checkEval("Hello World", 0, new Seq(new PExpr[]{new Range("A", "z"), new Range("a", "z")}), 2);
        checkEval("Hello World", 4, new Seq(new PExpr[]{new Range("A", "z"), new Range("a", "z")}),
                -1);
    }
    
    @Test
    void testAltEval() {
        for (int i = 0; i < 5; i++) {
            checkEval("Hello World", i, new Alt(new PExpr[]{new Range("A", "Z"), new Range("a", "z")}),
                    1);
        }
        checkEval("Hello World", 5, new Alt(new PExpr[]{new Range("A", "Z"), new Range("a", "z")}),
                -1);
    }
    
    @Test
    void testLookaheadEval() {
        checkEval("Hello Hello", 0, new Lookahead(new Terminal("Hello")), 0);
        checkEval("Hello Hello", 1, new Lookahead(new Terminal("Hello")), -1);
        checkEval("Hello Hello", 6, new Lookahead(new Terminal("Hello")), 0);
        checkEval("Hell o", 0, new Lookahead(new Terminal("Hello")), -1);
    }
    
    @Test
    void testNotEval() {
        checkEval("Hello Hello", 0, new Not(new Terminal("Hello")), -1);
        checkEval("Hello Hello", 1, new Not(new Terminal("Hello")), 0);
        checkEval("Hello Hello", 6, new Not(new Terminal("Hello")), -1);
        checkEval("Hell o", 0, new Not(new Terminal("Hello")), 0);
    }
}
