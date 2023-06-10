package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

import java.util.List;

import static net.stlutz.ohm.pexprs.PExpr.range;

public class BuildGrammar extends Semantics {
    public List<ConstructedGrammar> buildGrammars(Node node, Namespace namespace) {
        reset();
        builder = new ConstructedGrammarBuilder(namespace);
        apply(node);
        return builder.buildGrammars();
    }
    
    public ConstructedGrammar buildGrammar(Node node) {
        reset();
        builder = new ConstructedGrammarBuilder();
        apply(node);
        return builder.buildGrammar();
    }
    
    public PExpr buildPExpr(Node node) {
        return build(node);
    }
    
    private ConstructedGrammarBuilder builder;
    private GrammarDefinition currentGrammar;
    private RuleDefinition currentRule;
    
    private static final int backslashCodePoint = 92; // "\\".codePointAt(0);
    private static final int doubleQuoteCodePoint = 34; // "\"".codePointAt(0);
    private static final int singleQuoteCodePoint = 39; // "\'".codePointAt(0);
    private static final int backspaceCodePoint = 8; // "\b".codePointAt(0);
    private static final int lineFeedCodePoint = 10; // "\n".codePointAt(0);
    private static final int carriageReturnCodePoint = 13; // "\r".codePointAt(0);
    private static final int tabCodePoint = 9; // "\t".codePointAt(0);
    
    private String unescapedSubstring(String sourceString, int inclStart, int exclEnd) {
        // TODO: move to SourceInterval?
        // TODO: don't rely on correct input, handle errors
        var sb = new StringBuilder();
        int index = inclStart;
        while (index < exclEnd) {
            int codePoint = sourceString.codePointAt(index);
            if (codePoint == backslashCodePoint) {
                index++;
                char escapeChar = sourceString.charAt(index++);
                int actualCodePoint = switch (escapeChar) {
                    case '\\' -> backslashCodePoint;
                    case '"' -> doubleQuoteCodePoint;
                    case '\'' -> singleQuoteCodePoint;
                    case 'b' -> backspaceCodePoint;
                    case 'n' -> lineFeedCodePoint;
                    case 'r' -> carriageReturnCodePoint;
                    case 't' -> tabCodePoint;
                    case 'u' -> {
                        String hexDigits;
                        if (sourceString.charAt(index) == '{') {
                            int closingIndex = sourceString.indexOf('}', index + 2);
                            hexDigits = sourceString.substring(index + 1, closingIndex);
                            index = closingIndex + 1;
                        } else {
                            hexDigits = sourceString.substring(index, index + 4);
                            index += 4;
                        }
                        yield Integer.parseInt(hexDigits, 16);
                    }
                    case 'x' -> {
                        String hexDigits = sourceString.substring(index, index + 2);
                        index += 2;
                        yield Integer.parseInt(hexDigits, 16);
                    }
                    default -> {
                        throw new OhmException("Internal error. Unknown escape sequence.");
                    }
                };
                sb.appendCodePoint(actualCodePoint);
            } else {
                index += Character.charCount(codePoint);
                sb.appendCodePoint(codePoint);
            }
        }
        return sb.toString();
    }
    
    public void reset() {
        builder = null;
        currentGrammar = null;
        currentRule = null;
    }
    
    public PExpr build(Node node) {
        return PExpr.class.cast(apply(node));
    }
    
    public PExpr withSource(PExpr expr) {
        expr.setSource(self.getSource());
        return expr;
    }
    
    @Action
    public void Grammars(Node grammarIter) {
        for (var child : grammarIter.getChildren()) {
            apply(child);
        }
    }
    
    @Action
    public void Grammar(Node id, Node superGrammarOpt, Node left, Node rulesIter, Node right) {
        String grammarName = String.class.cast(apply(id));
        currentGrammar = builder.newGrammar(grammarName);
        if (superGrammarOpt.hasChildren()) {
            String superGrammarName = String.class.cast(apply(superGrammarOpt.onlyChild()));
            currentGrammar.extend(superGrammarName);
        }
        for (var ruleNode : rulesIter.getChildren()) {
            apply(ruleNode);
        }
        currentGrammar.sourceInterval(self.getSource().trimmed());
    }
    
    @Action
    public String SuperGrammar(Node op, Node node) {
        return String.class.cast(apply(node));
    }
    
    private void newRule(Node ident) {
        String ruleName = String.class.cast(apply(ident));
        currentRule = currentGrammar.newRule(ruleName);
        currentRule.sourceInterval(self.getSource().trimmed());
    }
    
    private void setRuleFormals(Node formalsOpt) {
        if (formalsOpt.hasChildren()) {
            String[] formals = (String[]) String.class.arrayType().cast(apply(formalsOpt.onlyChild()));
            currentRule.formals(formals);
        }
    }
    
    private void setRuleDescription(Node ruleDescrOpt) {
        if (ruleDescrOpt.hasChildren()) {
            String ruleDescr = String.class.cast(apply(ruleDescrOpt.onlyChild()));
            currentRule.description(ruleDescr);
        }
    }
    
    private void setRuleBody(Node bodyNode) {
        PExpr body = PExpr.class.cast(apply(bodyNode));
        currentRule.body(body);
    }
    
    @Action
    public void Rule_define(Node ident, Node formalsOpt, Node ruleDescrOpt, Node op,
                            Node bodyNode) {
        newRule(ident);
        currentRule.define();
        setRuleDescription(ruleDescrOpt);
        setRuleFormals(formalsOpt);
        setRuleBody(bodyNode);
    }
    
    @Action
    public void Rule_override(Node ident, Node formalsOpt, Node op, Node bodyNode) {
        newRule(ident);
        currentRule.override();
        setRuleFormals(formalsOpt);
        setRuleBody(bodyNode);
    }
    
    @Action
    public void Rule_extend(Node ident, Node formalsOpt, Node op, Node bodyNode) {
        newRule(ident);
        currentRule.extend();
        setRuleFormals(formalsOpt);
        setRuleBody(bodyNode);
    }
    
    @Action
    public String[] Formals(Node left, Node list, Node right) {
        Node[] nodes = (Node[]) Node.class.arrayType().cast(apply(list));
        var formals = new String[nodes.length];
        for (int i = 0; i < formals.length; i++) {
            formals[i] = String.class.cast(apply(nodes[i]));
        }
        return formals;
    }
    
    @Action
    @Action("OverrideRuleBody")
    public PExpr RuleBody(Node op, Node termsList) {
        Node[] termNodes = (Node[]) Node.class.arrayType().cast(apply(termsList));
        PExpr[] terms = new PExpr[termNodes.length];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = build(termNodes[i]);
        }
        return withSource(PExpr.alt(terms));
    }
    
    @Action
    public PExpr TopLevelTerm_inline(Node bodyNode, Node nameNode) {
        String caseName = String.class.cast(apply(nameNode));
        String inlineRuleName = currentRule.name + "_" + caseName;
        
        RuleDefinition inlineRule = currentGrammar.newRule(inlineRuleName);
        inlineRule.sourceInterval(self.getSource().trimmed());
        inlineRule.formals(currentRule.formals);
        PExpr body = build(bodyNode);
        inlineRule.body(body);
        
        PExpr[] params = new PExpr[currentRule.formals.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = PExpr.apply(currentRule.formals[i]);
        }
        PExpr result = PExpr.apply(inlineRuleName, params);
        result.setSource(body.getSource());
        return result;
    }
    
    @Action
    public PExpr OverrideTopLevelTerm_superSplice(Node op) {
        return withSource(PExpr.splice());
    }
    
    @Action
    public PExpr Alt(Node list) {
        // TODO: avoid code duplication with Seq
        Node[] termNodes = (Node[]) Node.class.arrayType().cast(apply(list));
        PExpr[] terms = new PExpr[termNodes.length];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = build(termNodes[i]);
        }
        return withSource(PExpr.alt(terms));
    }
    
    @Action
    public PExpr Seq(Node node) {
        PExpr[] terms = new PExpr[node.numChildren()];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = build(node.childAt(i));
        }
        return withSource(PExpr.seq(terms));
    }
    
    @Action
    public PExpr Iter_star(Node node, Node op) {
        return withSource(PExpr.star(build(node)));
    }
    
    @Action
    public PExpr Iter_plus(Node node, Node op) {
        return withSource(PExpr.plus(build(node)));
    }
    
    @Action
    public PExpr Iter_opt(Node node, Node op) {
        return withSource(PExpr.opt(build(node)));
    }
    
    @Action
    public PExpr Pred_not(Node op, Node node) {
        return withSource(PExpr.not(build(node)));
    }
    
    @Action
    public PExpr Pred_lookahead(Node op, Node node) {
        return withSource(PExpr.lookahead(build(node)));
    }
    
    @Action
    public PExpr Lex_lex(Node op, Node node) {
        return withSource(PExpr.lex(build(node)));
    }
    
    @Action
    public PExpr[] Params(Node left, Node list, Node right) {
        // TODO: avoid boilerplate
        var paramNodes = (Node[]) Node.class.arrayType().cast(apply(list));
        PExpr[] params = new PExpr[paramNodes.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = build(paramNodes[i]);
        }
        return params;
    }
    
    @Action
    public PExpr Base_application(Node identNode, Node paramsOpt) {
        String ruleName = String.class.cast(apply(identNode));
        PExpr[] parameters =
                paramsOpt.hasChildren() ? (PExpr[]) apply(paramsOpt.onlyChild()) : new PExpr[0];
        PExpr result = PExpr.apply(ruleName, parameters);
        result.setSource(self.getSource());
        return result;
    }
    
    @Action
    public PExpr Base_range(Node from, Node op, Node to) {
        String rangeStart = String.class.cast(apply(from));
        String rangeEnd = String.class.cast(apply(to));
        PExpr result = range(rangeStart, rangeEnd);
        result.setSource(self.getSource());
        return result;
    }
    
    @Action
    public PExpr Base_terminal(Node expr) {
        String contents = String.class.cast(apply(expr));
        PExpr result = PExpr.terminal(contents);
        result.setSource(self.getSource());
        return result;
    }
    
    @Action
    public Object Base_paren(Node open, Node expr, Node close) {
        return apply(expr);
    }
    
    @Action
    public String ruleDescr(Node open, Node text, Node close) {
        return text.sourceString().trim();
    }
    
    @Action
    public String caseName(Node op, Node space1, Node name, Node space2, Node end) {
        return String.class.cast(apply(name));
    }
    
    private String unescapedTerminal(SourceInterval chars) {
        return unescapedSubstring(chars.getSourceString(), chars.getStartIndex(),
                chars.getEndIndex());
    }
    
    @Action
    public String terminal(Node open, Node chars, Node close) {
        return unescapedTerminal(chars.getSource());
    }
    
    @Action
    public Object oneCharTerminal(Node open, Node oneChar, Node close) {
        return unescapedTerminal(oneChar.getSource());
    }
    
    @Action
    public Node[] NonemptyListOf(Node first, Node sepIter, Node restIter) {
        var result = new Node[restIter.numChildren() + 1];
        result[0] = first;
        for (int i = 0; i < restIter.numChildren(); i++) {
            result[i + 1] = restIter.childAt(i);
        }
        return result;
    }
    
    @Action
    public Node[] EmptyListOf() {
        return new Node[0];
    }
    
    @Action("name")
    @Action(SpecialActionNames.terminal)
    public String sourceString(Node[] nodes) {
        return self.sourceString();
    }
}
