package net.stlutz.ohm;

public class InputStream {
    private final String source;
    private int position = 0;
    private int rightmostExaminedPosition = 0;
    
    public InputStream(String source) {
        super();
        this.source = source;
    }
    
    public int getRightmostExaminedPosition() {
        return rightmostExaminedPosition;
    }
    
    public void setRightmostExaminedPosition(int rightmostExaminedPosition) {
        this.rightmostExaminedPosition = rightmostExaminedPosition;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public void advance(int offset) {
        position += offset;
    }
    
    /**
     * Returns the offset of the stream's current position to a previously held position.
     */
    public int offsetTo(int previousPosition) {
        return position - previousPosition;
    }
    
    public boolean atEnd() {
        rightmostExaminedPosition = Math.max(rightmostExaminedPosition, position + 1);
        return position >= source.length();
    }
    
    public int nextCodePoint() {
        int codePoint = source.codePointAt(position);
        position += Character.charCount(codePoint);
        rightmostExaminedPosition = Math.max(rightmostExaminedPosition, position);
        return codePoint;
    }
    
    public boolean matches(String str) {
        return match(str) == str.length();
    }
    
    public boolean matches(String str, boolean ignoreCase) {
        return match(str, ignoreCase) == str.length();
    }
    
    /**
     * See {@link #match(String, boolean)} with {@code ignoreCase == false}.
     */
    public int match(String str) {
        return match(str, false);
    }
    
    /**
     * Matches {@code str} against the input stream at the current position without moving the current position.
     * Comparison is done between Unicode code points.
     * Examines
     * <ul>
     *      <li>up to the first non-matching code point, </li>
     *      <li>until all of {@code str} was matched, or </li>
     *      <li>until the stream's end.</li>
     * </ul>
     *
     * @param str The string to match
     * @param ignoreCase If true, code points match if either their uppercase or lowercase variants match
     * @return The number of chars (not code points) that matched.
     */
    public int match(String str, boolean ignoreCase) {
        int maxMatchLength = Math.min(str.length(), source.length() - position);
        if (maxMatchLength == 0) {
            return 0;
        }
        
        int matchLength = 0;
        while (matchLength < maxMatchLength) {
            int codePointActual = source.codePointAt(position + matchLength);
            int codePointExpected = str.codePointAt(matchLength);
            if (codePointActual != codePointExpected
                && (!ignoreCase || !codePointsMatchCI(codePointActual, codePointExpected))) {
                break;
            }
            matchLength += Character.charCount(codePointActual);
        }
        
        rightmostExaminedPosition = Math.max(rightmostExaminedPosition, position + Math.min(matchLength + 1, str.length()));
        return matchLength;
    }
    
    private boolean codePointsMatchCI(int cpActual, int cpExpected) {
        int cpActualUpper = Character.toUpperCase(cpActual);
        int cpExpectedUpper = Character.toUpperCase(cpExpected);
        return cpActualUpper == cpExpectedUpper
            || Character.toLowerCase(cpActualUpper) == Character.toLowerCase(cpExpectedUpper);
    }
    
    public String sourceSlice(int startIndex, int endIndex) {
        return source.substring(startIndex, endIndex);
    }
    
    public SourceInterval sourceInterval(int startIndex) {
        return sourceInterval(startIndex, position);
    }
    
    public SourceInterval sourceInterval(int startIndex, int endIndex) {
        return new SourceInterval(source, startIndex, endIndex);
    }
}
