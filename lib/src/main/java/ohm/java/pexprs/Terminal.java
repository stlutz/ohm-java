package ohm.java.pexprs;

import org.json.JSONArray;
import org.json.JSONWriter;

import ohm.java.*;

public class Terminal extends Prim {
	private String obj;

	public Terminal(String obj) {
		super();
		this.obj = obj;
	}

	String getString() {
		return obj;
	}

	@Override
	public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
		if (inputStream.matches(obj)) {
			inputStream.advance(obj.length());
			matchState.pushBinding(TerminalNode.get(obj.length()), originalPosition);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		new JSONWriter(sb).value(obj);
	}

	@Override
	public String recipeName() {
		return "terminal";
	}

	@Override
	public JSONArray toRecipe(SourceInterval grammarInterval) {
		return super.toRecipe(grammarInterval).put(obj);
	}
}
