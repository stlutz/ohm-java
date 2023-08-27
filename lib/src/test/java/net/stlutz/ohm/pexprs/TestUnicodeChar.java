package net.stlutz.ohm.pexprs;

import static net.stlutz.ohm.pexprs.PExpr.letter;
import static net.stlutz.ohm.pexprs.PExpr.unicodeChar;

class TestUnicodeChar extends AbstractPExprTest {
    @Override
    protected void registerBasicEvalTestCases() {
        for (int i = 0; i < 5; i++) {
            addBasicEvalTestCase("Hello", i, letter(), 1);
        }
        addBasicEvalTestCase("Hello", 0, unicodeChar(Character.LOWERCASE_LETTER), -1);
        addBasicEvalTestCase("Hello", 1, unicodeChar(Character.UPPERCASE_LETTER), -1);
    }
}
