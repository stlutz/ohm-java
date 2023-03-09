package net.stlutz.ohm;

import static net.stlutz.ohm.MockNode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArithmeticSemanticsTest {
  ArithmeticSemantics semantics;

  static ArithmeticSemantics make() {
    return SemanticsBlueprint.create(ArithmeticSemantics.class).instantiate();
  }

  @BeforeEach
  void beforeEach() {
    semantics = make();
  }

  double interpret(Node node) {
    return semantics.interpret(node);
  }

  @Test
  void testInterpret01() {
    assertEquals(42.0, interpret(Nonterminal("Exp", Nonterminal("AddExp",
        Nonterminal("MulExp", Nonterminal("ExpExp", Nonterminal("PriExp", number(42))))))));
  }

  @Test
  void testInterpret02() {
    assertEquals(42.0,
        interpret(
            Nonterminal("Exp",
                Nonterminal("AddExp",
                    Nonterminal("MulExp",
                        Nonterminal("MulExp_times", Nonterminal("MulExp", Nonterminal("ExpExp",
                            Nonterminal("PriExp", Nonterminal("PriExp_paren", Terminal("("),
                                Nonterminal("Exp", Nonterminal("AddExp", Nonterminal("AddExp_plus",
                                    Nonterminal("AddExp", Nonterminal("MulExp",
                                        Nonterminal("ExpExp", Nonterminal("PriExp", number(2))))),
                                    Terminal("+"),
                                    Nonterminal("MulExp",
                                        Nonterminal("ExpExp", Nonterminal("PriExp", number(4))))))),
                                Terminal(")"))))),
                            Terminal("*"),
                            Nonterminal("ExpExp", Nonterminal("PriExp", number(7)))))))));
  }

  @Test
  void testInterpret03() {
    assertEquals(Math.PI,
        interpret(
            Nonterminal("Exp", Nonterminal("AddExp", Nonterminal("MulExp", Nonterminal("ExpExp",
                Nonterminal("PriExp", Nonterminal("ident", letter('p'), Iter(alnum('i'))))))))));
  }
}
