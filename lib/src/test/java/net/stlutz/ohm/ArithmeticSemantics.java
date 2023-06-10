package net.stlutz.ohm;

import java.util.HashMap;

public class ArithmeticSemantics extends Semantics {
    
    static final HashMap<String, Double> constants = new HashMap<>();
    
    static {
        constants.put("pi", Math.PI);
        constants.put("e", Math.E);
    }
    
    public double interpret(Node node) {
        return (double) apply(node);
    }
    
    @Action
    public double AddExp_plus(Node x, Node op, Node y) {
        return interpret(x) + interpret(y);
    }
    
    @Action
    public double AddExp_minus(Node x, Node op, Node y) {
        return interpret(x) - interpret(y);
    }
    
    @Action
    public double MulExp_times(Node x, Node op, Node y) {
        return interpret(x) * interpret(y);
    }
    
    @Action
    public double MulExp_divide(Node x, Node op, Node y) {
        return interpret(x) / interpret(y);
    }
    
    @Action
    public double ExpExp_power(Node x, Node op, Node y) {
        return Math.pow(interpret(x), interpret(y));
    }
    
    @Action
    public double PriExp_paren(Node l, Node expr, Node r) {
        return interpret(expr);
    }
    
    @Action
    public double PriExp_pos(Node sign, Node expr) {
        return interpret(expr);
    }
    
    @Action
    public double PriExp_neg(Node sign, Node expr) {
        return -interpret(expr);
    }
    
    @Action
    public double ident(Node l, Node ns) {
        return constants.getOrDefault(self.sourceString(), 0.0);
    }
    
    @Action
    public double number(Node expr) {
        return Double.parseDouble(self.sourceString());
    }
}
