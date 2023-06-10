package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LineAndColumnInfoTest {
    String loremIpsum() {
        return """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                Ullamcorper malesuada proin libero nunc consequat interdum varius sit amet.
                Ut morbi tincidunt augue interdum.
                Mauris sit amet massa vitae tortor condimentum lacinia quis vel.
                Nulla posuere sollicitudin aliquam ultrices.
                Varius duis at consectetur lorem.
                Morbi tristique senectus et netus et malesuada fames ac turpis.
                Et malesuada fames ac turpis egestas.
                Nisl nunc mi ipsum faucibus vitae aliquet nec.
                Odio pellentesque diam volutpat commodo sed.
                Sodales ut eu sem integer vitae.
                Massa tempor nec feugiat nisl pretium.
                Orci ac auctor augue mauris.""";
    }
    
    @Test
    void testMiddleLine() {
        LineAndColumnInfo info = LineAndColumnInfo.from(loremIpsum(), 209);
        // >t<incidunt
        assertEquals(3, info.lineNum);
        assertEquals(10, info.columnNum);
        assertEquals("Ut morbi tincidunt augue interdum.", info.line);
        assertEquals("Ullamcorper malesuada proin libero nunc consequat interdum varius sit amet.",
                info.previousLine);
        assertEquals("Mauris sit amet massa vitae tortor condimentum lacinia quis vel.", info.nextLine);
    }
    
    @Test
    void testFirstLine() {
        LineAndColumnInfo info = LineAndColumnInfo.from(loremIpsum(), 57);
        // >s<ed
        assertEquals(1, info.lineNum);
        assertEquals(58, info.columnNum);
        assertEquals(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                info.line);
        assertNull(info.previousLine);
        assertEquals("Ullamcorper malesuada proin libero nunc consequat interdum varius sit amet.",
                info.nextLine);
    }
    
    @Test
    void testLastLine() {
        LineAndColumnInfo info = LineAndColumnInfo.from(loremIpsum(), 645);
        // >O<rci
        assertEquals(13, info.lineNum);
        assertEquals(1, info.columnNum);
        assertEquals("Orci ac auctor augue mauris.", info.line);
        assertEquals("Massa tempor nec feugiat nisl pretium.", info.previousLine);
        assertNull(info.nextLine);
    }
    
    @Test
    void testFirstChar() {
        LineAndColumnInfo info = LineAndColumnInfo.from(loremIpsum(), 0);
        // >L<orem
        assertEquals(1, info.lineNum);
        assertEquals(1, info.columnNum);
        assertEquals(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                info.line);
        assertNull(info.previousLine);
        assertEquals("Ullamcorper malesuada proin libero nunc consequat interdum varius sit amet.",
                info.nextLine);
    }
    
    @Test
    void testLastChar() {
        LineAndColumnInfo info = LineAndColumnInfo.from(loremIpsum(), 672);
        // mauris>.<
        assertEquals(13, info.lineNum);
        assertEquals(28, info.columnNum);
        assertEquals("Orci ac auctor augue mauris.", info.line);
        assertEquals("Massa tempor nec feugiat nisl pretium.", info.previousLine);
        assertNull(info.nextLine);
    }
}
