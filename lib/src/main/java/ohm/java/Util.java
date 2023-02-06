package ohm.java;

import java.util.Arrays;

public final class Util {
	public static boolean isSyntactic(String ruleName) {
		return Character.isUpperCase(ruleName.charAt(0));
	}

	public static boolean isLexical(String ruleName) {
		return !isSyntactic(ruleName);
	}

	public static <T> T[] concatenate(T[] firstArray, T[]... arrays) {
		// TODO: write test
		int resultLength = firstArray.length;
		for (T[] array : arrays)
			resultLength += array.length;

		T[] result = Arrays.copyOf(firstArray, resultLength);

		int offset = firstArray.length;
		for (T[] array : arrays) {
			System.arraycopy(arrays, 0, result, offset, array.length);
			offset += array.length;
		}

		return result;
	}
}
