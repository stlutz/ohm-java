package net.stlutz.ohm;

import java.util.*;

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

  public static <T> Collection<T> getDuplicates(Collection<T> elements) {
    Set<T> seen = new HashSet<>();
    Set<T> duplicates = new HashSet<>();
    for (T element : elements) {
      if (!seen.add(element)) {
        duplicates.add(element);
      }
    }
    return duplicates;
  }

  public static <T> Collection<T> getDuplicates(T[] elements) {
    return getDuplicates(List.of(elements));
  }
}
