package ohm.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
