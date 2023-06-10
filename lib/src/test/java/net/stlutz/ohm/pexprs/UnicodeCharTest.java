package net.stlutz.ohm.pexprs;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnicodeCharTest {
    @Test
    void testUnicodeCategories() {
        Pattern pattern = UnicodeChar.unicodeCategoryPatterns.get("L");
        assertTrue(pattern.matcher("h").matches());
        assertFalse(pattern.matcher(" ").matches());
    }
    
}
