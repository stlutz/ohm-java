package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.IterationNode;
import net.stlutz.ohm.OhmException;
import net.stlutz.ohm.ParseNode;

public abstract class Iter extends PExpr {
    public PExpr expr;
    
    public abstract char getOperator();
    
    public abstract int getMinNumMatches();
    
    public abstract int getMaxNumMatches();
    
    boolean isOptional() {
        return false;
    }
    
    protected abstract Iter newInstance(PExpr expr);
    
    @Override
    public boolean allowsSkippingPrecedingSpace() {
        return false;
    }
    
    @Override
    public int getArity() {
        return expr.getArity();
    }
    
    @Override
    public PExpr substituteParams(PExpr[] actuals) {
        return newInstance(expr.substituteParams(actuals));
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        int arity = getArity();
        
        int numMatches = 0;
        int previousPosition = originalPosition;
        int maxNumMatches = getMaxNumMatches();
        while (numMatches < maxNumMatches && evalContext.eval(expr)) {
            if (inputStream.getPosition() == previousPosition) {
                // TODO: proper error handling
                throw new OhmException("Kleene expression has nullable operand");
            }
            previousPosition = inputStream.getPosition();
            numMatches++;
        }
        
        if (numMatches < getMinNumMatches()) {
            return false;
        }
        
        int offset = evalContext.positionToOffset(originalPosition);
        int matchLength = 0;
        int numBindings = numMatches * arity;
        ParseNode[] bindings = evalContext.spliceLastBindings(numBindings);
        int[] bindingOffsets = evalContext.spliceLastBindingOffsets(numBindings);
        
        if (numMatches > 0) {
            offset = bindingOffsets[0];
            int endOffset = bindingOffsets[numBindings - 1] + bindings[numBindings - 1].getMatchLength();
            matchLength = endOffset - offset;
        }
        
        int position = evalContext.offsetToPosition(offset);
        for (int columnIndex = 0; columnIndex < arity; columnIndex++) {
            ParseNode[] column = new ParseNode[numMatches];
            int[] columnOffsets = new int[numMatches];
            for (int rowIndex = 0; rowIndex < numMatches; rowIndex++) {
                int index = rowIndex * arity + columnIndex;
                column[rowIndex] = bindings[index];
                columnOffsets[rowIndex] = bindingOffsets[index];
            }
            IterationNode child = new IterationNode(matchLength, column, columnOffsets, isOptional());
            evalContext.pushBinding(child, position);
        }
        
        return true;
    }
    
    @Override
    public void toFailureDescription(StringBuilder sb) {
        sb.append("(");
        expr.toFailureDescription(sb);
        sb.append(getOperator());
        sb.append(")");
    }
    
    @Override
    public void toString(StringBuilder sb) {
        expr.toString(sb);
        sb.append(getOperator());
    }
}
