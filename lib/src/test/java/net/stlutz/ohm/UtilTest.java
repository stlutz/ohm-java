package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    
    private static Collection<String> duplicates(String... strings) {
        return Util.getDuplicates(strings);
    }
    
    @Test
    void testGetDuplicates() {
        assertEquals(0, duplicates("hello", "world").size());
        Collection<String> dups = duplicates("hello", "world", "hello");
        assertEquals(1, dups.size());
        assertTrue(dups.contains("hello"));
        assertEquals(0, duplicates().size());
        
    }
}
