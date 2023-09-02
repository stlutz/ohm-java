package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.range;
import static net.stlutz.ohm.pexprs.PExpr.star;

public class TestStar extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello World", 0, star(range("A", "z")), 5, 6);
        addBasicEvalTestCase("Hello World", 4, star(range("A", "z")), 1, 6);
        addBasicEvalTestCase("Hello World", 5, star(range("A", "z")), 0);
    }
}
