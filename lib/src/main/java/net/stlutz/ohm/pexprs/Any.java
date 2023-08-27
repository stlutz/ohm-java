package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.TerminalNode;

public class Any extends Prim {
    private static final Any instance = new Any();
    
    public static Any getInstance() {
        return instance;
    }
    
    private Any() {
        super();
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (inputStream.atEnd()) {
            return false;
        } else {
            inputStream.advance(1);
            evalContext.pushBinding(TerminalNode.get(1), originalPosition);
            return true;
        }
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("any");
    }
}
