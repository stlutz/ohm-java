package net.stlutz.ohm.pexprs;

public interface PExprVisitor<T> {
    default T visit(PExpr expr) {
        return expr.accept(this);
    }
    
    // Composites
    
    T visitAlt(Alt expr);
    
    T visitAny(Any expr);
    
    T visitApply(Apply expr);
    
    T visitCaseInsensitiveTerminal(CaseInsensitiveTerminal expr);
    
    T visitEnd(End expr);
    
    T visitExtend(Extend expr);
    
    T visitLex(Lex expr);
    
    T visitLookahead(Lookahead expr);
    
    T visitNot(Not expr);
    
    T visitOpt(Opt expr);
    
    T visitParam(Param expr);
    
    T visitPlus(Plus expr);
    
    T visitRange(Range expr);
    
    T visitSeq(Seq expr);
    
    T visitSplice(Splice expr);
    
    T visitStar(Star expr);
    
    T visitTerminal(Terminal expr);
    
    T visitUnicodeChar(UnicodeChar expr);
}
