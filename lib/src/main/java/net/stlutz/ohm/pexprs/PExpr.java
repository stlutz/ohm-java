package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.MatchState;
import net.stlutz.ohm.OhmException;
import net.stlutz.ohm.SourceInterval;

public abstract class PExpr {
    // TODO: rename to sourceInterval
    protected SourceInterval source;
    
    public PExpr() {
        super();
    }
    
    public PExpr(SourceInterval interval) {
        this();
        this.source = interval.trimmed();
    }
    
    public SourceInterval getSource() {
        return source;
    }
    
    public void setSource(SourceInterval source) {
        this.source = source;
    }
    
    public PExpr withSource(SourceInterval source) {
        this.source = source;
        return this;
    }
    
    public abstract boolean allowsSkippingPrecedingSpace();
    
    public abstract int getArity();
    
    /**
     * Returns a PExpr that results from recursively replacing every formal parameter (i.e., instance
     * of `Param`) inside this PExpr with its actual value from `actuals` (an Array).
     * <p>
     * The receiver must not be modified; a new PExpr must be returned if any replacement is
     * necessary.
     */
    public abstract PExpr substituteParams(PExpr[] actuals);
    
    /**
     * Called at grammar creation time to rewrite a rule body, replacing each reference to a formal
     * parameter with a `Param` node. Returns a PExpr -- either a new one, or the original one if it
     * was modified in place.
     */
    public abstract PExpr introduceParams(String[] formals);
    
    public void resolveSplice(PExpr superRuleBody) {
    }
    
    /**
     * Evaluate the expression and return `true` if it succeeds, `false` otherwise. This method should
     * only be called directly by `MatchState.eval(expr)`, which also updates the data structures that
     * are used for tracing. (Making those updates in a method of `MatchState` enables the
     * trace-specific data structures to be "secrets" of that class, which is good for modularity.)
     * <p>
     * The contract of this method is as follows: When the return value is `true`, the matcher will
     * have `expr.getArity()` more bindings than it did before the call. When the return value is
     * `false`, the matcher may have more bindings than it did before the call, and its input stream's
     * position may be anywhere.
     * <p>
     * Note that `MatchState.eval(expr)`, unlike this method, guarantees that neither the matcher's
     * bindings nor its input stream's position will change if the expression fails to match.
     *
     * @param matchState
     * @return
     */
    public abstract boolean eval(MatchState matchState, InputStream inputStream,
                                 int originalPosition);
    
    public boolean eval(MatchState matchState) {
        InputStream inputStream = matchState.getInputStream();
        return eval(matchState, inputStream, inputStream.getPosition());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
    
    public abstract void toString(StringBuilder sb);
    
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        toDisplayString(sb);
        return sb.toString();
    }
    
    public void toDisplayString(StringBuilder sb) {
        toString(sb);
    }
    
    // abstract void assertAllApplicationsAreValid(String ruleName, Grammar grammar);
    // abstract void assertChoicesHaveUniformArity(String ruleName);
    // abstract void assertIteratedExprsAreNotNullable(Grammar grammar);
    // public abstract boolean isNullable(Grammar grammar) {}
    // public abstract boolean isNullable(Grammar grammar, ? memo);
    // public abstract PExpr introduceParams(String[] formals);
    // public abstract PExpr substituteParams(PExpr[] actuals);
    // public abstract Failure toFailure(Grammar grammar);
    
    public boolean isSequence() {
        return false;
    }
    
    public boolean isAlternation() {
        return false;
    }
    
    public static PExpr alt(PExpr... terms) {
        // TODO: we could merge terms that are alternations directly into the terms,
        // however doing so modifies the resulting grammar, so a dedicated 'optimize'
        // method in Grammar might be preferable
        return new Alt(terms);
    }
    
    public static PExpr any() {
        return Any.getInstance();
    }
    
    public static PExpr apply(String ruleName, PExpr... params) {
        // TODO: change params type?
        return new Apply(ruleName, params);
    }
    
    public static PExpr end() {
        return End.getInstance();
    }
    
    // TODO: should this really be accessible?
    public static PExpr extend(PExpr superBody, PExpr body) {
        return new Extend(superBody, body);
    }
    
    public static PExpr lex(PExpr expr) {
        return new Lex(expr);
    }
    
    public static PExpr lookahead(PExpr expr) {
        return new Lookahead(expr);
    }
    
    public static PExpr none() {
        return seq();
    }
    
    public static PExpr not(PExpr expr) {
        return new Not(expr);
    }
    
    public static PExpr opt(PExpr expr) {
        return new Opt(expr);
    }
    
    public static PExpr param(int index) {
        // TODO: can we avoid leaking the param implementation to the outside world?
        return new Param(index);
    }
    
    public static PExpr plus(PExpr expr) {
        return new Plus(expr);
    }
    
    public static PExpr range(int fromCodePoint, int toCodePoint) {
        if (fromCodePoint > toCodePoint) {
            throw new OhmException(
                "Cannot create PExpr range. 'fromCodePoint' must not be higher than 'toCodePoint'.");
        }
        return new Range(fromCodePoint, toCodePoint);
    }
    
    public static PExpr range(String from, String to) {
        if (Character.codePointCount(from, 0, from.length()) != 1) {
            throw new OhmException(
                "Cannot create PExpr range. 'from' must contain exactly one code point.");
        }
        if (Character.codePointCount(to, 0, to.length()) != 1) {
            throw new OhmException(
                "Cannot create PExpr range. 'to' must contain exactly one code point.");
        }
        int fromCodePoint = from.codePointAt(0);
        int toCodePoint = to.codePointAt(0);
        return range(fromCodePoint, toCodePoint);
    }
    
    public static PExpr seq(PExpr... terms) {
        // TODO: see comment in alt
        return new Seq(terms);
    }
    
    public static PExpr splice() {
        return new Splice();
    }
    
    public static PExpr star(PExpr expr) {
        return new Star(expr);
    }
    
    public static PExpr terminal(String contents) {
        return new Terminal(contents);
    }
    
    public static PExpr terminal(String contents, boolean caseInsensitive) {
        return caseInsensitive ? new CaseInsensitiveTerminal(terminal(contents)) : terminal(contents);
    }
    
    public static PExpr unicodeChar(String unicodeCategory) {
        if (!UnicodeChar.unicodeCategoryPatterns.containsKey(unicodeCategory)) {
            throw new OhmException(
                "Cannot create PExpr unicodeChar. '%s' is not a valid unicode category."
                    .formatted(unicodeCategory));
        }
        return new UnicodeChar(unicodeCategory);
    }
}
