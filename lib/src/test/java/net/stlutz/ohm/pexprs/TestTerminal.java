package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.terminal;

public class TestTerminal extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello", 0, terminal("Hello"), 5);
        addBasicEvalTestCase(" Hello", 0, terminal("Hello"), -1);
        addBasicEvalTestCase(" Hello", 1, terminal("Hello"), 5);
        addBasicEvalTestCase("hello", 0, terminal("Hello"), -1);
    }
}
