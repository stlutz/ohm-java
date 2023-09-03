package net.stlutz.ohm.pexprs;

public abstract class Prim extends PExpr {
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return true;
    }
    
    @Override
    public int getArity() {
        return 1;
    }
    
    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return this;
    }
}
