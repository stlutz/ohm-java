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
    
    public static String escapedString(String string) {
        StringBuilder sb = new StringBuilder();
        escapeString(string, sb);
        return sb.toString();
    }
    
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
            sb.append(hexFormat.toHexDigits(codePoint, codePoint > 0xFFFFF ? 6 : 5));
            sb.append('}');
        } else if (codePoint < '\u0020'
            || (codePoint >= '\u0080' && codePoint < '\u00a0')) {
            sb.append("\\x");
            sb.append(hexFormat.toHexDigits(codePoint, 2));
        } else if (codePoint >= '\u2000' && codePoint < '\u2100') {
            sb.append("\\u");
            sb.append(hexFormat.toHexDigits(codePoint, 4));
        } else {
            sb.appendCodePoint(codePoint);
        }
    }
    
    private static final int backslashCodePoint = 92; // "\\".codePointAt(0);
    private static final int doubleQuoteCodePoint = 34; // "\"".codePointAt(0);
    private static final int singleQuoteCodePoint = 39; // "\'".codePointAt(0);
    private static final int backspaceCodePoint = 8; // "\b".codePointAt(0);
    private static final int lineFeedCodePoint = 10; // "\n".codePointAt(0);
    private static final int carriageReturnCodePoint = 13; // "\r".codePointAt(0);
    private static final int tabCodePoint = 9; // "\t".codePointAt(0);
    
    public static String unescapedString(String escapedString) {
        StringBuilder sb = new StringBuilder();
        if (!unescapeSubstring(escapedString, 0, escapedString.length(), sb)) {
            throw new OhmException("Internal error. Failed to unescape terminal string.");
        }
        return sb.toString();
    }
    
    public static String unescapedSubstring(String escapedString, int inclStart, int exclEnd) {
        StringBuilder sb = new StringBuilder();
        if (!unescapeSubstring(escapedString, inclStart, exclEnd, sb)) {
            throw new OhmException("Internal error. Failed to unescape terminal string.");
        }
        return sb.toString();
    }
    
    /**
     * Converts a substring of {@code escapedString} from an Ohm-escaped Terminal-String to its unescaped version.
     * Correctly handles Unicode beyond BMP-0.
     *
     * @param escapedString the string to take a substring from
     * @param inclStart the start index (inclusive) of the substring to unescape.
     * @param exclEnd the end index (exclusive) of the substring to unescape.
     * @param sb the {@link StringBuilder} to write the unescaped version of the substring into.
     * Contents are undefined if there are problems during unescaping.
     * @return {@code true} if the unescaped string was written to {@code sb} without problems, {@code false} otherwise.
     */
    public static boolean unescapeSubstring(String escapedString, int inclStart, int exclEnd, StringBuilder sb) {
        if (inclStart >= exclEnd || inclStart < 0 || exclEnd > escapedString.length()) return false;
        int index = inclStart;
        while (index < exclEnd) {
            int codePoint = escapedString.codePointAt(index);
            if (codePoint == backslashCodePoint) {
                if (++index >= exclEnd) return false; // stand-alone backslash at the end
                char escapeChar = escapedString.charAt(index++);
                int actualCodePoint = switch (escapeChar) {
                    case '\\' -> backslashCodePoint;
                    case '"' -> doubleQuoteCodePoint;
                    case '\'' -> singleQuoteCodePoint;
                    case 'b' -> backspaceCodePoint;
                    case 'n' -> lineFeedCodePoint;
                    case 'r' -> carriageReturnCodePoint;
                    case 't' -> tabCodePoint;
                    case 'u' -> {
                        String hexDigits;
                        if (escapedString.charAt(index) == '{') {
                            int closingIndex = escapedString.indexOf('}', index + 2);
                            if (closingIndex < 0 || closingIndex >= exclEnd || closingIndex - index > 7) yield -1;
                            hexDigits = escapedString.substring(index + 1, closingIndex);
                            index = closingIndex + 1;
                        } else {
                            if (index + 4 > exclEnd) yield -1;
                            hexDigits = escapedString.substring(index, index + 4);
                            index += 4;
                        }
                        try {
                            yield Integer.parseInt(hexDigits, 16);
                        } catch (NumberFormatException e) {
                            yield -1;
                        }
                    }
                    case 'x' -> {
                        if (index + 2 > exclEnd) yield -1;
                        String hexDigits = escapedString.substring(index, index + 2);
                        index += 2;
                        try {
                            yield Integer.parseInt(hexDigits, 16);
                        } catch (NumberFormatException e) {
                            yield -1;
                        }
                    }
                    default -> -1;
                };
                if (actualCodePoint < 0) return false;
                sb.appendCodePoint(actualCodePoint);
            } else {
                index += Character.charCount(codePoint);
                sb.appendCodePoint(codePoint);
            }
        }
        return true;
    }
}
