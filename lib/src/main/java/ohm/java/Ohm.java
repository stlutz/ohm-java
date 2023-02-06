package ohm.java;



public final class Ohm {
	private Ohm() { }
	
	public static Grammar grammar(String source) {
		return new Grammar();
	}
	
	public static Grammar[] grammars(String source) {
		return new Grammar[0];
	}
}
