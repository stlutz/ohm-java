package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.alt;
import static net.stlutz.ohm.pexprs.PExpr.range;

public class TestAlt extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello World", i, alt(range("A", "Z"), range("a", "z")), 1);
        }
        addBasicEvalTestCase("Hello World", 5, alt(range("A", "Z"), range("a", "z")), -1);
    }
}
