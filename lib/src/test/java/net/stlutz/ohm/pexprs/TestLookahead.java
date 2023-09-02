package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.lookahead;
import static net.stlutz.ohm.pexprs.PExpr.terminal;

public class TestLookahead extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello Hello", 0, lookahead(terminal("Hello")), 0, 5);
        addBasicEvalTestCase("Hello Hello", 1, lookahead(terminal("Hello")), -1);
        addBasicEvalTestCase("Hello Hello", 6, lookahead(terminal("Hello")), 0, 11);
        addBasicEvalTestCase("Hell o", 0, lookahead(terminal("Hello")), -1, 5);
    }
}
