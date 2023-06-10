package net.stlutz.ohm.pexprs;

public class Opt extends Iter {
    public static final char operator = '?';
    public static final int minNumMatches = 0;
    public static final int maxNumMatches = 1;

    public Opt(PExpr expr) {
        super();
        this.expr = expr;
    }

    @Override
    public char getOperator() {
        return operator;
    }

    @Override
    public int getMinNumMatches() {
        return minNumMatches;
    }

    @Override
    public int getMaxNumMatches() {
        return maxNumMatches;
    }

    @Override
    protected Iter newInstance(PExpr expr) {
        return new Opt(expr);
    }

    @Override
    boolean isOptional() {
        return true;
    }
}
