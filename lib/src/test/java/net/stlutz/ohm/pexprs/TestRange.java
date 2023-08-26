package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.range;

public class TestRange extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello", 0, range("o", "o"), -1);
        addBasicEvalTestCase("Hello", 4, range("o", "o"), 1);
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello", i, range("A", "z"), 1);
        }
        addBasicEvalTestCase("👍", 0, range("👍", "👍"), 2);
        addBasicEvalTestCase(" 👍", 0, range("👍", "👍"), -1);
    }
}
