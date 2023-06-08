package net.stlutz.ohm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class OhmTest {
    String getGrammarSource(String fileName) {
        var path = Paths.get("src", "main", "grammars", fileName);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Ohm grammar source");
        }
    }

    Grammar getOhmGrammar() {
        return ConstructedGrammar.OhmGrammar;
    }

    String getOhmGrammarSource() {
        return getGrammarSource("ohm-grammar.ohm");
    }

    @Test
    void testOhmGrammarMatchesOhmGrammarSource() {
        assertTrue(getOhmGrammar().match(getOhmGrammarSource()).succeeded());
    }

    @Test
    void testBuildOhmGrammar() {
        var grammar = Ohm.grammar(getOhmGrammarSource());
        assertEquals("Grammars", grammar.getDefaultStartRule());
    }

    @Test
    void testOhmGrammarOuroboros() {
        String ohmSource = getOhmGrammarSource();
        Grammar ohmGrammar = ConstructedGrammar.OhmGrammar;
        int numLoops = 10;
        for (int i = 0; i < numLoops; i++) {
            var matchResult = ohmGrammar.match(ohmSource);
            assertTrue(matchResult.succeeded(), "Ohm Ouroboros failed in iteration " + i);
            Node rootNode = matchResult.getRootNode();
            ohmGrammar = Ohm.buildGrammarSemantics.buildGrammar(rootNode);
        }
    }

    String getBuiltInRulesSource() {
        return getGrammarSource("built-in-rules.ohm");
    }

    @Test
    void testOhmGrammarMatchesBuiltInRulesSource() {
        assertTrue(getOhmGrammar().match(getBuiltInRulesSource()).succeeded());
    }

    String getOperationsAndAttributesSource() {
        return getGrammarSource("operations-and-attributes.ohm");
    }

    @Test
    void testOhmGrammarMatchesOperationsAndAttributesSource() {
        assertTrue(getOhmGrammar().match(getOperationsAndAttributesSource()).succeeded());
    }
}
