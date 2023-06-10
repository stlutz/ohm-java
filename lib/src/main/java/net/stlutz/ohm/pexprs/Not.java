package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;

public class Not extends PExpr {
    public PExpr expr;
    
    public Not(PExpr expr) {
        super();
        this.expr = expr;
    }
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return false;
    }
    
    @Override
    public int getArity() {
        return 0;
    }
    
    @Override
    public PExpr introduceParams(String[] formals) {
        expr = expr.introduceParams(formals);
        return this;
    }
    
    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return new Not(expr.substituteParams(actuals));
    }
    
    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        // TODO: Right now we're just throwing away all of the failures that happen
        // inside a `not`, and recording `this` as a failed expression.
        // TODO: Double negation should be equivalent to lookahead, but that's not the
        // case right now wrt failures. E.g., ~~'foo' produces a failure for ~~'foo',
        // but maybe it should produce a failure for 'foo' instead.
        boolean matched = matchState.eval(expr);
        
        if (matched) {
            return false;
        }
        
        inputStream.setPosition(originalPosition);
        return true;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append('~');
        expr.toString(sb);
    }
}
