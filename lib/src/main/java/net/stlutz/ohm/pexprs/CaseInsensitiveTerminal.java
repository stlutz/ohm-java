package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.*;

public class CaseInsensitiveTerminal extends PExpr {

    private final PExpr param;

    public CaseInsensitiveTerminal(PExpr param) {
        super();
        this.param = param;
    }

    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return true;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public PExpr introduceParams(String[] formals) {
        return this;
    }

    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return new CaseInsensitiveTerminal(param.substituteParams(actuals));
    }

    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        if (!(param instanceof Terminal)) {
            throw new OhmException("Expected a Terminal expression");
        }
        Terminal terminal = (Terminal) param;
        String matchStr = terminal.getString();

        if (inputStream.matches(matchStr, true)) {
            inputStream.advance(matchStr.length());
            matchState.pushBinding(TerminalNode.get(matchStr.length()), originalPosition);
            return true;
        }

        return false;
    }

    @Override
    public void toString(StringBuilder sb) {
        param.toString(sb);
        sb.append(" (case-insensitive)");
    }
}
