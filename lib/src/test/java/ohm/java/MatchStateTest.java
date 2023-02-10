package ohm.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ohm.java.pexprs.Apply;

class MatchStateTest {
	private MatchState matchState;

	private static MatchState getMatchState(String input, Apply startExpr) {
		Grammar grammar = null;
		Matcher matcher = new Matcher(grammar, input);

		return new MatchState(matcher, startExpr);
	}

	private static MatchState getMatchState() {
		return getMatchState("", new Apply("Hello"));
	}

	@Test
	void testApplicationStack() {
		matchState = getMatchState("Hello World!", new Apply("Initial"));
		InputStream inputStream = matchState.getInputStream();
		assertEquals(0, inputStream.getPosition());
		assertNull(matchState.currentApplication());
		assertFalse(matchState.inLexifiedContext());
		inputStream.advance(3);

		Apply fooApp = new Apply("foo");
		PositionInfo fooPos = new PositionInfo();
		matchState.enterApplication(fooPos, fooApp);
		assertSame(fooApp, matchState.currentApplication(), "Should be the last application entered (and not exited)");
		inputStream.advance(5);
		assertEquals(6, matchState.positionToOffset(9),
				"Should be the offset to the position the inputStream had when entering the current application");
		matchState.enterLexifiedContext();
		assertTrue(matchState.inLexifiedContext());

		Apply barApp = new Apply("bar");
		PositionInfo barPos = new PositionInfo();
		matchState.enterApplication(barPos, barApp);
		assertSame(barApp, matchState.currentApplication(), "Should be the last application entered (and not exited)");
		assertEquals(5, matchState.positionToOffset(13),
				"Should be the offset to the position the inputStream had when entering the current application");
		assertFalse(matchState.inLexifiedContext());
		matchState.exitApplication(barPos, null);

		assertTrue(matchState.inLexifiedContext());
		assertSame(fooApp, matchState.currentApplication(), "Should be the last application entered (and not exited)");
		assertEquals(7, matchState.positionToOffset(10),
				"Should be the offset to the position the inputStream had when entering the current application");

		matchState.exitApplication(fooPos, null);
	}

	@Test
	void testSyntacticContext() {
		Apply lexApp = new Apply("foo");
		Apply synApp = new Apply("Foo");

		matchState = getMatchState("Hello World!", lexApp);
		assertFalse(matchState.inSyntacticContext(), "Should be false when start application is not syntactic");
		matchState.enterApplication(new PositionInfo(), synApp);
		assertTrue(matchState.inSyntacticContext(), "Should be true when current application is syntactic");
		matchState.enterLexifiedContext();
		assertFalse(matchState.inSyntacticContext(), "Should be false in lexified context");

		matchState = getMatchState("Hello World!", synApp);
		assertTrue(matchState.inSyntacticContext(), "Should be true when start application is syntactic");
		matchState.enterApplication(new PositionInfo(), lexApp);
		assertFalse(matchState.inSyntacticContext(), "Should be false when current application is not syntactic");
		matchState.enterLexifiedContext();
		assertFalse(matchState.inSyntacticContext(), "Should still be false in lexified context");
	}

	@Test
	void testLexifiedContext() {
		matchState = getMatchState();
		assertFalse(matchState.inLexifiedContext(), "Should not be lexified unless explicitly entered");
		matchState.enterLexifiedContext();
		assertTrue(matchState.inLexifiedContext());
		matchState.exitLexifiedContext();
		assertFalse(matchState.inLexifiedContext());
	}

//	@Test
//	void testSkipSpaces() {
//		fail("Not yet implemented");
//	}

	@Test
	void testBindingsStack() {
		matchState = getMatchState();
		ParseNode node = TerminalNode.get(3);
		assertEquals(0, matchState.numBindings());
		matchState.pushBinding(node, 0);
		matchState.pushBinding(node, 3);
		matchState.pushBinding(node, 6);
		assertEquals(3, matchState.numBindings());
		matchState.popBinding();
		assertEquals(2, matchState.numBindings());
	}

	@Test
	void testBindingsSplice() {
		matchState = getMatchState();
		ParseNode node1 = TerminalNode.get(3);
		ParseNode node2 = TerminalNode.get(5);
		ParseNode node3 = TerminalNode.get(1);
		matchState.pushBinding(node1, 0);
		matchState.pushBinding(node2, 3);
		matchState.pushBinding(node3, 8);
		assertArrayEquals(new ParseNode[] { node2, node3 }, matchState.spliceLastBindings(2));
		assertArrayEquals(new int[] { 3, 8 }, matchState.spliceLastBindingOffsets(2));
		assertEquals(1, matchState.numBindings());
	}

	@Test
	void testTruncateBindings() {
		matchState = getMatchState();
		ParseNode node1 = TerminalNode.get(3);
		ParseNode node2 = TerminalNode.get(5);
		ParseNode node3 = TerminalNode.get(1);
		matchState.pushBinding(node1, 0);
		matchState.pushBinding(node2, 3);
		matchState.pushBinding(node3, 8);
		assertEquals(3, matchState.numBindings());
		matchState.truncateBindings(1);
		assertEquals(1, matchState.numBindings());
	}

}
