package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;
import net.stlutz.ohm.TerminalNode;
import org.json.JSONWriter;

public class Range extends Prim {
    /**
     * {@code from} and {@code to} contain a Unicode code point each, describing an inclusive range of
     * Unicode characters.
     */
    public int from;
    public int to;
    
    public Range(int from, int to) {
        super();
        this.from = from;
        this.to = to;
    }
    
    public Range(String from, String to) {
        this(from.codePointAt(0), to.codePointAt(0));
    }
    
    @Override
    public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
        if (!inputStream.atEnd()) {
            int codePoint = inputStream.nextCodePoint();
            if (codePoint >= from && codePoint <= to) {
                matchState.pushBinding(TerminalNode.get(Character.charCount(codePoint)), originalPosition);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        JSONWriter writer = new JSONWriter(sb);
        writer.value(Character.toString(from));
        sb.append("..");
        writer.value(Character.toString(to));
    }
}
