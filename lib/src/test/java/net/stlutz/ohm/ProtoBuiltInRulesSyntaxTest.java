package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

class ProtoBuiltInRulesSyntaxTest extends SyntaxTest {
  @Override
  protected Grammar getGrammar() {
    return Grammar.ProtoBuiltInRules;
  }

  @Test
  void testAny() {
    shouldMatch("any", "H");
    shouldNotMatch("any", "H ", "");
  }

  @Test
  void testEnd() {
    shouldMatch("end", "");
    shouldNotMatch("end", "H");
  }

  @Test
  void testLower() {
    shouldMatch("lower", "h");
    shouldNotMatch("lower", "H", " ");
  }

  @Test
  void testUpper() {
    shouldMatch("upper", "H");
    shouldNotMatch("upper", "h", " ");
  }

  @Test
  void testUnicodeLtmo() {
    shouldMatch("unicodeLtmo", "ˇ");
    shouldNotMatch("unicodeLtmo", "H");
  }

  @Test
  void testSpace() {
    shouldMatch("space", " ", "\n", "\t");
    shouldNotMatch("space", "H", ".");
  }

  @Test
  void testSpaces() {
    shouldMatch("spaces", "", " ", "    ", "\t\t\t");
    shouldNotMatch("spaces", "H", "  H", ".\n");
  }

}
