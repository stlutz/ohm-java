package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;

public class Seq extends Aggregation {
    public Seq(PExpr[] terms) {
        super(terms);
    }
    
    @Override
    protected Aggregation newInstance(PExpr[] terms) {
        return new Seq(terms);
    }
    
    @Override
    public int getArity() {
        int arity = 0;
        for (PExpr term : terms) {
            arity += term.getArity();
        }
        return arity;
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitSeq(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        for (PExpr term : terms) {
            if (!evalContext.eval(term)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean isSequence() {
        return true;
    }
    
    @Override
    protected String getOperator() {
        return " ";
    }
}
