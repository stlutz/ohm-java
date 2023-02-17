package net.stlutz.ohm.pexprs;

import java.util.Arrays;

import org.json.JSONArray;

import net.stlutz.ohm.Grammar;
import net.stlutz.ohm.SourceInterval;

/**
 * Splice is an implementation detail of rule overriding with the `...`
 * operator.
 *
 */
public class Splice extends Alt {
	public Grammar superGrammar;
	public String ruleName;
	public int expansionPos;

	public Splice(Grammar superGrammar, String ruleName, PExpr[] beforeTerms, PExpr[] afterTerms) {
		// TODO
		super(new PExpr[0]);
//		PExpr originalBody = superGrammar.rules[ruleName].body;
//		super(new PExpr[] { Util.concatenate(beforeTerms, new PExpr[] { originalBody }, afterTerms) });

		this.superGrammar = superGrammar;
		this.ruleName = ruleName;
		this.expansionPos = beforeTerms.length;
	}

	@Override
	public String recipeName() {
		return "splice";
	}

	@Override
	public JSONArray toRecipe(SourceInterval grammarInterval) {
		JSONArray recipe = super.toRecipe(grammarInterval);

		// beforeTerms
		recipe.put(new JSONArray()
				.putAll(Arrays.stream(terms, 0, expansionPos).map(term -> term.toRecipe(grammarInterval))));

		// afterTerms
		recipe.put(new JSONArray().putAll(
				Arrays.stream(terms, expansionPos + 1, terms.length).map(term -> term.toRecipe(grammarInterval))));

		return recipe;
	}
}
