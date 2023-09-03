package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MemoizationRecord;
import net.stlutz.ohm.NonterminalNode;
import net.stlutz.ohm.OhmException;
import net.stlutz.ohm.ParseNode;
import net.stlutz.ohm.PositionInfo;
import net.stlutz.ohm.Rule;
import net.stlutz.ohm.Util;

public class Apply extends PExpr {
    public String ruleName;
    private PExpr[] args;
    
    /**
     * Caches the result of {@code this.toString()}.
     */
    private String memoKey;
    
    public Apply(String ruleName) {
        this(ruleName, new PExpr[0]);
    }
    
    public Apply(String ruleName, PExpr[] args) {
        super();
        this.ruleName = ruleName;
        this.args = args;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public PExpr[] getArgs() {
        return args;
    }
    
    public PExpr getArg(int index) {
        return args[index];
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
    public PExpr substituteParams(PExpr[] actuals) {
        if (args.length == 0) {
            return this;
        }
        
        PExpr[] substituted = new PExpr[args.length];
        for (int i = 0; i < args.length; i++) {
            substituted[i] = args[i].substituteParams(actuals);
        }
        return new Apply(ruleName, substituted);
    }
    
    @Override
    public <T> T accept(PExprVisitor<T> visitor) {
        return visitor.visitApply(this);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        Apply caller = evalContext.currentApplication();
        PExpr[] actuals = (caller != null) ? caller.args : new PExpr[0];
        Apply app = (Apply) substituteParams(actuals);
        
        PositionInfo posInfo = evalContext.getCurrentPositionInfo();
        if (posInfo.isActive(app)) {
            // This rule is already active at this position, i.e. it's left-recursive
            return app.handleCycle(evalContext, inputStream);
        }
        
        String memoKey = app.toMemoKey();
        MemoizationRecord memoRec = posInfo.remember(memoKey);
        
        if (memoRec != null && posInfo.shouldUseMemoizedResult(memoRec)) {
            if (evalContext.hasNecessaryInfo(memoRec)) {
                return evalContext.useMemoizedResult(inputStream.getPosition(), memoRec);
            }
            posInfo.forget(memoKey);
        }
        
        return app.reallyEval(evalContext, inputStream, inputStream.getPosition());
    }
    
    private boolean handleCycle(EvalContext evalContext, InputStream inputStream) {
        PositionInfo posInfo = evalContext.getCurrentPositionInfo();
        MemoizationRecord currentLeftRecursion = posInfo.getCurrentLeftRecursion();
        String memoKey = toMemoKey();
        MemoizationRecord memoRec = posInfo.remember(memoKey);
        
        if (currentLeftRecursion != null
            && currentLeftRecursion.getHeadApplication().toMemoKey().equals(memoKey)) {
            // We already know about this left recursion, but it's possible there are
            // "involved applications" that we don't already know about, so...
            memoRec.updateInvolvedApplicationMemoKeys();
        } else if (memoRec == null) {
            // New left recursion detected! Memoize a failure to try to get a seed parse.
            memoRec = posInfo.memoize(memoKey);
            posInfo.startLeftRecursion(this, memoRec);
        }
        
        return evalContext.useMemoizedResult(inputStream.getPosition(), memoRec);
    }
    
    private boolean reallyEval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        PositionInfo origPosInfo = evalContext.getCurrentPositionInfo();
        // TODO: bake rule body into apply node?
        Rule rule = evalContext.getRule(ruleName);
        if (rule == null) {
            throw new OhmException("No rule '%s' found".formatted(ruleName));
        }
        
        evalContext.enterApplication(origPosInfo, this);
        
        ParseNode nodeOrNull = evalOnce(rule.getBody(), evalContext);
        MemoizationRecord currentLR = origPosInfo.getCurrentLeftRecursion();
        String memoKey = toMemoKey();
        boolean isHeadOfLeftRecursion =
            (currentLR != null) && (currentLR.getHeadApplication().toMemoKey().equals(memoKey));
        
        MemoizationRecord memoRec;
        if (isHeadOfLeftRecursion) {
            nodeOrNull =
                growSeedResult(rule.getBody(), evalContext, originalPosition, currentLR, nodeOrNull);
            origPosInfo.endLeftRecursion();
            memoRec = currentLR;
            origPosInfo.memoize(memoKey, memoRec);
        } else if ((currentLR == null) || (!currentLR.isInvolved(memoKey))) {
            // This application is not involved in left recursion, so it's ok to memoize it
            memoRec = new MemoizationRecord();
            memoRec.setMatchLength(inputStream.getPosition() - originalPosition);
            memoRec.setValue(nodeOrNull);
            memoRec = origPosInfo.memoize(memoKey, memoRec);
        }
        
        evalContext.exitApplication(origPosInfo, nodeOrNull);
        
        return nodeOrNull != null;
    }
    
    private ParseNode evalOnce(PExpr body, EvalContext evalContext) {
        InputStream inputStream = evalContext.getInputStream();
        int originalPosition = inputStream.getPosition();
        
        if (evalContext.eval(body)) {
            int arity = body.getArity();
            ParseNode[] bindings = evalContext.spliceLastBindings(arity);
            int[] offsets = evalContext.spliceLastBindingOffsets(arity);
            int matchLength = inputStream.getPosition() - originalPosition;
            return new NonterminalNode(matchLength, ruleName, bindings, offsets);
        }
        
        return null;
    }
    
    private ParseNode growSeedResult(PExpr body, EvalContext evalContext, int originalPosition,
                                     MemoizationRecord lrMemoRec, ParseNode newValue) {
        if (newValue == null) {
            return null;
        }
        
        InputStream inputStream = evalContext.getInputStream();
        
        while (true) {
            lrMemoRec.setMatchLength(inputStream.getPosition() - originalPosition);
            lrMemoRec.setValue(newValue);
            inputStream.setPosition(originalPosition);
            newValue = evalOnce(body, evalContext);
            if (inputStream.getPosition() - originalPosition <= lrMemoRec.getMatchLength()) {
                break;
            }
        }
        
        inputStream.setPosition(originalPosition + lrMemoRec.getMatchLength());
        return lrMemoRec.getValue();
    }
    
    public boolean isSyntactic() {
        return Util.isSyntactic(ruleName);
    }
    
    public String toMemoKey() {
        // TODO: rename variable and function
        if (memoKey == null) {
            memoKey = toString();
        }
        return memoKey;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append(ruleName);
        
        if (args.length == 0) {
            return;
        }
        
        sb.append('<');
        boolean isFirst = true;
        for (PExpr arg : args) {
            if (!isFirst) {
                sb.append(',');
            }
            arg.toString(sb);
            isFirst = false;
        }
        sb.append('>');
    }
    
    @Override
    public void toDisplayString(StringBuilder sb) {
        // TODO: Almost identical to toString()
        sb.append(ruleName);
        
        if (args.length == 0) {
            return;
        }
        
        sb.append('<');
        boolean isFirst = true;
        for (PExpr arg : args) {
            if (!isFirst) {
                sb.append(',');
            }
            arg.toDisplayString(sb);
            isFirst = false;
        }
        sb.append('>');
    }
}
