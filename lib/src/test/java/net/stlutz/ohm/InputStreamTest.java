package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputStreamTest {
  private static final String sentence = "The quick brown fox jumps over the lazy dog.";
  // "Good job 👍 👍🏿 👍🏽 👍🏻!!!"
  // 1F44D -> D83D DC4D
  // 1F3FF -> D83C DFFF
  // 1F3FD -> D83C DFFD
  // 1F3FB -> D83C DFFB
  private static final String unicodeString =
      "Good job \uD83D\uDC4D \uD83D\uDC4D\uD83C\uDFFF \uD83D\uDC4D\uD83C\uDFFD \uD83D\uDC4D\uD83C\uDFFB!!!";

  private InputStream stream;

  @BeforeEach
  void beforeEach() {
    stream = new InputStream(sentence);
  }

  @Test
  void testExaminedLength() {
    // TODO: what is it even supposed to do?
  }

  @Test
  void testAtEnd() {
    assertFalse(stream.atEnd());
    stream.setPosition(16); // lazy >f<ox
    assertFalse(stream.atEnd());
    stream.setPosition(43); // dog>.<
    assertFalse(stream.atEnd());
    stream.setPosition(44);
    assertTrue(stream.atEnd());
  }

  @Test
  void testNext() {
    assertEquals('T', stream.next());
    assertEquals('h', stream.next());
    assertEquals('e', stream.next());
    assertEquals(3, stream.getPosition());

    stream.setPosition(42);
    assertEquals('g', stream.next());
    assertEquals('.', stream.next());
    assertThrows(RuntimeException.class, () -> stream.next(), "Should throw exception if at end");
  }

  @Test
  void testUnicodeNext() {
    stream = new InputStream(unicodeString);
    stream.setPosition(8);
    assertEquals(' ', stream.next());
    assertEquals('\uD83D', stream.next());
    assertEquals('\uDC4D', stream.next());
    assertEquals(11, stream.getPosition());
    stream.setPosition(12);
    assertEquals('\uD83D', stream.next());
    assertEquals('\uDC4D', stream.next());
    assertEquals('\uD83C', stream.next());
    assertEquals('\uDFFF', stream.next());
    assertEquals(' ', stream.next());
  }

  @Test
  void testNextCharCode() {
    assertEquals(84, stream.nextCharCode());
    assertEquals(104, stream.nextCharCode());
    assertEquals(101, stream.nextCharCode());
    assertEquals(3, stream.getPosition());

    stream.setPosition(42);
    assertEquals(103, stream.nextCharCode());
    assertEquals(46, stream.nextCharCode());
    assertThrows(RuntimeException.class, () -> stream.nextCharCode(),
        "Should throw exception if at end");
  }

  @Test
  void testUnicodeNextCharCode() {
    stream = new InputStream(unicodeString);
    stream.setPosition(8);
    assertEquals(32, stream.nextCharCode());
    assertEquals(0xD83D, stream.nextCharCode());
    assertEquals(0xDC4D, stream.nextCharCode());
    assertEquals(11, stream.getPosition());
    stream.setPosition(12);
    assertEquals(0xD83D, stream.nextCharCode());
    assertEquals(0xDC4D, stream.nextCharCode());
    assertEquals(0xD83C, stream.nextCharCode());
    assertEquals(0xDFFF, stream.nextCharCode());
    assertEquals(32, stream.nextCharCode());
  }

  @Test
  void testNextCodePoint() {
    assertEquals(84, stream.nextCodePoint());
    assertEquals(104, stream.nextCodePoint());
    assertEquals(101, stream.nextCodePoint());
    assertEquals(3, stream.getPosition());

    stream.setPosition(42);
    assertEquals(103, stream.nextCodePoint());
    assertEquals(46, stream.nextCodePoint());
    assertThrows(RuntimeException.class, () -> stream.nextCodePoint(),
        "Should throw exception if at end");
  }

  @Test
  void testUnicodeNextCodePoint() {
    stream = new InputStream(unicodeString);
    stream.setPosition(8);
    assertEquals(32, stream.nextCodePoint());
    assertEquals(0x1F44D, stream.nextCodePoint());
    assertEquals(11, stream.getPosition());
    stream.setPosition(12);
    assertEquals(0x1F44D, stream.nextCodePoint());
    assertEquals(0x1F3FF, stream.nextCodePoint());
    assertEquals(32, stream.nextCharCode());
    assertEquals(17, stream.getPosition());
    stream.setPosition(18);
    assertEquals(0xDC4D, stream.nextCodePoint(),
        "Should return char code if in the middle of a code point");
    assertEquals(19, stream.getPosition());
  }

  @Test
  void testMatchesStringCaseInsensitive() {
    assertTrue(stream.matches("The quick ", true), "Should match equal strings");
    assertTrue(stream.matches("The quick ", true), "Should not advance the position");
    assertFalse(stream.matches("brown fox", true), "Should not match unequal strings");
    assertTrue(stream.matches("The Quick ", true), "Should match differing case");

    stream.setPosition(40); // lazy >d<og
    assertTrue(stream.matches("DOg.", true), "Should match at the current position");
    assertFalse(stream.matches("dog. ", true),
        "Should not error or match when matching past the end");
  }

  @Test
  void testMatchesStringCaseSensitive() {
    assertTrue(stream.matches("The quick "), "Should match equal strings");
    assertTrue(stream.matches("The quick "), "Should not advance the position");
    assertFalse(stream.matches("brown fox"), "Should not match unequal strings");
    assertFalse(stream.matches("The Quick "), "Should not match differing case");

    stream.setPosition(40); // lazy >d<og
    assertTrue(stream.matches("dog."), "Should match at the current position");
    assertFalse(stream.matches("dog. "), "Should not error or match when matching past the end");
  }

  @Test
  void testMatchPattern() {
    assertEquals(10, stream.match(Pattern.compile("The quick ")));
    assertEquals(0, stream.getPosition(), "Should not advance the position");
    assertEquals(-1, stream.match(Pattern.compile("brown fox")));
    stream.setPosition(40); // lazy >d<og
    assertEquals(4, stream.match(Pattern.compile("dog.")));
    assertEquals(-1, stream.match(Pattern.compile("dog. ")),
        "Should not error or match when matching past the end");
  }

  @Test
  void testUnicodeMatchPattern() {
    stream = new InputStream(unicodeString);
    stream.setPosition(8);
    assertEquals(1, stream.match(Pattern.compile("\\p{Zs}")));
    stream.setPosition(9);
    assertEquals(2, stream.match(Pattern.compile("\\p{So}")));
    assertEquals(2, stream.match(Pattern.compile("\\x{1f44d}")));
    stream.setPosition(10);
    assertEquals(-1, stream.match(Pattern.compile("\\p{So}")));
  }

  @Test
  void testSourceSlice() {
    assertEquals("The", stream.sourceSlice(0, 3));
    assertEquals("dog.", stream.sourceSlice(40, 44));
    assertThrows(RuntimeException.class, () -> stream.sourceSlice(40, 45),
        "Should throw exception when slicing over end");
  }

  @Test
  void testSourceIntervalRange() {
    stream.setPosition(19); // brown fox> <jumps
    assertEquals(new SourceInterval(sentence, 16, 25), stream.sourceInterval(16, 25));
  }

  @Test
  void testSourceIntervalStart() {
    stream.setPosition(19); // brown fox> <jumps
    assertEquals(new SourceInterval(sentence, 16, 19), stream.sourceInterval(16));
  }

}
