package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

class OhmGrammarTest extends SyntaxTest {
  @Override
  protected Grammar getGrammar() {
    return Grammar.OhmGrammar;
  }

  @Test
  void testComment() {
    shouldMatch("comment", "//", "// todo", "////", "/**/", "/* todo */", "/* \n \n */",
        "/*/////*/");
    shouldNotMatch("comment", "/", "//\n", "/*/");
  }

  @Test
  void testEscapeChar() {
    shouldMatch("escapeChar", "\\\\", "\\\"", "\\\'", "\\b", "\\n", "\\r", "\\t", "\\u{F}",
        "\\u{FF}", "\\u{FFF}", "\\u{1234}", "\\u{56789}", "\\u{11ffff}", "\\u0000", "\\x00");
    shouldNotMatch("escapeChar", "\\z", "\\\n", "\\u{}", "\\u{0000000}", "\\x{0000}", "\\");
  }

  @Test
  void testOneCharTerminal() {
    shouldMatch("oneCharTerminal", "\"a\"", "\"\\\"\"", "\"\\xff\"", "\" \"");
    shouldNotMatch("oneCharTerminal", "\"\"", "\"", "\"ab\"");
  }

  @Test
  void testTerminal() {
    shouldMatch("terminal", "\"\"", "\"\\\"\\\"\"", "\"//abc\"", "\" \"");
    shouldNotMatch("terminal", "\"", "\"\"\"", "\"a\nb\"");
  }

  @Test
  void testIdent() {
    shouldMatch("ident", "_", "__", "_abc", "a_b", "a1", "_123", "a_b_c");
    shouldNotMatch("ident", "123", "0", "", ".a", " ", "a-b");
  }

  @Test
  void testCaseName() {
    shouldMatch("caseName", "--abc\n", "-- _\n", "-- abc_def\n", "--\t\tabc\t\t\n", "-- abc  \n",
        "-- /* yo */ abc\n", "-- abc // yo\n");
    shouldNotMatch("caseName", "", "\n", "-- \n", "-- abc", " -- abc\n", "-- \n abc \n", "--- abc");
  }

  @Test
  void testRuleDescr() {
    shouldMatch("ruleDescr", "()", "(hello world)", "(abc\ndef)", "(//this is not a comment)");
    shouldNotMatch("ruleDescr", "", "(())");
  }

  @Test
  void testBaseRange() {
    shouldMatch("Base_range", "\"a\"..\"c\"", "\"\\u{0}\"..\"\\u{10FFFF}\"");
    shouldNotMatch("Base_range", "", "a..b");
  }

  @Test
  void testBaseApplication() {
    shouldMatch("Base_application", "abc", "abc<>", "abc<def>", "abc<\"def\" \"ghi\">",
        "abc<def,ghi>", "abc<def<ghi>>", "abc<,>", "abc < def >", "abc\n<\ndef\n>");
    shouldNotMatch("Base_application", "abc def", "abc<~>");
  }

  @Test
  void testSeq() {
    shouldMatch("Seq", "", "\"a\"", "\"a\" \"b\"", "\"a\" \"b\" \"c\"");
    shouldNotMatch("Seq", "| \"a\"", "\"a\" |");
  }

  @Test
  void testAlt() {
    shouldMatch("Alt", "", "\"a\"", "\"a\" | \"b\"", "\"a\" | \"b\" | \"c\"", "| \"a\"", "\"a\" |");
  }

  @Test
  void testRuleDefine() {
    shouldMatch("Rule_define", "abc = def", "abc(def)=ghi", "abc<def,ghi>=def ghi",
        "abc<def>(ghi)=jkl--mno\n", "abc = ");
    shouldNotMatch("Rule_define", "", "abc<\"def\">=ghi", "abc == def", "abc += def", "abc := def");
  }

  @Test
  void testRuleOverride() {
    shouldMatch("Rule_override", "abc := def", "abc := ...", "abc:=...|ghi",
        "abc<def,ghi>:=def|...|ghi", "abc := def | ... | jkl -- ghi\n");
    shouldNotMatch("Rule_override", "", "abc(def):=ghi", "abc := def ...",
        "abc := def | ... -- ghi\n");
  }

  @Test
  void testRuleExtend() {
    shouldMatch("Rule_extend", "abc += def", "abc+=ghi|jkl", "abc<def,ghi>+=def|ghi|jkl",
        "abc += def | jkl -- ghi\n");
    shouldNotMatch("Rule_extend", "", "abc(def)+=ghi", "abc += ...", "abc := def", "abc = def");
  }

  @Test
  void testGrammar() {
    shouldMatch("Grammar", "G{}", "G\n{\n}", "G1 <: G2 { }", "G1\n<:\nG2\n{\n}",
        "G { R1 = \"abc\" R2 = \"def\" R3 = \"ghi\" }");
    shouldNotMatch("Grammar", "", "G{}H{}", "G1 < : G2 { }");
  }

  @Test
  void testGrammars() {
    shouldMatch("Grammars", "", "G{}", "G{}H{}I{}", "G{}\n\n\nH{}", "G{}\n\n");
  }
}
