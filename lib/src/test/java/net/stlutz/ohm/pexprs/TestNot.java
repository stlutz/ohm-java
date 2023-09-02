package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.not;
import static net.stlutz.ohm.pexprs.PExpr.terminal;

public class TestNot extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        addBasicEvalTestCase("Hello Hello", 0, not(terminal("Hello")), -1, 5);
        addBasicEvalTestCase("Hello Hello", 1, not(terminal("Hello")), 0);
        addBasicEvalTestCase("Hello Hello", 6, not(terminal("Hello")), -1, 11);
        addBasicEvalTestCase("Hell o", 0, not(terminal("Hello")), 0, 5);
    }
}
