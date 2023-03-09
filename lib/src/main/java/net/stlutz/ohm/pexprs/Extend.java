package net.stlutz.ohm.pexprs;

import org.json.JSONArray;

import net.stlutz.ohm.Grammar;
import net.stlutz.ohm.SourceInterval;

/**
 * Extend is an implementation detail of rule extension
 *
 */
public class Extend extends Alt {// TODO: Does this need to inherit from Alt?
  public String name;
  public Grammar superGrammar;
  public PExpr body;

  public Extend(Grammar superGrammar, String name, PExpr body) {
    // TODO implement Grammar
    super(new PExpr[0]);
    // PExpr originalBody = superGrammar.rules[name].body;
    // super(PExpr[] {body, originalBody});

    this.superGrammar = superGrammar;
    this.name = name;
    this.body = body;
  }

  @Override
  public JSONArray toRecipe(SourceInterval grammarInterval) {
    return terms[0].toRecipe(grammarInterval);
  }
}
