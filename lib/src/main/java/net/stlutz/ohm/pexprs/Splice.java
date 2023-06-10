package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;

/**
 * Splice is an implementation detail of rule overriding with the `...` operator.
 */
public class Splice extends PExpr {
    private PExpr superExpr;
    
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
    public PExpr introduceParams(String[] formals) {
        superExpr = superExpr.introduceParams(formals);
        return this;
    }
    
    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return new Splice(superExpr.substituteParams(actuals));
    }
    
    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        return matchState.eval(superExpr);
    }
    
    @Override
    public void toString(StringBuilder sb) {
        // TODO Auto-generated method stub
    }
}
