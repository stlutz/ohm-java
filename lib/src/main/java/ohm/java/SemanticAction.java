package ohm.java;

@FunctionalInterface
public interface SemanticAction<R> {
	R apply(Node... nodes);
}
