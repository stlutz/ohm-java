package net.stlutz.ohm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

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
        for (T[] array : arrays) {
            resultLength += array.length;
        }
        
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
    
    private static final HexFormat hexFormat = HexFormat.of().withUpperCase();
    
    public static void escapeString(String string, StringBuilder sb) {
        if (string == null) return;
        
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '\\', '"', '\'' -> {
                    sb.append('\\');
                    sb.append(c);
                }
                case '\b' -> sb.append("\\b");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    int codePoint = c;
                    if (Character.isHighSurrogate(c)) {
                        if (++i >= string.length()) break; // something's not right
                        codePoint = Character.toCodePoint(c, string.charAt(i));
                    }
                    escapeCodePoint(codePoint, sb);
                }
            }
        }
    }
    
    public static void escapeCodePoint(int codePoint, StringBuilder sb) {
        if (codePoint > 0xFFFF) {
            sb.append("\\u{");
            sb.append(hexFormat.toHexDigits(codePoint));
            sb.append('}');
        } else if (codePoint < '\u0020'
            || (codePoint >= '\u0080' && codePoint < '\u00a0')) {
            sb.append("\\x");
            sb.append(hexFormat.toHexDigits(codePoint, 2));
        } else if (codePoint >= '\u2000' && codePoint < '\u2100') {
            sb.append("\\u");
            sb.append(hexFormat.toHexDigits(codePoint, 4));
        } else {
            sb.append(codePoint);
        }
    }
}
