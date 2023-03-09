package net.stlutz.ohm;

import static net.stlutz.ohm.MockNode.Nonterminal;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SemanticsTest {
  Semantics semantics;
  OhmException e;

  static SemanticsBlueprint blueprint(Class<? extends Semantics> semanticsClass) {
    return SemanticsBlueprint.create(semanticsClass);
  }

  public static class SimpleSemantics extends Semantics {
    public class SimpleOperation extends Operation {
      @Action
      public String Exp() {
        return "Exp";
      }
    }
  }

  @Test
  void testSimpleSemantics() {
    semantics = blueprint(SimpleSemantics.class).on(Nonterminal("Exp"));
    assertEquals("Exp", semantics.execute());
  }

  public static class NoOpSemantics extends Semantics {
  }

  @Test
  void testSemanticsWithoutOperationsFailBluerpinting() {
    e = assertThrows(OhmException.class, () -> blueprint(NoOpSemantics.class));
    assertTrue(e.getMessage().contains("operations"));
  }

  public static abstract class AbstractSemantics extends SimpleSemantics {
  }

  @Test
  void testAbstractSemanticsFailBluerpinting() {
    e = assertThrows(OhmException.class, () -> blueprint(AbstractSemantics.class));
    assertTrue(e.getMessage().contains("abstract"));
  }

  public class MemberSemantics extends SimpleSemantics {
  }

  @Test
  void testMemberSemanticsFailBluerpinting() {
    e = assertThrows(OhmException.class, () -> blueprint(MemberSemantics.class));
    assertTrue(e.getMessage().contains("static"));
  }

  static class NonPublicSemantics extends SimpleSemantics {
  }

  @Test
  void testNonPublicSemanticsFailBluerpinting() {
    e = assertThrows(OhmException.class, () -> blueprint(NonPublicSemantics.class));
    assertTrue(e.getMessage().contains("public"));
  }

  static class ErrorInConstructorSemantics extends SimpleSemantics {
    public ErrorInConstructorSemantics() {
      super();
      throw new RuntimeException("oops");
    }
  }

  @Test
  void testErrorInConstructorFailsInstantiation() {
    SemanticsBlueprint bp = blueprint(ErrorInConstructorSemantics.class);
    e = assertThrows(OhmException.class, () -> bp.on(Nonterminal("Exp")));
    assertTrue(e.getMessage().contains("constructor"));
  }
}
