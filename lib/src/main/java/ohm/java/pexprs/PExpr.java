package ohm.java.pexprs;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import ohm.java.*;

public abstract class PExpr {
	// TODO: rename to sourceInterval
	protected SourceInterval source;

	public PExpr() {
		super();
	}

	public PExpr(SourceInterval interval) {
		this();
		this.source = interval.trimmed();
	}

	public SourceInterval getSource() {
		return source;
	}

	public void setSource(SourceInterval source) {
		this.source = source;
	}

	public abstract boolean allowsSkippingPrecedingSpace();

	public abstract int getArity();

	/**
	 * Returns a PExpr that results from recursively replacing every formal
	 * parameter (i.e., instance of `Param`) inside this PExpr with its actual value
	 * from `actuals` (an Array).
	 * 
	 * The receiver must not be modified; a new PExpr must be returned if any
	 * replacement is necessary.
	 * 
	 */
	public abstract PExpr substituteParams(PExpr[] actuals);

	/**
	 * Called at grammar creation time to rewrite a rule body, replacing each
	 * reference to a formal parameter with a `Param` node. Returns a PExpr --
	 * either a new one, or the original one if it was modified in place.
	 */
	public abstract PExpr introduceParams(String[] formals);

	/**
	 * Evaluate the expression and return `true` if it succeeds, `false` otherwise.
	 * This method should only be called directly by `MatchState.eval(expr)`, which
	 * also updates the data structures that are used for tracing. (Making those
	 * updates in a method of `MatchState` enables the trace-specific data
	 * structures to be "secrets" of that class, which is good for modularity.)
	 * 
	 * The contract of this method is as follows: When the return value is `true`,
	 * the matcher will have `expr.getArity()` more bindings than it did before the
	 * call. When the return value is `false`, the matcher may have more bindings
	 * than it did before the call, and its input stream's position may be anywhere.
	 * 
	 * Note that `MatchState.eval(expr)`, unlike this method, guarantees that
	 * neither the matcher's bindings nor its input stream's position will change if
	 * the expression fails to match.
	 * 
	 * @param matchState
	 * @return
	 */
	public abstract boolean eval(MatchState matchState, InputStream inputStream, int originalPosition);

	public boolean eval(MatchState matchState) {
		InputStream inputStream = matchState.getInputStream();
		return eval(matchState, inputStream, inputStream.getPosition());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public abstract void toString(StringBuilder sb);

	public String toDisplayString() {
		StringBuilder sb = new StringBuilder();
		toDisplayString(sb);
		return sb.toString();
	}

	public void toDisplayString(StringBuilder sb) {
		toString(sb);
	}

//	abstract void assertAllApplicationsAreValid(String ruleName, Grammar grammar);
//	abstract void assertChoicesHaveUniformArity(String ruleName);
//	abstract void assertIteratedExprsAreNotNullable(Grammar grammar);
//	public abstract boolean isNullable(Grammar grammar) {}
//	public abstract boolean isNullable(Grammar grammar, ? memo);
//	public abstract PExpr introduceParams(String[] formals);
//	public abstract PExpr substituteParams(PExpr[] actuals);
//	public abstract Failure toFailure(Grammar grammar);

	public abstract String recipeName();

	public JSONArray toRecipe(SourceInterval grammarInterval) {
		JSONArray recipe = new JSONArray();
		recipe.put(recipeName());

		SourceInterval adjustedInterval = source;
		if (Objects.nonNull(source) && Objects.nonNull(grammarInterval)) {
			adjustedInterval = source.relativeTo(grammarInterval);
		}
		recipe.put(new JSONObject().put("sourceInterval",
				new JSONArray().put(adjustedInterval.startIndex).put(adjustedInterval.endIndex)));

		return recipe;
	}

	public JSONArray toRecipe() {
		return toRecipe(null);
	}

//	public static PExpr fromRecipe(JSONArray recipe) {
//		
//	}
}
