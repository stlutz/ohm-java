package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.any;

public class TestAny extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello", i, any(), 1);
        }
        // TODO: Should Any consume a whole codepoint? yes
        addBasicEvalTestCase("👍", 0, any(), 1);
    }
}
