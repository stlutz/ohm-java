package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;

public class Lex extends PExpr {
    public PExpr expr;
    
    public Lex(PExpr expr) {
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
        return new Lex(expr.substituteParams(actuals));
    }
    
    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        matchState.enterLexifiedContext();
        boolean succeeded = matchState.eval(expr);
        matchState.exitLexifiedContext();
        return succeeded;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("#(");
        expr.toString(sb);
        sb.append(')');
    }
}
