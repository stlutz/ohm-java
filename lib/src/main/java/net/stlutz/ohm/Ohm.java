package net.stlutz.ohm;

import java.util.List;

public final class Ohm {
    static final BuildGrammar buildGrammarSemantics =
            DynamicGrammar.OhmGrammar.createSemanticsBlueprint(BuildGrammar.class).instantiate();

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
            throw new OhmException("source must not be null");
        }
        var matchResult = DynamicGrammar.OhmGrammar.match(source);
        if (matchResult.failed()) {
            throw new OhmException("Syntax error in grammar source");
        }

        return buildGrammarSemantics.buildGrammars(matchResult.getRootNode(), namespace);
    }
}
