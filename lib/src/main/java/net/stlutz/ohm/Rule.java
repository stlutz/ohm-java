package net.stlutz.ohm;

import net.stlutz.ohm.pexprs.PExpr;

public class Rule {
	private final Operation operation;
	private final PExpr body;
	private final String[] formals;
	private final String description;
	private final SourceInterval source;

	public enum Operation {
		DEFINE, EXTEND, OVERRIDE,
	}

	Rule(PExpr body, String[] formals, String description) {
		this(body, formals, description, null, Operation.DEFINE);
	}

	public Rule(PExpr body, String[] formals, String description, SourceInterval source, Operation operation) {
		super();
		this.body = body;
		this.formals = formals;
		this.description = description;
		this.source = source;
		this.operation = operation;
	}

	public Operation getOperation() {
		return operation;
	}

	public PExpr getBody() {
		return body;
	}

	public int getArity() {
		return body.getArity();
	}

	public String[] getFormals() {
		return formals;
	}

	public String getDescription() {
		return description;
	}

	public SourceInterval getSource() {
		return source;
	}

	public boolean isDefinition() {
		return operation == Operation.DEFINE;
	}

	public boolean isOverride() {
		return operation == Operation.OVERRIDE;
	}

	public boolean isExtension() {
		return operation == Operation.EXTEND;
	}
}
