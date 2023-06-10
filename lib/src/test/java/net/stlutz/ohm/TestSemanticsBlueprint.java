package net.stlutz.ohm;

import org.junit.jupiter.api.Test;

import static net.stlutz.ohm.MockNode.Nonterminal;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSemanticsBlueprint {
    Throwable e;
    
    public static class SimpleSemantics extends Semantics {
        @Action
        public String Exp() {
            return "Exp";
        }
    }
    
    @Test
    void testSimpleSemantics() {
        SemanticsBlueprint.create(SimpleSemantics.class).instantiate();
    }
    
    public static abstract class AbstractSemantics extends SimpleSemantics {
    }
    
    @Test
    void testAbstractSemanticsFailBlueprinting() {
        Throwable e = assertThrows(OhmException.class, () -> SemanticsBlueprint.create(AbstractSemantics.class));
        assertTrue(e.getMessage().contains("abstract"));
    }
    
    static class NonPublicSemantics extends SimpleSemantics {
    }
    
    @Test
    void testNonPublicSemanticsFailBlueprinting() {
        Throwable e = assertThrows(OhmException.class, () -> SemanticsBlueprint.create(NonPublicSemantics.class));
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
        SemanticsBlueprint<?> bp = SemanticsBlueprint.create(ErrorInConstructorSemantics.class);
        Throwable e = assertThrows(OhmException.class, () -> bp.on(Nonterminal("Exp")));
        assertTrue(e.getMessage().contains("constructor"));
    }
    
    public class MemberSemantics extends SimpleSemantics {
    }
    
    @Test
    void testMemberSemanticsFailInstantiationWithoutInstance() {
        SemanticsBlueprint<?> blueprint = SemanticsBlueprint.create(MemberSemantics.class);
        Throwable e = assertThrows(OhmException.class, () -> blueprint.instantiate());
        assertTrue(e.getMessage().contains("Cannot instantiate this semantics blueprint without the enclosing instance"));
    }
    
    @Test
    void testMemberSemanticsFailInstantiationWithWrongInstance() {
        SemanticsBlueprint<?> blueprint = SemanticsBlueprint.create(MemberSemantics.class);
        Throwable e = assertThrows(OhmException.class, () -> blueprint.instantiate(new Object()));
        assertTrue(e.getMessage().contains("enclosing instance is not an instance of"));
    }
}
