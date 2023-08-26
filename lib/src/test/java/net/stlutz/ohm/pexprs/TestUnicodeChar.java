package net.stlutz.ohm.pexprs;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static net.stlutz.ohm.pexprs.PExpr.unicodeChar;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUnicodeChar extends AbstractPExprTest {
    @Test
    void testUnicodeCategories() {
        Pattern pattern = UnicodeChar.unicodeCategoryPatterns.get("L");
        assertTrue(pattern.matcher("h").matches());
        assertFalse(pattern.matcher(" ").matches());
    }
    
    @Override
    protected void registerBasicEvalTestCases() {
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello", i, unicodeChar("L"), 1);
        }
        addBasicEvalTestCase("Hello", 0, unicodeChar("Ll"), -1);
        addBasicEvalTestCase("Hello", 1, unicodeChar("Lu"), -1);
    }
}
