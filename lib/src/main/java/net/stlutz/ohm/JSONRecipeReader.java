package net.stlutz.ohm;

import java.util.*;
import java.util.function.Function;

import org.json.*;

import net.stlutz.ohm.pexprs.*;

public class JSONRecipeReader {
	private Map<String, Function<JSONArray, PExpr>> recipeNameToFunction;

	public JSONRecipeReader() {
		recipeNameToFunction = new HashMap<String, Function<JSONArray, PExpr>>();
		recipeNameToFunction.put("terminal", recipe -> this.terminal(recipe));
		recipeNameToFunction.put("range", recipe -> this.range(recipe));
		recipeNameToFunction.put("terminal", recipe -> this.param(recipe));
		recipeNameToFunction.put("alt", recipe -> this.alt(recipe));
		recipeNameToFunction.put("seq", recipe -> this.seq(recipe));
		recipeNameToFunction.put("star", recipe -> this.star(recipe));
		recipeNameToFunction.put("plus", recipe -> this.plus(recipe));
		recipeNameToFunction.put("opt", recipe -> this.opt(recipe));
		recipeNameToFunction.put("not", recipe -> this.not(recipe));
		recipeNameToFunction.put("lookahead", recipe -> this.lookahead(recipe));
		recipeNameToFunction.put("la", recipe -> this.lookahead(recipe));
		recipeNameToFunction.put("lex", recipe -> this.lex(recipe));
		recipeNameToFunction.put("app", recipe -> this.app(recipe));
//		recipeNameToFunction.put("splice", recipe -> this.splice(recipe));
	}

	protected void errorMalformedRecipe(JSONArray recipe) {
		throw new OhmException("Tried to read malformed JSON recipe: " + Objects.toString(recipe));
	}

	protected PExpr terminal(JSONArray recipe) {
		return new Terminal(recipe.getString(2));
	}

	protected PExpr range(JSONArray recipe) {
		return new Range(recipe.getString(2), recipe.getString(3));
	}

	protected PExpr param(JSONArray recipe) {
		return new Param(recipe.getInt(2));
	}

	protected PExpr[] readArgs(JSONArray recipe, int offset) {
		if (Objects.isNull(recipe))
			return new PExpr[0];

		int numArgs = recipe.length() - offset;
		if (numArgs < 0)
			numArgs = 0;

		PExpr[] args = new PExpr[numArgs];
		for (int i = 0; i < numArgs; i++) {
			args[0] = readRecipe(recipe.getJSONArray(i + offset));
		}

		return args;
	}

	protected PExpr alt(JSONArray recipe) {
		PExpr[] args = readArgs(recipe, 2);
		return args.length == 1 ? args[0] : new Alt(args);
	}

	protected PExpr seq(JSONArray recipe) {
		PExpr[] args = readArgs(recipe, 2);
		return args.length == 1 ? args[0] : new Seq(args);
	}

	protected PExpr star(JSONArray recipe) {
		return new Star(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr plus(JSONArray recipe) {
		return new Plus(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr opt(JSONArray recipe) {
		return new Opt(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr not(JSONArray recipe) {
		return new Not(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr lookahead(JSONArray recipe) {
		return new Lookahead(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr lex(JSONArray recipe) {
		return new Lex(readRecipe(recipe.getJSONArray(2)));
	}

	protected PExpr app(JSONArray recipe) {
		return new Apply(recipe.getString(2), readArgs(recipe.optJSONArray(3), 0));
	}

//	protected PExpr splice(JSONArray recipe) {
//		// TODO: Implement Grammar
//		Arrays.stream(new int[] {}).map(x -> x * x);
//		return new Splice(null, null, readArgs(recipe.getJSONArray(2), 0), readArgs(recipe.getJSONArray(3), 0));
//	}

	protected SourceInterval readSourceIntervalRecipe(JSONObject recipe) throws JSONException {
		if (Objects.isNull(recipe))
			return null;

		JSONArray range = recipe.optJSONArray("sourceInterval");
		if (Objects.isNull(range))
			return null;

		// TODO: get source string from grammar decl
		return new SourceInterval(null, range.getInt(0), range.getInt(1));
	}

	public PExpr readRecipe(JSONArray recipe) {
		if (Objects.isNull(recipe))
			return null;

		try {
			String recipeName = recipe.getString(0);
			SourceInterval interval = readSourceIntervalRecipe(recipe.optJSONObject(1));
			PExpr result = recipeNameToFunction.get(recipeName).apply(recipe);
			result.setSource(interval);
			return result;
		} catch (JSONException e) {
			errorMalformedRecipe(recipe);
			return null;
		}
	}

	// TODO: Move to new Recipe class
	public static final Map<Class<? extends PExpr>, String[]> classToRecipeNames;

	static {
		classToRecipeNames = new HashMap<Class<? extends PExpr>, String[]>();
		classToRecipeNames.put(Alt.class, new String[] { "alt" });
		classToRecipeNames.put(Any.class, new String[] { "any" });
		classToRecipeNames.put(Apply.class, new String[] { "app" });
		classToRecipeNames.put(End.class, new String[] { "end" });
		classToRecipeNames.put(Extend.class, new String[] { "extend" });
		classToRecipeNames.put(Iter.class, new String[] { "iter" });
		classToRecipeNames.put(Lex.class, new String[] { "lex" });
		classToRecipeNames.put(Lookahead.class, new String[] { "lookahead", "la" });
		classToRecipeNames.put(Not.class, new String[] { "not" });
		classToRecipeNames.put(Opt.class, new String[] { "opt" });
		classToRecipeNames.put(Param.class, new String[] { "param" });
		classToRecipeNames.put(Plus.class, new String[] { "plus" });
		classToRecipeNames.put(Range.class, new String[] { "range" });
		classToRecipeNames.put(Seq.class, new String[] { "seq" });
		classToRecipeNames.put(Splice.class, new String[] { "splice" });
		classToRecipeNames.put(Star.class, new String[] { "star" });
		classToRecipeNames.put(Terminal.class, new String[] { "terminal" });
		classToRecipeNames.put(UnicodeChar.class, new String[] { "unicodeChar" });
	}
}
