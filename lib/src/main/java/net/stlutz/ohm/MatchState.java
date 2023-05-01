package net.stlutz.ohm;

import java.util.ArrayDeque;
import java.util.Deque;

import net.stlutz.ohm.pexprs.*;

public class MatchState {
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

    public InputStream getInputStream() {
        return inputStream;
    }

    public Rule getRule(String ruleName) {
        return grammar.getRule(ruleName);
    }

    private PExpr getStartExpr(Apply startApplication) {
        return new Seq(new PExpr[]{startApplication, End.getInstance()});
    }

    public int positionToOffset(int position) {
        return position - positionStack.getLast();
    }

    public int offsetToPosition(int offset) {
        return positionStack.getLast() + offset;
    }

    public void enterApplication(PositionInfo positionInfo, Apply application) {
        positionStack.addLast(inputStream.getPosition());
        applicationStack.addLast(application);
        inLexifiedContextStack.addLast(false);
        positionInfo.enter(application);
    }

    public void exitApplication(PositionInfo positionInfo, ParseNode nodeOrNull) {
        int originalPosition = positionStack.removeLast();
        applicationStack.removeLast();
        inLexifiedContextStack.removeLast();
        positionInfo.exit();

        if (nodeOrNull != null) {
            pushBinding(nodeOrNull, originalPosition);
        }
    }

    public void enterLexifiedContext() {
        inLexifiedContextStack.addLast(true);
    }

    public void exitLexifiedContext() {
        inLexifiedContextStack.removeLast();
    }

    public Apply currentApplication() {
        return applicationStack.peekLast();
    }

    public boolean inSyntacticContext() {
        Apply app = currentApplication();
        if (app == null) {
            // The top-level context is sytactic if the start application is
            app = startApplication;
        }
        return app.isSyntactic() && !inLexifiedContext();
    }

    public boolean inLexifiedContext() {
        return inLexifiedContextStack.getLast();
    }

    public int skipSpaces() {
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

    public ParseNode[] spliceLastBindings(int numBindings) {
        ParseNode[] result = new ParseNode[numBindings];
        for (int i = numBindings - 1; i >= 0; i--) {
            result[i] = bindings.removeLast();
        }
        return result;
    }

    public int[] spliceLastBindingOffsets(int numBindingOffsets) {
        int[] result = new int[numBindingOffsets];
        for (int i = numBindingOffsets - 1; i >= 0; i--) {
            result[i] = bindingOffsets.removeLast();
        }
        return result;
    }

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

    public void truncateBindings(int newLength) {
        int bindingsToRemove = bindings.size() - newLength;
        for (int i = 0; i < bindingsToRemove; i++) {
            popBinding();
        }
    }

    public PositionInfo getCurrentPositionInfo() {
        return getPositionInfo(inputStream.getPosition());
    }

    public PositionInfo getPositionInfo(int position) {
        PositionInfo positionInfo = memoTable[position];
        if (positionInfo == null) {
            positionInfo = memoTable[position] = new PositionInfo();
        }
        return positionInfo;
    }

    public boolean hasNecessaryInfo(MemoizationRecord memoRec) {
        return true;
    }

    public boolean useMemoizedResult(int originalPosition, MemoizationRecord memoRec) {
        if (memoRec.getValue() != null) {
            inputStream.advance(memoRec.getMatchLength());
            pushBinding(memoRec.getValue(), originalPosition);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Evaluate {@code expr} and return {@code true} if it succeeded, {@code false} otherwise. On
     * success, {@code bindings} will have {@code expr.getArity()} more elements than before, and the
     * input stream's position may have increased. On failure, {@code bindings} and position will be
     * unchanged.
     *
     * @param expr
     * @return
     */

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
