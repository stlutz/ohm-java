package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.OhmException;
import net.stlutz.ohm.TerminalNode;

public class CaseInsensitiveTerminal extends PExpr {
    
    private final PExpr param;
    
    public CaseInsensitiveTerminal(PExpr param) {
        super();
        this.param = param;
    }
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return true;
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
        return new CaseInsensitiveTerminal(param.substituteParams(actuals));
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitCaseInsensitiveTerminal(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (!(param instanceof Terminal terminal)) {
            throw new OhmException("Expected a Terminal expression");
        }
        String matchStr = terminal.getString();
        
        if (inputStream.matches(matchStr, true)) {
            inputStream.advance(matchStr.length());
            evalContext.pushBinding(TerminalNode.get(matchStr.length()), originalPosition);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        param.toString(sb);
        sb.append(" (case-insensitive)");
    }
}
