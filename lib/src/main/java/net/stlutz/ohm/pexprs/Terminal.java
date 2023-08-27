package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.TerminalNode;
import net.stlutz.ohm.Util;

public class Terminal extends Prim {
    // TODO: rename 'obj' to something more meaningful
    private String obj;
    
    public Terminal(String obj) {
        super();
        this.obj = obj;
    }
    
    public String getString() {
        return obj;
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (inputStream.matches(obj)) {
            inputStream.advance(obj.length());
            evalContext.pushBinding(TerminalNode.get(obj.length()), originalPosition);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append('"');
        Util.escapeString(obj, sb);
        sb.append('"');
    }
}
