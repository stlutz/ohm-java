package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.terminal;

public class TestCaseInsensitiveTerminal extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello", 0, terminal("Hello", true), 5);
        addBasicEvalTestCase(" Hello", 0, terminal("Hello", true), -1);
        addBasicEvalTestCase(" heLlO", 1, terminal("Hello", true), 5);
    }
}
