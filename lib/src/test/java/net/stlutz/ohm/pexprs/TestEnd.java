package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.end;

public class TestEnd extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello", i, end(), -1);
        }
        addBasicEvalTestCase("Hello", 6, end(), 0);
    }
}
