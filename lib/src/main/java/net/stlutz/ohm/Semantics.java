package net.stlutz.ohm;

public class Semantics {
	Grammar grammar;
	Operation[] operations;
	Operation defaultOperation;
	Node rootNode;

	public Grammar getGrammar() {
		return grammar;
	}

	public Node getRootNode() {
		return rootNode;
	}

	public Operation getOperation() {
		if (defaultOperation != null) {
			return defaultOperation;
		} else {
			throw new OhmException("Cannot get operation.");
		}
	}

	public Operation getOperation(String name) {
		for (Operation operation : operations) {
			if (operation.getName().equals(name)) {
				return operation;
			}
		}
		return null;
	}

	protected void initialize() {
	}

	void initializeOperations() {
		for (Operation operation : operations) {
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
