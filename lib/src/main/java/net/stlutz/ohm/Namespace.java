package net.stlutz.ohm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Namespace implements Collection<Grammar> {
    private final Map<String, Grammar> grammars = new HashMap<>();
    
    private Namespace() {
        super();
    }
    
    private Namespace(Namespace toCopy) {
        super();
        addAll(toCopy.getGrammars());
    }
    
    static Namespace empty() {
        return new Namespace();
    }
    
    public static Namespace create() {
        return copyOf(ConstructedGrammar.DefaultNamespace);
    }
    
    public static Namespace copyOf(Namespace toCopy) {
        return new Namespace(toCopy);
    }
    
    public Grammar getGrammarNamed(String grammarName) {
        return grammars.get(grammarName);
    }
    
    @Override
    public int size() {
        return grammars.size();
    }
    
    @Override
    public boolean isEmpty() {
        return grammars.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Grammar grammar)) return false;
        return containsGrammarNamed(grammar.getName());
    }
    
    public boolean containsGrammarNamed(String grammarName) {
        return grammars.containsKey(grammarName);
    }
    
    @Override
    public Iterator<Grammar> iterator() {
        return grammars.values().iterator();
    }
    
    @Override
    public Grammar[] toArray() {
        return toArray(new Grammar[0]);
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return grammars.values().toArray(a);
    }
    
    @Override
    public boolean add(Grammar grammar) {
        return grammar != grammars.put(grammar.getName(), grammar);
    }
    
    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Grammar grammar)) return false;
        return grammars.remove(grammar.getName(), grammar);
    }
    
    public Grammar removeGrammarNamed(String grammarName) {
        return grammars.remove(grammarName);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return grammars.values().containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends Grammar> c) {
        boolean changed = false;
        for (var grammar : c) {
            changed |= add(grammar);
        }
        return changed;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (var grammar : c) {
            changed |= remove(grammar);
        }
        return changed;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return grammars.values().retainAll(c);
    }
    
    @Override public void clear() {
        grammars.clear();
    }
    
    public Collection<Grammar> getGrammars() {
        return List.copyOf(grammars.values());
    }
}
