package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.TerminalNode;

public class End extends Prim {
    private static final End instance = new End();
    
    public static final End getInstance() {
        return instance;
    }
    
    private End() {
        super();
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitEnd(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (inputStream.atEnd()) {
            evalContext.pushBinding(TerminalNode.get(0), originalPosition);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("end");
    }
}
