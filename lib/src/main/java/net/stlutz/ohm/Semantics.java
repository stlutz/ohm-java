package net.stlutz.ohm;

public class Semantics {
	Grammar grammar;
	SemanticActions[] operations;
	SemanticActions defaultOperation;
	Node rootNode;

	public Grammar getGrammar() {
		return grammar;
	}

	public Node getRootNode() {
		return rootNode;
	}

	public SemanticActions getOperation() {
		if (defaultOperation != null) {
			return defaultOperation;
		} else {
			throw new OhmException("Cannot get operation.");
		}
	}

	public SemanticActions getOperation(String name) {
		for (SemanticActions operation : operations) {
			if (operation.getName().equals(name)) {
				return operation;
			}
		}
		return null;
	}

	protected void initialize() {
	}

	void initializeOperations() {
		for (SemanticActions operation : operations) {
			operation.initialize();
		}
	}

	public boolean hasOperation(String name) {
		return getOperation(name) != null;
	}

	public Object execute() {
		return getOperation().apply(rootNode);
	}

	public Object execute(String operationName) {
		return getOperation(operationName).apply(rootNode);
	}

}
