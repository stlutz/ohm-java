package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.*;
import org.json.JSONObject;

public class Terminal extends Prim {
    // TODO: rename
    private String obj;

    public Terminal(String obj) {
        super();
        this.obj = obj;
    }

    public String getString() {
        return obj;
    }

    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        if (inputStream.matches(obj)) {
            inputStream.advance(obj.length());
            matchState.pushBinding(TerminalNode.get(obj.length()), originalPosition);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(JSONObject.quote(obj));
    }
}
