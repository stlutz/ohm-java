package net.stlutz.ohm.pexprs;

import org.json.JSONArray;

import net.stlutz.ohm.SourceInterval;

public abstract class Aggregation extends PExpr {
	protected final PExpr[] terms;

	protected Aggregation(PExpr[] terms) {
		super();
		this.terms = terms;
	}

	protected abstract Aggregation newInstance(PExpr[] terms);

	@Override
	public boolean allowsSkippingPrecedingSpace() {
		return false;
	}

	@Override
	public void resolveSplice(PExpr superRuleBody) {
		for (PExpr term : terms) {
			term.resolveSplice(superRuleBody);
		}
	}

	@Override
	public PExpr introduceParams(String[] formals) {
		for (int i = 0; i < terms.length; i++) {
			terms[i] = terms[i].introduceParams(formals);
		}
		return this;
	}

	@Override
	public PExpr substituteParams(PExpr[] actuals) {
		// TODO: if all terms stay identical, return this?
		PExpr[] substituted = new PExpr[terms.length];
		for (int i = 0; i < terms.length; i++) {
			substituted[i] = terms[i].substituteParams(actuals);
		}
		return newInstance(substituted);
	}

	@Override
	public JSONArray toRecipe(SourceInterval grammarInterval) {
		JSONArray recipe = super.toRecipe(grammarInterval);
		for (PExpr term : terms)
			recipe.put(term.toRecipe(grammarInterval));
		return recipe;
	}

	protected abstract String getOperator();

	@Override
	public void toString(StringBuilder sb) {
		if (terms.length > 1) {
			sb.append('(');
		}

		boolean isFirst = true;
		for (PExpr term : terms) {
			if (!isFirst) {
				sb.append(getOperator());
			}
			term.toString(sb);
			isFirst = false;
		}

		if (terms.length > 1) {
			sb.append(')');
		}
	}

	@Override
	public void toDisplayString(StringBuilder sb) {
		if (source != null) {
			sb.append(source.trimmed().getContents());
		} else {
			sb.append('[');
			sb.append(getClass().getSimpleName());
			sb.append(']');
		}
	}
}
