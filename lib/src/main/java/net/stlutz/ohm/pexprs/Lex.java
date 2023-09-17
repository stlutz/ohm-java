package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

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
    public PExpr substituteParams(PExpr[] actuals) {
        return new Lex(expr.substituteParams(actuals));
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitLex(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        evalContext.enterLexifiedContext();
        boolean succeeded = evalContext.eval(expr);
        evalContext.exitLexifiedContext();
        return succeeded;
    }
    
    @Override
    public void toFailureDescription(StringBuilder sb) {
        // TODO: It could be though, right? (e.g. within an iter)
        throw new InternalError("Lex::toFailureDescription should never be called");
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("#(");
        expr.toString(sb);
        sb.append(')');
    }
}
