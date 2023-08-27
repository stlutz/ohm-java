package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MemoizationRecord;
import net.stlutz.ohm.ParseNode;
import net.stlutz.ohm.PositionInfo;
import net.stlutz.ohm.Rule;

public interface EvalContext {
    
    InputStream getInputStream();
    
    /**
     * Evaluate {@code expr} and return {@code true} if it succeeded, {@code false} otherwise. On
     * success, {@code bindings} will have {@code expr.getArity()} more elements than before, and the
     * input stream's position may have increased. On failure, {@code bindings} and position will be
     * unchanged.
     */
    boolean eval(PExpr expr);
    
    void pushBinding(ParseNode node, int originalPosition);
    
    
    // Apply, Param, Iter
    
    Apply currentApplication();
    
    ParseNode[] spliceLastBindings(int numBindings);
    
    int[] spliceLastBindingOffsets(int numBindingOffsets);
    
    int positionToOffset(int position);
    
    int offsetToPosition(int offset);
    
    
    // Lex
    
    void enterLexifiedContext();
    
    void exitLexifiedContext();
    
    
    // Apply
    
    Rule getRule(String ruleName);
    
    void enterApplication(PositionInfo positionInfo, Apply application);
    
    void exitApplication(PositionInfo positionInfo, ParseNode nodeOrNull);
    
    boolean useMemoizedResult(int originalPosition, MemoizationRecord memoRec);
    
    PositionInfo getCurrentPositionInfo();
    
    boolean hasNecessaryInfo(MemoizationRecord memoRec);
}
