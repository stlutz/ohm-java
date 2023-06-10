package net.stlutz.ohm;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SyntaxTest {
    protected Grammar grammar;
    
    protected abstract Grammar getGrammar();
    
    @BeforeEach
    void initializeGrammar() {
        grammar = getGrammar();
    }
    
    private void assertMatch(String startRule, String[] inputs, boolean shouldMatch) {
        for (String input : inputs) {
            var matchResult = grammar.match(input, startRule);
            boolean doesMatch = matchResult.succeeded();
            assertEquals(shouldMatch, doesMatch, () -> {
                String template =
                        shouldMatch ? "Expected input '%s' to be matched by rule '%s', but it wasn't"
                                : "Expected input '%s' to not be matched by rule '%s', but it was";
                return template.formatted(input, startRule);
            });
        }
    }
    
    protected void shouldMatch(String startRule, String... inputs) {
        assertMatch(startRule, inputs, true);
    }
    
    protected void shouldNotMatch(String startRule, String... inputs) {
        assertMatch(startRule, inputs, false);
    }
}
