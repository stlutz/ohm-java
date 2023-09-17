package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

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
    public PExpr substituteParams(PExpr[] actuals) {
        return new Lookahead(expr.substituteParams(actuals));
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitLookahead(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (evalContext.eval(expr)) {
            inputStream.setPosition(originalPosition);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void toFailureDescription(StringBuilder sb) {
        expr.toFailureDescription(sb);
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append('&');
        expr.toString(sb);
    }
}
