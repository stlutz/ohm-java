package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.opt;
import static net.stlutz.ohm.pexprs.PExpr.range;

public class TestOpt extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello World", 0, opt(range("A", "z")), 1);
        addBasicEvalTestCase("Hello World", 4, opt(range("A", "z")), 1);
        addBasicEvalTestCase("Hello World", 5, opt(range("A", "z")), 0);
    }
}
