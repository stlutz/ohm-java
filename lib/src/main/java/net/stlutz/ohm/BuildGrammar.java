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
    
    public void reset() {
        builder = null;
        currentGrammar = null;
        currentRule = null;
    }
    
    public PExpr build(Node node) {
        return (PExpr) apply(node);
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
        String grammarName = (String) apply(id);
        currentGrammar = builder.newGrammar(grammarName);
        if (superGrammarOpt.hasChildren()) {
            String superGrammarName = (String) apply(superGrammarOpt.onlyChild());
            currentGrammar.extend(superGrammarName);
        }
        for (var ruleNode : rulesIter.getChildren()) {
            apply(ruleNode);
        }
        currentGrammar.sourceInterval(self.getSource().trimmed());
    }
    
    @Action
    public String SuperGrammar(Node op, Node node) {
        return (String) apply(node);
    }
    
    private void newRule(Node ident) {
        String ruleName = (String) apply(ident);
        currentRule = currentGrammar.newRule(ruleName);
        currentRule.sourceInterval(self.getSource().trimmed());
    }
    
    private void setRuleFormals(Node formalsOpt) {
        if (formalsOpt.hasChildren()) {
            String[] formals = (String[]) apply(formalsOpt.onlyChild());
            currentRule.formals(formals);
        }
    }
    
    private void setRuleDescription(Node ruleDescrOpt) {
        if (ruleDescrOpt.hasChildren()) {
            String ruleDescr = (String) apply(ruleDescrOpt.onlyChild());
            currentRule.description(ruleDescr);
        }
    }
    
    private void setRuleBody(Node bodyNode) {
        PExpr body = (PExpr) apply(bodyNode);
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
        Node[] nodes = (Node[]) apply(list);
        var formals = new String[nodes.length];
        for (int i = 0; i < formals.length; i++) {
            formals[i] = (String) apply(nodes[i]);
        }
        return formals;
    }
    
    @Action
    @Action("OverrideRuleBody")
    public PExpr RuleBody(Node op, Node termsList) {
        Node[] termNodes = (Node[]) apply(termsList);
        PExpr[] terms = new PExpr[termNodes.length];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = build(termNodes[i]);
        }
        return withSource(PExpr.alt(terms));
    }
    
    @Action
    public PExpr TopLevelTerm_inline(Node bodyNode, Node nameNode) {
        String caseName = (String) apply(nameNode);
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
        Node[] termNodes = (Node[]) apply(list);
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
        var paramNodes = (Node[]) apply(list);
        PExpr[] params = new PExpr[paramNodes.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = build(paramNodes[i]);
        }
        return params;
    }
    
    @Action
    public PExpr Base_application(Node identNode, Node paramsOpt) {
        String ruleName = (String) apply(identNode);
        PExpr[] parameters =
            paramsOpt.hasChildren() ? (PExpr[]) apply(paramsOpt.onlyChild()) : new PExpr[0];
        PExpr result = PExpr.apply(ruleName, parameters);
        result.setSource(self.getSource());
        return result;
    }
    
    @Action
    public PExpr Base_range(Node from, Node op, Node to) {
        String rangeStart = (String) apply(from);
        String rangeEnd = (String) apply(to);
        PExpr result = range(rangeStart, rangeEnd);
        result.setSource(self.getSource());
        return result;
    }
    
    @Action
    public PExpr Base_terminal(Node expr) {
        String contents = (String) apply(expr);
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
        return (String) apply(name);
    }
    
    private String unescapedTerminal(SourceInterval chars) {
        return Util.unescapedSubstring(chars.getSourceString(), chars.getStartIndex(), chars.getEndIndex());
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
