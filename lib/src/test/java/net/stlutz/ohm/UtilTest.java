package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

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
