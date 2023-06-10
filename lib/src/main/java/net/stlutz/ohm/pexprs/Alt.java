package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;

public class Alt extends Aggregation {
    public Alt(PExpr[] terms) {
        super(terms);
    }

    @Override
    protected Aggregation newInstance(PExpr[] terms) {
        return new Alt(terms);
    }

    @Override
    public int getArity() {
        // This is ok because all terms must have the same arity
        // (checked by the Grammar constructor)
        return terms.length > 0 ? terms[0].getArity() : 0;
    }

    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        for (PExpr term : terms) {
            if (matchState.eval(term)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAlternation() {
        return true;
    }

    @Override
    protected String getOperator() {
        return " | ";
    }
}
