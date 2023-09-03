package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

/**
 * Splice is an implementation detail of rule overriding with the `...` operator.
 */
public class Splice extends PExpr {
    PExpr superExpr;
    
    public Splice() {
        super();
    }
    
    public Splice(PExpr superExpr) {
        super();
        this.superExpr = superExpr;
    }
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return superExpr.allowsSkippingPrecedingSpace();
    }
    
    @Override
    public int getArity() {
        return superExpr.getArity();
    }
    
    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return new Splice(superExpr.substituteParams(actuals));
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitSplice(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        return evalContext.eval(superExpr);
    }
    
    @Override
    public void toString(StringBuilder sb) {
        // TODO Auto-generated method stub
    }
}
