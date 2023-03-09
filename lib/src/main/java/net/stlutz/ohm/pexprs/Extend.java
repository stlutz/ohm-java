package net.stlutz.ohm.pexprs;

import org.json.JSONArray;
import net.stlutz.ohm.SourceInterval;

/**
 * Extend is an implementation detail of rule extension
 *
 */
public class Extend extends Alt {
  public PExpr superBody;
  public PExpr body;

  public Extend(PExpr superBody, PExpr body) {
    super(new PExpr[] {body, superBody});
    this.superBody = superBody;
    this.body = body;
  }

  @Override
  public JSONArray toRecipe(SourceInterval grammarInterval) {
    return terms[0].toRecipe(grammarInterval);
  }
}
