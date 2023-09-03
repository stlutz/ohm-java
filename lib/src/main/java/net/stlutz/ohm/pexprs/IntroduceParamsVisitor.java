package net.stlutz.ohm.pexprs;

public class IntroduceParamsVisitor extends BasePExprVisitor<Void> {
    PExpr currentExpr = null;
    private final String[] formals;
    
    public IntroduceParamsVisitor(String[] formals) {
        this.formals = formals;
    }
    
    @Override
    public Void visit(PExpr expr) {
        PExpr previousExpr = currentExpr;
        currentExpr = expr;
        super.visit(expr);
        currentExpr = previousExpr;
        return null;
    }
    
    @Override
    public Void visitApply(Apply expr) {
        
        return null;
    }
}
