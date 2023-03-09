package net.stlutz.ohm.pexprs;

import org.json.JSONArray;
import net.stlutz.ohm.*;

public class Apply extends PExpr {
  public String ruleName;
  private PExpr[] args;

  /**
   * Caches the result of {@code this.toString()}.
   */
  private String memoKey;

  public Apply(String ruleName) {
    this(ruleName, new PExpr[0]);
  }

  public Apply(String ruleName, PExpr[] args) {
    super();
    this.ruleName = ruleName;
    this.args = args;
  }

  public PExpr[] getArgs() {
    return args;
  }

  public PExpr getArg(int index) {
    return args[index];
  }

  @Override
  public boolean allowsSkippingPrecedingSpace() {
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public PExpr introduceParams(String[] formals) {
    for (int index = 0; index < formals.length; index++) {
      if (ruleName.equals(formals[index])) {
        // We are not actually an application, just a parameter
        if (args.length > 0) {
          throw new OhmException(
              "Parameterized rules cannot be passed as arguments to another rule");
        }
        Param result = new Param(index);
        result.setSource(source);
        return result;
      }
    }

    for (int i = 0; i < args.length; i++) {
      args[i] = args[i].introduceParams(formals);
    }
    return this;
  }

  @Override
  public PExpr substituteParams(PExpr[] actuals) {
    if (args.length == 0) {
      return this;
    }

    PExpr[] substituted = new PExpr[args.length];
    for (int i = 0; i < args.length; i++) {
      substituted[i] = args[i].substituteParams(actuals);
    }
    return new Apply(ruleName, substituted);
  }

  @Override
  public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
    Apply caller = matchState.currentApplication();
    PExpr[] actuals = (caller != null) ? caller.args : new PExpr[0];
    Apply app = (Apply) substituteParams(actuals);

    PositionInfo posInfo = matchState.getCurrentPositionInfo();
    if (posInfo.isActive(app)) {
      // This rule is already active at this position, i.e. it's left-recursive
      return app.handleCycle(matchState, inputStream);
    }

    String memoKey = app.toMemoKey();
    MemoizationRecord memoRec = posInfo.remember(memoKey);

    if (memoRec != null && posInfo.shouldUseMemoizedResult(memoRec)) {
      if (matchState.hasNecessaryInfo(memoRec)) {
        return matchState.useMemoizedResult(inputStream.getPosition(), memoRec);
      }
      posInfo.forget(memoKey);
    }

    return app.reallyEval(matchState, inputStream, inputStream.getPosition());
  }

  private boolean handleCycle(MatchState matchState, InputStream inputStream) {
    PositionInfo posInfo = matchState.getCurrentPositionInfo();
    MemoizationRecord currentLeftRecursion = posInfo.getCurrentLeftRecursion();
    String memoKey = toMemoKey();
    MemoizationRecord memoRec = posInfo.remember(memoKey);

    if (currentLeftRecursion != null
        && currentLeftRecursion.getHeadApplication().toMemoKey().equals(memoKey)) {
      // We already know about this left recursion, but it's possible there are
      // "involved applications" that we don't already know about, so...
      memoRec.updateInvolvedApplicationMemoKeys();
    } else if (memoRec == null) {
      // New left recursion detected! Memoize a failure to try to get a seed parse.
      memoRec = posInfo.memoize(memoKey);
      posInfo.startLeftRecursion(this, memoRec);
    }

    return matchState.useMemoizedResult(inputStream.getPosition(), memoRec);
  }

  private boolean reallyEval(MatchState matchState, InputStream inputStream, int originalPosition) {
    PositionInfo origPosInfo = matchState.getCurrentPositionInfo();
    // TODO: bake rule body into apply node?
    Rule rule = matchState.getRule(ruleName);
    if (rule == null) {
      throw new OhmException("No rule '%s' found".formatted(ruleName));
    }

    matchState.enterApplication(origPosInfo, this);

    ParseNode nodeOrNull = evalOnce(rule.getBody(), matchState);
    MemoizationRecord currentLR = origPosInfo.getCurrentLeftRecursion();
    String memoKey = toMemoKey();
    boolean isHeadOfLeftRecursion =
        (currentLR != null) && (currentLR.getHeadApplication().toMemoKey().equals(memoKey));

    MemoizationRecord memoRec;
    if (isHeadOfLeftRecursion) {
      nodeOrNull =
          growSeedResult(rule.getBody(), matchState, originalPosition, currentLR, nodeOrNull);
      origPosInfo.endLeftRecursion();
      memoRec = currentLR;
      origPosInfo.memoize(memoKey, memoRec);
    } else if ((currentLR == null) || (!currentLR.isInvolved(memoKey))) {
      // This application is not involved in left recursion, so it's ok to memoize it
      memoRec = new MemoizationRecord();
      memoRec.setMatchLength(inputStream.getPosition() - originalPosition);
      memoRec.setValue(nodeOrNull);
      memoRec = origPosInfo.memoize(memoKey, memoRec);
    }

    matchState.exitApplication(origPosInfo, nodeOrNull);

    return nodeOrNull != null;
  }

  private ParseNode evalOnce(PExpr body, MatchState matchState) {
    InputStream inputStream = matchState.getInputStream();
    int originalPosition = inputStream.getPosition();

    if (matchState.eval(body)) {
      int arity = body.getArity();
      ParseNode[] bindings = matchState.spliceLastBindings(arity);
      int[] offsets = matchState.spliceLastBindingOffsets(arity);
      int matchLength = inputStream.getPosition() - originalPosition;
      return new NonterminalNode(matchLength, ruleName, bindings, offsets);
    }

    return null;
  }

  private ParseNode growSeedResult(PExpr body, MatchState matchState, int originalPosition,
      MemoizationRecord lrMemoRec, ParseNode newValue) {
    if (newValue == null) {
      return null;
    }

    InputStream inputStream = matchState.getInputStream();

    while (true) {
      lrMemoRec.setMatchLength(inputStream.getPosition() - originalPosition);
      lrMemoRec.setValue(newValue);
      inputStream.setPosition(originalPosition);
      newValue = evalOnce(body, matchState);
      if (inputStream.getPosition() - originalPosition <= lrMemoRec.getMatchLength()) {
        break;
      }
    }

    inputStream.setPosition(originalPosition + lrMemoRec.getMatchLength());
    return lrMemoRec.getValue();
  }

  public boolean isSyntactic() {
    return Util.isSyntactic(ruleName);
  }

  public String toMemoKey() {
    // TODO: rename variable and function
    if (memoKey == null) {
      memoKey = toString();
    }
    return memoKey;
  }

  @Override
  public void toString(StringBuilder sb) {
    sb.append(ruleName);

    if (args.length == 0) {
      return;
    }

    sb.append('<');
    boolean isFirst = true;
    for (PExpr arg : args) {
      if (!isFirst) {
        sb.append(',');
      }
      arg.toString(sb);
      isFirst = false;
    }
    sb.append('>');
  }

  @Override
  public void toDisplayString(StringBuilder sb) {
    // TODO: Almost identical to toString()
    sb.append(ruleName);

    if (args.length == 0) {
      return;
    }

    sb.append('<');
    boolean isFirst = true;
    for (PExpr arg : args) {
      if (!isFirst) {
        sb.append(',');
      }
      arg.toDisplayString(sb);
      isFirst = false;
    }
    sb.append('>');
  }

  @Override
  public String recipeName() {
    return "app";
  }

  @Override
  public JSONArray toRecipe(SourceInterval grammarInterval) {
    JSONArray argsRecipe = new JSONArray();
    for (PExpr arg : args) {
      argsRecipe.put(arg.toRecipe(grammarInterval));
    }
    return super.toRecipe(grammarInterval).put(ruleName).put(argsRecipe);
  }
}
