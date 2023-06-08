package net.stlutz.ohm;

import java.util.Collections;
import java.util.List;

public final class Ohm {
    static final BuildGrammar buildGrammarSemantics =
            ConstructedGrammar.OhmGrammar.createSemanticsBlueprint(BuildGrammar.class).instantiate();

    private Ohm() {
    }

    public static Grammar grammar(String source) {
        // TODO: assert
        return grammars(source, null).get(0);
    }

    public static List<Grammar> grammars(String source) {
        return grammars(source, null);
    }

    public static List<Grammar> grammars(String source, Namespace namespace) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        var matchResult = ConstructedGrammar.OhmGrammar.match(source);
        if (matchResult.failed()) {
            // TODO: actual syntax error
            throw new OhmException("Syntax error in grammar source");
        }

        return Collections.unmodifiableList(buildGrammarSemantics.buildGrammars(matchResult.getRootNode(), namespace));
    }
}
