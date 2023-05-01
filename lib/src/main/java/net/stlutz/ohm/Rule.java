package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

public interface Rule {
    public enum Operation {
        DEFINE, EXTEND, OVERRIDE,
    }

    PExpr getBody();

    Operation getOperation();

    int getArity();

    String[] getFormals();

    String getDescription();

    boolean isDefinition();

    boolean isOverride();

    boolean isExtension();
}
