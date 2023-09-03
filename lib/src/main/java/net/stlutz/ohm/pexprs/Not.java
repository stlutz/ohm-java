package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

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
    public PExpr substituteParams(PExpr[] actuals) {
        return new Not(expr.substituteParams(actuals));
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitNot(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        // TODO: Right now we're just throwing away all of the failures that happen
        // inside a `not`, and recording `this` as a failed expression.
        // TODO: Double negation should be equivalent to lookahead, but that's not the
        // case right now wrt failures. E.g., ~~'foo' produces a failure for ~~'foo',
        // but maybe it should produce a failure for 'foo' instead.
        boolean matched = evalContext.eval(expr);
        
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
