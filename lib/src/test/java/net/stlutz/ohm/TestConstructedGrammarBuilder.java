package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

public class TestConstructedGrammarBuilder {
    private ConstructedGrammarBuilder builder;
    private int validRuleNameIndex;
    private int validGrammarNameIndex;
    
    @BeforeEach
    void beforeEach() {
        validRuleNameIndex = 1;
        validGrammarNameIndex = 1;
        builder = new ConstructedGrammarBuilder();
    }
    
    @Test
    void testNoGrammarsSingle() {
        assertErrorMessage("No grammars defined.", () -> builder.buildGrammar());
    }
    
    @Test
    void testNoGrammarsMultiple() {
        assertBuildValidGrammars(0);
    }
    
    @Test
    void testOneGrammarSingle() {
        defineValidGrammar();
        Assertions.assertNotNull(builder.buildGrammar());
    }
    
    @Test
    void testOneGrammarMultiple() {
        defineValidGrammar();
        assertBuildValidGrammars(1);
    }
    
    @Test
    void testMultipleGrammarsSingle() {
        defineValidGrammar();
        defineValidGrammar();
        assertErrorMessage("More than one grammar defined.", () -> builder.buildGrammar());
    }
    
    @Test
    void testMultipleGrammarsMultiple() {
        defineValidGrammar();
        defineValidGrammar();
        defineValidGrammar();
        assertBuildValidGrammars(3);
    }
    
    
    @Test
    void testReuseBuilder() {
        defineValidGrammar();
        assertBuildValidGrammars(1);
        
        builder.clear();
        defineValidGrammar();
        defineValidGrammar();
        assertBuildValidGrammars(2);
    }
    
    @Test
    void testEmptyGrammar() {
        builder.newGrammar("myGrammar");
        assertBuildValidGrammars(1);
    }
    
    @Test
    void testNullGrammarName() {
        defineValidGrammar(null);
        assertBuildError("Grammar name must not be null.");
    }
    
    @Test
    void testBuiltInRulesGrammarName() {
        defineValidGrammar(ConstructedGrammar.BuiltInRules.getName());
        assertBuildValidGrammars(1);
    }
    
    @Test
    void testCircularGrammarInheritance() {
        defineValidGrammar("grammar1").extend("grammar2");
        defineValidGrammar("grammar2").extend("grammar3");
        defineValidGrammar("grammar3").extend("grammar4");
        defineValidGrammar("grammar4").extend("grammar1");
        defineValidGrammar("grammar5");
        assertBuildError("Either the specified super grammar was not found in the given namespace or grammar inheritance is circular.");
    }
    
    @Test
    void testResolvableGrammarInheritance() {
        defineValidGrammar("grammar5").extend("grammar3");
        defineValidGrammar("grammar4").extend("grammar2");
        defineValidGrammar("grammar3").extend("grammar1");
        defineValidGrammar("grammar2").extend("grammar1");
        defineValidGrammar("grammar1");
        assertBuildValidGrammars(5);
    }
    
    @Test
    void testUnknownSuperGrammar() {
        defineValidGrammar("grammar1").extend("grammar2");
        assertBuildError("Either the specified super grammar was not found in the given namespace or grammar inheritance is circular.");
    }
    
    @Test
    void testDuplicateGrammar() {
        defineValidGrammar("grammar1");
        defineValidGrammar("grammar1");
        assertBuildError("The grammar 'grammar1' was declared multiple times.");
    }
    
    @Test
    void testGrammarAlreadyInNamespace() {
        defineValidGrammar("grammar1");
        builder.buildGrammars();
        builder.clear();
        defineValidGrammar("grammar1");
        assertBuildError("The grammar 'grammar1' was already declared in the provided namespace.");
    }
    
    @Test
    void testNullRuleName() {
        defineValidGrammar().newRule(null).body(PExpr.any());
        assertBuildError("Rule name must not be null.");
    }
    
    @Test
    void testForbiddenRuleName() {
        defineValidGrammar().newRule("_nonterminal").body(PExpr.any());
        assertBuildError("Rule name '_nonterminal' is forbidden");
    }
    
    @Test
    void testDuplicateRuleName() {
        var grammarDefinition = defineValidGrammar();
        grammarDefinition.newRule("rule1").body(PExpr.any());
        grammarDefinition.newRule("rule1").body(PExpr.any());
        assertBuildError("Duplicate declaration for rule 'rule1'");
    }
    
    @Test
    void testNullRuleBody() {
        defineValidGrammar().newRule("rule1");
        assertBuildError("Rule body must not be null.");
    }
    
    @Test
    void testDuplicateParameterNames() {
        defineValidGrammar().newRule("rule1").body(PExpr.any()).formals("param1", "param1");
        assertBuildError("Duplicate parameter names in rule 'rule1': param1");
    }
    
    @Test
    void testOverrideRuleWithoutSuperGrammar() {
        defineValidGrammar().newRule("rule1").body(PExpr.any()).override();
        assertBuildError("Cannot override rule 'rule1'. No rule of the same name was found in a super grammar.");
    }
    
    @Test
    void testExtendRuleWithoutSuperGrammar() {
        defineValidGrammar().newRule("rule1").body(PExpr.any()).extend();
        assertBuildError("Cannot extend rule 'rule1'. No rule of the same name was found in a super grammar.");
    }
    
    @Test
    void testRedefineRuleFromSuperGrammar() {
        defineValidGrammar("grammar1").newRule("rule1").body(PExpr.any());
        defineValidGrammar().extend("grammar1").newRule("rule1").body(PExpr.any());
        assertBuildError("Rule 'rule1' was already declared in super grammar and must be explicitly overridden.");
    }
    
    GrammarDefinition defineValidGrammar() {
        return defineValidGrammar(getValidGrammarName());
    }
    
    GrammarDefinition defineValidGrammar(String grammarName) {
        var grammarDefinition = builder.newGrammar(grammarName);
        grammarDefinition.newRule(getValidRuleName()).body(PExpr.any());
        return grammarDefinition;
    }
    
    String getValidRuleName() {
        return "__validRule" + validRuleNameIndex++;
    }
    
    String getValidGrammarName() {
        return "__validGrammar" + validGrammarNameIndex++;
    }
    
    void assertBuildValidGrammars(int expectedNumGrammars) {
        int previousNamespaceSize = builder.getNamespace().size();
        assertValidGrammars(builder.buildGrammars(), expectedNumGrammars);
        Assertions.assertEquals(
            previousNamespaceSize + expectedNumGrammars, builder.getNamespace().size(),
            "Built grammars should be added to the namespace");
    }
    
    void assertValidGrammars(List<ConstructedGrammar> grammars, int expectedNumGrammars) {
        grammars.forEach(Assertions::assertNotNull);
        Assertions.assertEquals(
            expectedNumGrammars, grammars.size(),
            "Incorrect number of built grammars");
    }
    
    void assertBuildError(String expectedErrorMessage) {
        assertErrorMessage(expectedErrorMessage, () -> builder.buildGrammars());
    }
    
    void assertErrorMessage(String expectedErrorMessage, Executable executable) {
        Throwable e = Assertions.assertThrows(OhmException.class, executable);
        Assertions.assertEquals(expectedErrorMessage, e.getMessage());
    }
}
