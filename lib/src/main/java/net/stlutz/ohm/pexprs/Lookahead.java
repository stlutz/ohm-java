package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.*;

public class Lookahead extends PExpr {
    public PExpr expr;

    public Lookahead(PExpr expr) {
        super();
        this.expr = expr;
    }

    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return false;
    }

    @Override
    public int getArity() {
        return expr.getArity();
    }

    @Override
    public PExpr introduceParams(String[] formals) {
        expr = expr.introduceParams(formals);
        return this;
    }

    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return new Lookahead(expr.substituteParams(actuals));
    }

    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        if (matchState.eval(expr)) {
            inputStream.setPosition(originalPosition);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append('&');
        expr.toString(sb);
    }
}
