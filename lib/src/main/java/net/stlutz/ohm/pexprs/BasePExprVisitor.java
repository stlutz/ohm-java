package net.stlutz.ohm.pexprs;

import java.util.function.BiConsumer;

public class BasePExprVisitor<T> implements PExprVisitor<T> {
    protected void traverseAggregation(Aggregation expr, BiConsumer<PExpr, T> resultHandler) {
        for (PExpr term : expr.getTerms()) {
            resultHandler.accept(term, visit(term));
        }
    }
    
    protected T visitIterChild(Iter expr) {
        return visit(expr.expr);
    }
    
    protected T visitLexChild(Lex expr) {
        return visit(expr.expr);
    }
    
    protected T visitLookaheadChild(Lookahead expr) {
        return visit(expr.expr);
    }
    
    protected T visitNotChild(Not expr) {
        return visit(expr.expr);
    }
    
    protected T visitSpliceChild(Splice expr) {
        return visit(expr.superExpr);
    }
    
    
    // Composites
    
    @Override
    public T visitAlt(Alt expr) {
        traverseAggregation(expr, (term, result) -> {});
        return null;
    }
    
    @Override
    public T visitExtend(Extend expr) {
        traverseAggregation(expr, (term, result) -> {});
        return null;
    }
    
    @Override
    public T visitLex(Lex expr) {
        return visitLexChild(expr);
    }
    
    @Override
    public T visitLookahead(Lookahead expr) {
        return visitLookaheadChild(expr);
    }
    
    @Override
    public T visitNot(Not expr) {
        return visitNotChild(expr);
    }
    
    @Override
    public T visitOpt(Opt expr) {
        return visitIterChild(expr);
    }
    
    @Override
    public T visitPlus(Plus expr) {
        return visitIterChild(expr);
    }
    
    @Override
    public T visitSeq(Seq expr) {
        traverseAggregation(expr, (term, result) -> {});
        return null;
    }
    
    @Override
    public T visitSplice(Splice expr) {
        return visitSpliceChild(expr);
    }
    
    @Override
    public T visitStar(Star expr) {
        return visitIterChild(expr);
    }
    
    
    // Leaves
    
    @Override
    public T visitAny(Any expr) {
        return null;
    }
    
    @Override
    public T visitApply(Apply expr) {
        return null;
    }
    
    @Override
    public T visitCaseInsensitiveTerminal(CaseInsensitiveTerminal expr) {
        return null;
    }
    
    @Override
    public T visitEnd(End expr) {
        return null;
    }
    
    @Override
    public T visitParam(Param expr) {
        return null;
    }
    
    @Override
    public T visitRange(Range expr) {
        return null;
    }
    
    @Override
    public T visitTerminal(Terminal expr) {
        return null;
    }
    
    @Override
    public T visitUnicodeChar(UnicodeChar expr) {
        return null;
    }
}
