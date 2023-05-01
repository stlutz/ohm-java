package net.stlutz.ohm;

import java.util.*;

public class Namespace {
    private final Map<String, Grammar> grammars = new HashMap<>();

    private Namespace() {
        super();
    }

    private Namespace(Namespace toCopy) {
        super();
        for (Grammar grammar : toCopy.getGrammars()) {
            add(grammar);
        }
    }

    static Namespace empty() {
        return new Namespace();
    }

    public static Namespace create() {
        return copyOf(DynamicGrammar.DefaultNamespace);
    }

    public static Namespace copyOf(Namespace toCopy) {
        return new Namespace(toCopy);
    }

    public Grammar get(String grammarName) {
        return grammars.get(grammarName);
    }

    public Grammar add(Grammar grammar) {
        return grammars.put(grammar.getName(), grammar);
    }

    public Grammar remove(String grammarName) {
        return grammars.remove(grammarName);
    }

    public boolean has(String grammarName) {
        return grammars.containsKey(grammarName);
    }

    public Collection<Grammar> getGrammars() {
        return List.copyOf(grammars.values());
    }
}
