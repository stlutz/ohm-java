package net.stlutz.ohm;

import java.util.HashMap;
import java.util.Map;

import net.stlutz.ohm.pexprs.*;

public class Grammar {
	static final Grammar ProtoBuiltInRules = buildProtoBuiltInRules();

	private final String name;
	private final Grammar superGrammar;
	private final Map<String, Rule> rules;
	private String defaultStartRule;

	Grammar(String name, Grammar superGrammar, Map<String, Rule> rules, String defaultStartRule) {
		super();
		this.name = name;
		this.superGrammar = superGrammar;
		this.rules = rules;
		setDefaultStartRule(defaultStartRule);
	}

	public String getDefaultStartRule() {
		return defaultStartRule;
	}

	public void setDefaultStartRule(String defaultStartRule) {
		if (defaultStartRule == null) {
			throw new OhmException("Default start rule cannot be null");
		}
		if (!hasRule(defaultStartRule)) {
			throw new OhmException(
					"Invalid start rule: '%s' is not a rule in grammar '%s'".formatted(defaultStartRule, name));
		}
		this.defaultStartRule = defaultStartRule;
	}

	public String getName() {
		return name;
	}

	public Grammar getSuperGrammar() {
		return superGrammar;
	}

	static Grammar buildProtoBuiltInRules() {
		Map<String, Rule> rules = new HashMap<>();
		rules.put("any", new Rule(Any.getInstance(), new String[0], "any character"));
		rules.put("end", new Rule(End.getInstance(), new String[0], "end of input"));
		rules.put("caseInsensitive", new Rule(new CaseInsensitiveTerminal(new Param(0)), new String[] { "str" },
				"case-insensitive terminal"));
		rules.put("lower", new Rule(new UnicodeChar("Ll"), new String[0], "a lowercase letter"));
		rules.put("upper", new Rule(new UnicodeChar("Lu"), new String[0], "a lowercase letter"));
		rules.put("unicodeLtmo",
				new Rule(new UnicodeChar("Ltmo"), new String[0], "a Unicode character in Lt, Lm, or Lo"));
		rules.put("spaces", new Rule(new Star(new Apply("space")), new String[0], "zero or more spaces"));
		rules.put("space", new Rule(new Range(0x00, " ".codePointAt(0)), new String[0], "a space"));
		return new Grammar("ProtoBuiltInRules", null, rules, "any");
	}

	public static Grammar parse(String grammar) {
		// TODO
		return null;
	}

	public Rule getRule(String ruleName) {
		return rules.get(ruleName);
	}

	public boolean hasRule(String ruleName) {
		return rules.containsKey(ruleName);
	}

	public Matcher getMatcher(String input) {
		return new Matcher(this, input);
	}

	public MatchResult match(String input) {
		if (defaultStartRule == null) {
			throw new OhmException("Grammar has no default start rule.");
		}

		return match(input, defaultStartRule);
	}

	public MatchResult match(String input, String startRule) {
		return getMatcher(input).match(startRule);
	}

	public SemanticsBlueprint createSemanticsBlueprint(Class<? extends Semantics> semanticsClass) {
		return SemanticsBlueprint.create(semanticsClass, this);
	}

	Apply parseApplication(String ruleName) {
		if (ruleName.contains("<")) {
			// TODO
			throw new OhmException("parameterized applications are not implemented yet");
		}

		if (!hasRule(ruleName)) {
			throw new OhmException("'%s' is not a rule in grammar '%s'".formatted(ruleName, name));
		}

		return new Apply(ruleName);
	}
}
