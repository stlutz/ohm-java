package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SourceIntervalTest {
  private static final String sentence = "The quick brown fox jumps over the lazy dog.";

  private SourceInterval make(int startIndex, int endIndex) {
    return new SourceInterval(sentence, startIndex, endIndex);
  }

  @Test
  void testContents() {
    SourceInterval interval = make(16, 19);
    assertEquals("fox", interval.getContents());
  }

  @Test
  void testLength() {
    SourceInterval interval = make(16, 19);
    assertEquals(3, interval.length());
  }

  @Test
  void testCollapsedLeft() {
    SourceInterval interval = make(16, 19);
    SourceInterval collapsedLeft = interval.collapsedLeft();

    assertEquals(make(16, 16), collapsedLeft);
    // original should not change
    assertEquals(make(16, 19), interval);
  }

  @Test
  void testCollapsedRight() {
    SourceInterval interval = make(16, 19);
    SourceInterval collapsedRight = interval.collapsedRight();

    assertEquals(make(19, 19), collapsedRight);
    // original should not change
    assertEquals(make(16, 19), interval);
  }

  @Test
  void testCoverage() {
    SourceInterval interval1 = make(16, 19);
    SourceInterval interval2 = make(35, 39);
    SourceInterval interval3 = make(26, 30);
    SourceInterval coverage = interval1.coverageWith(interval2, interval3);
    assertEquals(16, coverage.getStartIndex());
    assertEquals(39, coverage.getEndIndex());
    assertEquals(interval1.getSourceString(), coverage.getSourceString());
  }

  @Test
  void testMinus() {
    // "fox"
    SourceInterval interval = make(16, 19);

    // "brown fox jumps"
    SourceInterval superset = make(10, 25);
    assertArrayEquals(new SourceInterval[0], interval.minus(superset));

    // "brown"
    SourceInterval before = make(10, 15);
    assertArrayEquals(new SourceInterval[] {interval}, interval.minus(before));

    // "jumps"
    SourceInterval after = make(20, 25);
    assertArrayEquals(new SourceInterval[] {interval}, interval.minus(after));

    // "o"
    SourceInterval split = make(17, 18);
    assertArrayEquals(new SourceInterval[] {make(16, 17), make(18, 19)}, interval.minus(split));

    // "brown fo"
    SourceInterval prefix = make(10, 18);
    assertArrayEquals(new SourceInterval[] {make(18, 19)}, interval.minus(prefix));

    // "ox jumps"
    SourceInterval suffix = make(17, 25);
    assertArrayEquals(new SourceInterval[] {make(16, 17)}, interval.minus(suffix));
  }

  @Test
  void testRelativeTo() {
    // "fox"
    SourceInterval interval = make(16, 19);
    // "brown fox jumps"
    SourceInterval context = make(10, 25);
    assertEquals(make(6, 9), interval.relativeTo(context));
  }

  @Test
  void testTrimmed() {
    // "fox"
    SourceInterval alreadyTrimmed = make(16, 19);
    assertEquals(alreadyTrimmed, alreadyTrimmed.trimmed());

    // " fox "
    SourceInterval needsTrimming = make(15, 20);
    assertEquals(make(16, 19), needsTrimming.trimmed());

    // " "
    SourceInterval onlyWhitespace = make(15, 16);
    assertEquals(make(15, 15), onlyWhitespace.trimmed());
  }

  @Test
  void testSubInterval() {
    // "brown fox jumps"
    SourceInterval interval = make(10, 25);
    assertEquals(make(16, 19), interval.subInterval(6, 3));
  }
}
