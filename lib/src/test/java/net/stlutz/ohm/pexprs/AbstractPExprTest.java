package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;
import net.stlutz.ohm.Matcher;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPExprTest {
    private final List<Arguments> basicEvalTestCases = new ArrayList<>();
    
    public AbstractPExprTest() {
        registerBasicEvalTestCases();
    }
    
    private static MatchState getMatchState(String input, Apply startExpr) {
        Matcher matcher = new Matcher(null, input);
        return new MatchState(matcher, startExpr);
    }
    
    private MatchState newMatchState(String input, int offset) {
        MatchState matchState = getMatchState(input, new Apply("rule"));
        matchState.getInputStream().setPosition(offset);
        return matchState;
    }
    
    protected void addBasicEvalTestCase(String input, int offset, PExpr expr, int matchLength) {
        basicEvalTestCases.add(Arguments.of(input, offset, expr, matchLength));
    }
    
    protected Stream<Arguments> basicEvalTestCases() {
        return basicEvalTestCases.stream();
    }
    
    abstract protected void registerBasicEvalTestCases();
    
    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("basicEvalTestCases")
    public void testBasicEval(String input, int offset, PExpr expr, int matchLength) {
        MatchState matchState = newMatchState(input, offset);
        InputStream inputStream = matchState.getInputStream();
        int arity = expr.getArity();
        int numBindingsBefore = matchState.numBindings();
        
        boolean succeeded = expr.eval(matchState);
        
        assertEquals(matchLength >= 0, succeeded);
        if (succeeded) {
            assertEquals(arity, matchState.numBindings() - numBindingsBefore);
            assertEquals(matchLength + offset, inputStream.getPosition());
        }
    }
}
