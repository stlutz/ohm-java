package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.plus;
import static net.stlutz.ohm.pexprs.PExpr.range;

public class TestPlus extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello World", 0, plus(range("A", "z")), 5);
        addBasicEvalTestCase("Hello World", 4, plus(range("A", "z")), 1);
        addBasicEvalTestCase("Hello World", 5, plus(range("A", "z")), -1);
    }
}
