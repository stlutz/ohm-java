package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

public abstract class SyntaxTest {
	protected Grammar grammar;

	protected abstract Grammar getGrammar();

	@BeforeEach
	void initializeGrammar() {
		grammar = getGrammar();
	}

	private void assertMatch(String startRule, String[] inputs, boolean shouldMatch) {
		for (String input : inputs) {
			assertEquals(shouldMatch, grammar.match(input, startRule).succeeded());
		}
	}

	protected void shouldMatch(String startRule, String... inputs) {
		assertMatch(startRule, inputs, true);
	}

	protected void shouldNotMatch(String startRule, String... inputs) {
		assertMatch(startRule, inputs, false);
	}
}
