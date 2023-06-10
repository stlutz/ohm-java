package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.Apply;

import java.util.Map;

public interface Grammar {
    /**
     * Returns the default start rule name of this grammar (used in {@link Grammar#match(String)}.
     * <p>
     * Unless specifically set through {@link Grammar#setDefaultStartRule(String)}, the default start rule is the first in the following list:
     * <ul>
     *  <li>If this grammar has a super grammar with a non-{@code null} default start rule, the super grammar's default start rule is used,</li>
     *  <li>If this grammar has at least 1 rule, the first rule in this grammar's definition is used,</li>
     *  <li>Otherwise, it is {@code null} since there is no reasonable default.</li>
     * </ul>
     *
     * @return This grammar's default start rule name
     */
    String getDefaultStartRule();
    
    /**
     * Sets the default start rule name of this grammar (used in {@link Grammar#match(String)} to the specified value.
     * <p>
     * Overrides the previously set or implicitly determined value.
     *
     * @param defaultStartRule The rule name to use as the default start rule.
     * @throws IllegalArgumentException If {@code defaultStartRule} is {@code null}.
     * @throws OhmException If {@code defaultStartRule} is not a rule of this grammar.
     */
    void setDefaultStartRule(String defaultStartRule);
    
    /**
     * Returns the name of this grammar.
     *
     * @return This grammar's name.
     */
    String getName();
    
    /**
     * Returns the super grammar of this grammar.
     *
     * @return This grammar's super grammar, or {@code null} if it has none.
     */
    Grammar getSuperGrammar();
    
    /**
     * Returns this grammar's rule object for the rule named {@code ruleName}.
     * <p>
     * Rules defined by super grammars are considered rules of this grammar.
     *
     * @param ruleName The name of the rule.
     * @return The rule object, or {@code null} if this grammar does not have a rule of this name.
     */
    Rule getRule(String ruleName);
    
    /**
     * Returns all rules defined by this grammar or its super grammar as a map with the respective rule name as key.
     *
     * @return A map of rule names to rule objects for all rules in this grammar.
     */
    Map<String, Rule> getRules();
    
    /**
     * Returns {@code true} if this grammar or any super grammar contains a rule named {@code ruleName}.
     *
     * @param ruleName The name of the rule
     * @return {@code true} if this grammar or a super grammar can match {@code ruleName}, otherwise {@code false}
     */
    boolean hasRule(String ruleName);
    
    /**
     * Return a new {@link Matcher} object which supports incrementally matching this grammar against a changing input string.
     *
     * @param input The initial string to match against.
     * @return A new matcher object for this grammar.
     */
    Matcher getMatcher(String input);
    
    /**
     * Try to match {@code input} against this grammar, starting at the default start rule.
     * <p>
     * Roughly equivalent to {@code this.match(input, this.getDefaultStartRule())}
     *
     * @param input The string to be matched.
     * @return The match result, which contains the parse tree if successful.
     * @throws OhmException If {@link Grammar#getDefaultStartRule()} returns {@code null}.
     */
    MatchResult match(String input);
    
    /**
     * Try to match {@code input} against this grammar, starting at rule {@code startRule}.
     *
     * @param input The string to be matched.
     * @param startRule The rule to start matching with.
     * @return The match result, which contains the parse tree if successful.
     */
    MatchResult match(String input, String startRule);
    
    <T extends Semantics> SemanticsBlueprint<T> createSemanticsBlueprint(
        Class<T> semanticsClass);
    
    Apply parseApplication(String ruleName);
    
    /**
     * Returns whether this grammar is a built-in grammar.
     *
     * @return {@code true} if this grammar is built-in, otherwise {@code false}.
     */
    boolean isBuiltIn();
}
