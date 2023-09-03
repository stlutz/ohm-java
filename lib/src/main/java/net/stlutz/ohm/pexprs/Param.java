package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

// TODO: is this actually used anywhere?
public class Param extends PExpr {
    public int index;
    
    public Param(int index) {
        super();
        this.index = index;
    }
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return false;
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
        return actuals[index];
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitParam(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        // TODO: figure out what's happening here
        return evalContext.eval(evalContext.currentApplication().getArg(index));
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append('$');
        sb.append(index);
    }
}
