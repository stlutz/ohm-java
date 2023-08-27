package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Apply;
import net.stlutz.ohm.pexprs.End;
import net.stlutz.ohm.pexprs.EvalContext;
import net.stlutz.ohm.pexprs.PExpr;
import net.stlutz.ohm.pexprs.Seq;

import java.util.ArrayDeque;
import java.util.Deque;

public class MatchState implements EvalContext {
    private final Matcher matcher;
    // borrowed from matcher for easier access
    private final Grammar grammar;
    private final String input;
    private final PositionInfo[] memoTable;
    
    private final InputStream inputStream;
    private final PExpr startExpr;
    private final Apply startApplication;
    
    private final Deque<ParseNode> bindings = new ArrayDeque<>();
    private final Deque<Integer> bindingOffsets = new ArrayDeque<>();
    private final Deque<Apply> applicationStack = new ArrayDeque<>();
    private final Deque<Integer> positionStack = new ArrayDeque<>();
    private final Deque<Boolean> inLexifiedContextStack = new ArrayDeque<>();
    
    private static final Apply applySpaces = new Apply("spaces");
    
    // private int positionToRecordFailures;
    // private Map<String, ?> recordedFailures;
    
    public MatchState(Matcher matcher, Apply startApplication) {
        super();
        this.matcher = matcher;
        this.startApplication = startApplication;
        this.startExpr = getStartExpr(startApplication);
        
        input = matcher.getInput();
        grammar = matcher.getGrammar();
        memoTable = matcher.getMemoTable();
        
        inputStream = new InputStream(input);
        positionStack.addLast(0);
        inLexifiedContextStack.addLast(false);
    }
    
    // public Matcher(MatchTask matchTask, PExpr startExpr, int positionToRecordFailures) {
    // this(matchTask, startExpr);
    // this.positionToRecordFailures = positionToRecordFailures;
    // // TODO: allow for recorded failures
    // }
    
    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
    
    @Override
    public Rule getRule(String ruleName) {
        return grammar.getRule(ruleName);
    }
    
    private PExpr getStartExpr(Apply startApplication) {
        return new Seq(new PExpr[]{startApplication, End.getInstance()});
    }
    
    @Override
    public int positionToOffset(int position) {
        return position - positionStack.getLast();
    }
    
    @Override
    public int offsetToPosition(int offset) {
        return positionStack.getLast() + offset;
    }
    
    @Override
    public void enterApplication(PositionInfo positionInfo, Apply application) {
        positionStack.addLast(inputStream.getPosition());
        applicationStack.addLast(application);
        inLexifiedContextStack.addLast(false);
        positionInfo.enter(application);
    }
    
    @Override
    public void exitApplication(PositionInfo positionInfo, ParseNode nodeOrNull) {
        int originalPosition = positionStack.removeLast();
        applicationStack.removeLast();
        inLexifiedContextStack.removeLast();
        positionInfo.exit();
        
        if (nodeOrNull != null) {
            pushBinding(nodeOrNull, originalPosition);
        }
    }
    
    @Override
    public void enterLexifiedContext() {
        inLexifiedContextStack.addLast(true);
    }
    
    @Override
    public void exitLexifiedContext() {
        inLexifiedContextStack.removeLast();
    }
    
    @Override
    public Apply currentApplication() {
        return applicationStack.peekLast();
    }
    
    boolean inSyntacticContext() {
        Apply app = currentApplication();
        if (app == null) {
            // The top-level context is sytactic if the start application is
            app = startApplication;
        }
        return app.isSyntactic() && !inLexifiedContext();
    }
    
    boolean inLexifiedContext() {
        return inLexifiedContextStack.getLast();
    }
    
    int skipSpaces() {
        eval(applySpaces);
        popBinding();
        return inputStream.getPosition();
    }
    
    private int skipSpacesIfInSyntacticContext() {
        return inSyntacticContext() ? skipSpaces() : inputStream.getPosition();
    }
    
    private int maybeSkipSpacesBefore(PExpr expr) {
        if (expr.allowsSkippingPrecedingSpace() && !(expr.equals(applySpaces))) {
            return skipSpacesIfInSyntacticContext();
        } else {
            return inputStream.getPosition();
        }
    }
    
    @Override
    public ParseNode[] spliceLastBindings(int numBindings) {
        ParseNode[] result = new ParseNode[numBindings];
        for (int i = numBindings - 1; i >= 0; i--) {
            result[i] = bindings.removeLast();
        }
        return result;
    }
    
    @Override
    public int[] spliceLastBindingOffsets(int numBindingOffsets) {
        int[] result = new int[numBindingOffsets];
        for (int i = numBindingOffsets - 1; i >= 0; i--) {
            result[i] = bindingOffsets.removeLast();
        }
        return result;
    }
    
    @Override
    public void pushBinding(ParseNode node, int originalPosition) {
        bindings.addLast(node);
        bindingOffsets.addLast(positionToOffset(originalPosition));
    }
    
    public void popBinding() {
        bindings.removeLast();
        bindingOffsets.removeLast();
    }
    
    public int numBindings() {
        return bindings.size();
    }
    
    void truncateBindings(int newLength) {
        int bindingsToRemove = bindings.size() - newLength;
        for (int i = 0; i < bindingsToRemove; i++) {
            popBinding();
        }
    }
    
    @Override
    public PositionInfo getCurrentPositionInfo() {
        return getPositionInfo(inputStream.getPosition());
    }
    
    PositionInfo getPositionInfo(int position) {
        PositionInfo positionInfo = memoTable[position];
        if (positionInfo == null) {
            positionInfo = memoTable[position] = new PositionInfo();
        }
        return positionInfo;
    }
    
    @Override
    public boolean hasNecessaryInfo(MemoizationRecord memoRec) {
        return true;
    }
    
    @Override
    public boolean useMemoizedResult(int originalPosition, MemoizationRecord memoRec) {
        if (memoRec.getValue() != null) {
            inputStream.advance(memoRec.getMatchLength());
            pushBinding(memoRec.getValue(), originalPosition);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean eval(PExpr expr) {
        int originalNumBindings = numBindings();
        int originalPosition = inputStream.getPosition();
        int memoPosition = maybeSkipSpacesBefore(expr);
        
        boolean succeeded = expr.eval(this, inputStream, inputStream.getPosition());
        
        if (!succeeded) {
            inputStream.setPosition(originalPosition);
            truncateBindings(originalNumBindings);
        }
        
        return succeeded;
    }
    
    public void match() {
        eval(startExpr);
    }
    
    public MatchResult getMatchResult() {
        ParseNode cst = bindings.peekFirst();
        int cstOffset = bindingOffsets.isEmpty() ? 0 : bindingOffsets.getFirst();
        return new MatchResult(matcher, input, startApplication, cst, cstOffset);
    }
    
}
