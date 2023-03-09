package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

class BuiltInRulesTest extends SyntaxTest {
  @Override
  protected Grammar getGrammar() {
    return Grammar.BuiltInRules;
  }

  @Test
  void testAlnum() {
    shouldMatch("alnum", "H", "h", "0", "1", "9");
    shouldNotMatch("alnum", ".", "0x0", "10");
  }

  @Test
  void testLetter() {
    shouldMatch("letter", "H", "h", "ˇ");
    shouldNotMatch("letter", "0", "1", "9", "0x0", "10");
  }

  @Test
  void testDigit() {
    shouldMatch("digit", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    shouldNotMatch("digit", "0x0", "10", "H", "h", "00", "A");
  }

  @Test
  void testHexDigit() {
    shouldMatch("hexDigit", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "f", "A", "F");
    shouldNotMatch("hexDigit", "0x0", "10", "H", "h", "00", "G", "g");
  }

  // TODO: implement startApplication
  // @Test
  // void testListOf() {
  // shouldMatch("ListOf<", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "f", "A", "F");
  // shouldNotMatch("hexDigit", "0x0", "10", "H", "h", "00", "G", "g");
  // }
}
