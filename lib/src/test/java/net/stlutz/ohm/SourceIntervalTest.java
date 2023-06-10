package net.stlutz.ohm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceIntervalTest {
    private static final String sentence = "The quick brown fox jumps over the lazy dog.";
    
    private static SourceInterval make(int startIndex, int endIndex) {
        return make(sentence, startIndex, endIndex);
    }
    
    private static SourceInterval make(String sourceString, int startIndex, int endIndex) {
        return new SourceInterval(sourceString, startIndex, endIndex);
    }
    
    void assertThrowsNPE(Executable executable) {
        assertThrows(NullPointerException.class, executable);
    }
    
    void assertThrowsInvalidBounds(Executable executable) {
        var e = assertThrows(RuntimeException.class, executable);
        assertTrue(e.getMessage().contains("Invalid interval bounds"));
    }
    
    void assertThrowsDifferentSource(Executable executable) {
        var e = assertThrows(RuntimeException.class, executable);
        assertTrue(e.getMessage().contains("Interval sources don't match"));
    }
    
    void assertThrowsNoCover(Executable executable) {
        var e = assertThrows(RuntimeException.class, executable);
        assertTrue(e.getMessage().contains("does not cover"));
    }
    
    @Test
    void testCreation() {
        // startIndex out of bounds
        assertThrowsInvalidBounds(() -> make(-1, 5));
        
        // endIndex out of bounds
        assertThrowsInvalidBounds(() -> make(5, 1000));
        
        // endIndex < startIndex
        assertThrowsInvalidBounds(() -> make(19, 16));
        
        // null source string
        assertThrowsNPE(() -> make(null, 16, 19));
    }
    
    @Test
    void testToString() {
        assertEquals("SourceInterval (16, 19, \"fox\")", make(16, 19).toString());
    }
    
    @Test
    void testContents() {
        assertEquals("fox", make(16, 19).getContents());
    }
    
    @Test
    void testLength() {
        assertEquals(3, make(16, 19).length());
    }
    
    @Test
    void testCollapsedLeft() {
        var interval = make(16, 19);
        var collapsedLeft = interval.collapsedLeft();
        
        assertEquals(make(16, 16), collapsedLeft);
        // original should not change
        assertEquals(make(16, 19), interval);
    }
    
    @Test
    void testCollapsedRight() {
        var interval = make(16, 19);
        var collapsedRight = interval.collapsedRight();
        
        assertEquals(make(19, 19), collapsedRight);
        // original should not change
        assertEquals(make(16, 19), interval);
    }
    
    @Test
    void testMinus() {
        // "fox"
        var interval = make(16, 19);
        
        // "brown fox jumps"
        var superset = make(10, 25);
        assertArrayEquals(new SourceInterval[0], interval.minus(superset));
        
        // "brown"
        var before = make(10, 15);
        assertArrayEquals(new SourceInterval[]{interval}, interval.minus(before));
        
        // "jumps"
        var after = make(20, 25);
        assertArrayEquals(new SourceInterval[]{interval}, interval.minus(after));
        
        // "o"
        var split = make(17, 18);
        assertArrayEquals(new SourceInterval[]{make(16, 17), make(18, 19)}, interval.minus(split));
        
        // "brown fo"
        var prefix = make(10, 18);
        assertArrayEquals(new SourceInterval[]{make(18, 19)}, interval.minus(prefix));
        
        // "ox jumps"
        var suffix = make(17, 25);
        assertArrayEquals(new SourceInterval[]{make(16, 17)}, interval.minus(suffix));
        
        assertThrowsNPE(() -> interval.minus(null));
        assertThrowsDifferentSource(() -> interval.minus(make("hello", 1, 4)));
    }
    
    @Test
    void testRelativeTo() {
        // "fox"
        var interval = make(16, 19);
        // "brown fox jumps"
        var context = make(10, 25);
        assertEquals(make(6, 9), interval.relativeTo(context));
        
        assertThrowsNPE(() -> interval.relativeTo(null));
        assertThrowsDifferentSource(() -> interval.relativeTo(make("hello", 1, 4)));
        assertThrowsNoCover(() -> interval.relativeTo(make(18, 22)));
    }
    
    @Test
    void testTrimmed() {
        // "fox"
        var alreadyTrimmed = make(16, 19);
        assertEquals(alreadyTrimmed, alreadyTrimmed.trimmed());
        
        // " fox "
        var needsTrimming = make(15, 20);
        assertEquals(make(16, 19), needsTrimming.trimmed());
        // original should not change
        assertEquals(make(15, 20), needsTrimming);
        
        // " "
        var onlyWhitespace = make(15, 16);
        assertEquals(make(15, 15), onlyWhitespace.trimmed());
    }
    
    @Test
    void testSubInterval() {
        // "brown fox jumps"
        var interval = make(10, 25);
        assertEquals(make(16, 19), interval.subInterval(6, 3));
        
        assertThrowsInvalidBounds(() -> interval.subInterval(30, 10));
    }
    
    @Test
    void testCovers() {
        // "fox"
        var interval = make(16, 19);
        assertTrue(interval.covers(interval));
        assertTrue(interval.covers(make(17, 18)));
        assertTrue(make(10, 25).covers(interval));
        
        assertThrowsNPE(() -> interval.covers(null));
    }
    
    @Test
    void testCover() {
        var interval1 = make(16, 19);
        var interval2 = make(35, 39);
        var interval3 = make(26, 30);
        var coverage = SourceInterval.cover(interval1, interval2, interval3);
        assertTrue(coverage.covers(interval1));
        assertTrue(coverage.covers(interval2));
        assertTrue(coverage.covers(interval3));
        assertEquals(16, coverage.getStartIndex());
        assertEquals(39, coverage.getEndIndex());
        
        var intervalWithDifferentSourceString = make(sentence + " ", 16, 19);
        var e = assertThrows(RuntimeException.class,
                () -> SourceInterval.cover(interval1, intervalWithDifferentSourceString));
        assertTrue(e.getMessage().contains("Interval sources don't match"));
    }
}
