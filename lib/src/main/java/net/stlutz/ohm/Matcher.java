package net.stlutz.ohm;

public class Matcher {
    private final String input;
    private final Grammar grammar;
    private final PositionInfo[] memoTable;
    
    // private int positionToRecordFailures;
    // private Map<String, ?> recordedFailures;
    
    public Matcher(Grammar grammar, String input) {
        super();
        if (input == null)
            throw new OhmException("Cannot match against null string");
        
        this.grammar = grammar;
        this.input = input;
        memoTable = new PositionInfo[input.length() + 1];
    }
    
    public String getInput() {
        return input;
    }
    
    public Grammar getGrammar() {
        return grammar;
    }
    
    public PositionInfo[] getMemoTable() {
        return memoTable;
    }
    
    public MatchResult match(String startApplication) {
        if (startApplication == null) {
            throw new OhmException("Start application cannot be null");
        }
        
        MatchState matchState = new MatchState(this, grammar.parseApplication(startApplication));
        matchState.match();
        return matchState.getMatchResult();
    }
}
