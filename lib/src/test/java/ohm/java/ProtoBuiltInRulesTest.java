package ohm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProtoBuiltInRulesTest {
	private Grammar grammar = Grammar.ProtoBuiltInRules;

	void checkMatch(String rule, boolean shouldMatch, String input) {
		assertEquals(shouldMatch, grammar.match(input, rule).succeeded());
	}

	@Test
	void testAny() {
		checkMatch("any", true, "H");
		checkMatch("any", false, "H ");
	}

	@Test
	void testEnd() {
		checkMatch("end", true, "");
		checkMatch("end", false, "H");
	}

	@Test
	void testLower() {
		checkMatch("lower", true, "h");
		checkMatch("lower", false, "H ");
	}

	@Test
	void testUpper() {
		checkMatch("upper", true, "H");
		checkMatch("upper", false, "h ");
	}

	@Test
	void testUnicodeLtmo() {
		checkMatch("unicodeLtmo", true, "ˇ");
		checkMatch("unicodeLtmo", false, "H");
	}

	@Test
	void testSpace() {
		checkMatch("space", true, " ");
		checkMatch("space", false, "H");
	}

	@Test
	void testSpaces() {
		checkMatch("spaces", true, "");
		checkMatch("spaces", true, " ");
		checkMatch("spaces", true, "      ");
		checkMatch("spaces", false, "H");
	}

}
