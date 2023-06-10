package net.stlutz.ohm.pexprs;

/**
 * Extend is an implementation detail of rule extension
 */
public class Extend extends Alt {
    public PExpr superBody;
    public PExpr body;

    public Extend(PExpr superBody, PExpr body) {
        super(new PExpr[]{body, superBody});
        this.superBody = superBody;
        this.body = body;
    }
}
