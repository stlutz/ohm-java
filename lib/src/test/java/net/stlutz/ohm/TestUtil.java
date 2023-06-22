package net.stlutz.ohm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUtil {
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
    
    static Stream<Arguments> escapeStringTestCases() {
        return Stream.of(
            Arguments.arguments("no escapes", "hello world", "hello world"),
            Arguments.arguments("char escapes", "\\ \" ' \b \t \n \r", "\\\\ \\\" \\' \\b \\t \\n \\r"),
            Arguments.arguments("unicode", "\u0019\u2000", "\\x19\\u2000"),
            Arguments.arguments("unicode > FFFF", "hello \uD83D\uDC4D world", "hello \\u{1F44D} world")
        );
    }
    
    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("escapeStringTestCases")
    void testEscapeString(String name, String unescapedString, String expectedResult) {
        StringBuilder sb = new StringBuilder();
        Util.escapeString(unescapedString, sb);
        assertEquals(expectedResult, sb.toString());
    }
    
    @Test
    void testEscapeUnescapeAsciiString() {
        for (char c = 0; c < 256; c++) {
            String unescapedString = Character.toString(c);
            String escapedString = Util.escapedString(unescapedString);
            assertEquals(unescapedString, Util.unescapedString(escapedString));
        }
    }
    
    static Stream<Arguments> unescapeSubstringTestCases() {
        return Stream.of(
            Arguments.arguments("no escapes", "hello world", 0, 11, "hello world"),
            Arguments.arguments("no escapes substring", "hello world", 1, 10, "ello worl"),
            Arguments.arguments("unicode > FFFF", "hello \uD83D\uDC4D world", 0, 14, "hello \uD83D\uDC4D world"),
            
            Arguments.arguments("start < 0", "hello world", -1, 11, null),
            Arguments.arguments("end < 0", "hello world", 0, -1, null),
            Arguments.arguments("start > length", "hello world", 11, 11, null),
            Arguments.arguments("end > length", "hello world", 0, 12, null),
            Arguments.arguments("start >= end", "hello world", 11, 0, null),
            
            Arguments.arguments("char escapes", "start \\\\ \\\" \\' \\b \\t \\n \\r end", 6, 26, "\\ \" ' \b \t \n \r"),
            Arguments.arguments("invalid char escape", "\\k", 0, 2, null),
            Arguments.arguments("interrupted char escape", "\\n", 0, 1, null),
            
            Arguments.arguments("4-digit unicode escape", "\\u0047\\u004f\\u004F\\u2191", 0, 24, "GOO↑"),
            Arguments.arguments("incomplete 4-digit unicode escape", "\\u123", 0, 5, null),
            Arguments.arguments("interrupted 4-digit unicode escape", "\\u1234", 0, 5, null),
            Arguments.arguments("non-hex 4-digit unicode escape", "\\u1gba", 0, 6, null),
            
            Arguments.arguments("2-digit unicode escape", "\\x47\\x4f\\x4F", 0, 12, "GOO"),
            Arguments.arguments("incomplete 2-digit unicode escape", "\\x4", 0, 3, null),
            Arguments.arguments("interrupted 2-digit unicode escape", "\\x47", 0, 3, null),
            Arguments.arguments("non-hex 2-digit unicode escape", "\\x4g", 0, 4, null),
            
            Arguments.arguments("braced unicode escape", "\\u{9}\\u{4f}\\u{04F}\\u{2191}\\u{1F44D}\\u{01F44D}", 0, 45, "\tOO↑\uD83D\uDC4D\uD83D\uDC4D"),
            Arguments.arguments("empty braced unicode escape", "\\u{}", 0, 4, null),
            Arguments.arguments(">6 digits braced unicode escape", "\\u{1234567}", 0, 11, null),
            Arguments.arguments("unclosed braced unicode escape", "\\u{1234 hello world", 0, 19, null),
            Arguments.arguments("incomplete braced unicode escape", "\\u{1234", 0, 7, null),
            Arguments.arguments("interrupted braced unicode escape", "\\u{1234}", 0, 3, null),
            Arguments.arguments("non-hex braced unicode escape", "\\u{1gba}", 0, 8, null)
        );
    }
    
    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("unescapeSubstringTestCases")
    void testUnescapeSubstring(String name, String escapedString, int start, int end, String expectedResult) {
        StringBuilder sb = new StringBuilder();
        boolean success = Util.unescapeSubstring(escapedString, start, end, sb);
        assertEquals(expectedResult != null, success);
        if (success) {
            assertEquals(expectedResult, sb.toString());
        }
    }
}
