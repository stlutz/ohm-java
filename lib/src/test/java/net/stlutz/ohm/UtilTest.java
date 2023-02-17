package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UtilTest {
	@Test
	void testSyntacticRuleNames() {
		assertTrue(Util.isSyntactic("AddExp"));
		assertTrue(Util.isSyntactic("Array"));
		assertFalse(Util.isSyntactic("addExp"));
		assertFalse(Util.isSyntactic("array"));
	}

	@Test
	void testLexicalRuleNames() {
		assertTrue(Util.isLexical("addExp"));
		assertTrue(Util.isLexical("array"));
		assertFalse(Util.isLexical("AddExp"));
		assertFalse(Util.isLexical("Array"));
	}
}
