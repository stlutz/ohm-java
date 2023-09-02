package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.range;
import static net.stlutz.ohm.pexprs.PExpr.seq;

public class TestSeq extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello World", 0, seq(range("A", "z"), range("a", "z")), 2);
        addBasicEvalTestCase("Hello World", 4, seq(range("A", "z"), range("a", "z")), -1, 6);
    }
}
