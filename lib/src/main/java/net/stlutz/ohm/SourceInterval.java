package net.stlutz.ohm;

import java.util.Objects;

/**
 * An object describing a subrange of a string.
 */
public class SourceInterval {
    private final String sourceString;
    private final int startIndex;
    private final int endIndex;
    private String contentsCache;
    
    public SourceInterval(String sourceString, int startIndex, int endIndex) {
        this.sourceString = Objects.requireNonNull(sourceString);
        
        if (startIndex > endIndex || startIndex < 0 || endIndex > sourceString.length()) {
            throw new RuntimeException(
                    "Invalid interval bounds (%1$d, %2$d).".formatted(startIndex, endIndex));
        }
        
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
    
    /**
     * Returns the string the receiver is referencing.
     */
    public String getSourceString() {
        return sourceString;
    }
    
    /**
     * Returns the start index (inclusive) of the receiver.
     */
    public int getStartIndex() {
        return startIndex;
    }
    
    /**
     * Returns the end index (exclusive) of the receiver.
     */
    public int getEndIndex() {
        return endIndex;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SourceInterval)) {
            return false;
        }
        SourceInterval other = (SourceInterval) obj;
        return endIndex == other.endIndex && Objects.equals(sourceString, other.sourceString)
                && startIndex == other.startIndex;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endIndex, sourceString, startIndex);
    }
    
    @Override
    public String toString() {
        return "SourceInterval (" + startIndex + ", " + endIndex + ", \"" + getContents() + "\")";
    }
    
    public String getContents() {
        if (contentsCache == null) {
            contentsCache = sourceString.substring(startIndex, endIndex);
        }
        return contentsCache;
    }
    
    /**
     * Returns the number of characters covered by the receiver.
     */
    public int length() {
        return endIndex - startIndex;
    }
    
    /**
     * Returns a new interval of length 0 that starts at the start index of the receiver.
     */
    public SourceInterval collapsedLeft() {
        // TODO: why?
        return new SourceInterval(sourceString, startIndex, startIndex);
    }
    
    /**
     * Returns a new interval of length 0 that starts at the end index of the receiver.
     */
    public SourceInterval collapsedRight() {
        // TODO: why?
        return new SourceInterval(sourceString, endIndex, endIndex);
    }
    
    LineAndColumnInfo getLineAndColumn() {
        // TODO
        return LineAndColumnInfo.from(sourceString, startIndex);
    }
    
    String getLineAndColumnMessage() {
        // TODO
        throw new RuntimeException("Not yet implemented");
    }
    
    /**
     * Subtracts the interval {@code sub} from this interval.
     *
     * @param subtrahend The interval to be subtracted. Must reference the same source string.
     * @return An array of 0, 1, or 2 intervals
     */
    public SourceInterval[] minus(SourceInterval subtrahend) {
        Objects.requireNonNull(subtrahend);
        assertSameSource(subtrahend);
        
        if (startIndex >= subtrahend.startIndex && endIndex <= subtrahend.endIndex) {
            // we are a subset of the subtrahend
            return new SourceInterval[0];
        } else if (endIndex <= subtrahend.startIndex || startIndex >= subtrahend.endIndex) {
            // no overlap with the subtrahend
            return new SourceInterval[]{this};
        } else if (startIndex < subtrahend.startIndex && endIndex > subtrahend.endIndex) {
            // we are split by the subtrahend
            return new SourceInterval[]{
                    new SourceInterval(sourceString, startIndex, subtrahend.startIndex),
                    new SourceInterval(sourceString, subtrahend.endIndex, endIndex),};
        } else if (startIndex >= subtrahend.startIndex) {
            // subtrahend overlaps with our start
            return new SourceInterval[]{new SourceInterval(sourceString, subtrahend.endIndex, endIndex)};
        } else {
            // subtrahend overlaps with our end
            return new SourceInterval[]{
                    new SourceInterval(sourceString, startIndex, subtrahend.startIndex)};
        }
    }
    
    /**
     * Returns a new interval with this interval's extent, but relative to the start index of
     * {@code interval}.
     *
     * @param anchor The interval the returned interval will be relative to. Must have the same source
     * string as the receiver.
     */
    public SourceInterval relativeTo(SourceInterval anchor) {
        Objects.requireNonNull(anchor);
        assertSameSource(anchor);
        anchor.assertCovers(this);
        
        return new SourceInterval(sourceString, startIndex - anchor.startIndex,
                endIndex - anchor.startIndex);
    }
    
    /**
     * Returns a new interval which contains the same contents as the receiver, but with whitespace
     * trimmed from both ends.
     */
    public SourceInterval trimmed() {
        int left = startIndex;
        int right = endIndex;
        
        while (left < right && Character.isWhitespace(sourceString.charAt(right - 1))) {
            right--;
        }
        
        while (left < right && Character.isWhitespace(sourceString.charAt(left))) {
            left++;
        }
        
        return new SourceInterval(sourceString, left, right);
    }
    
    /**
     * Returns a new interval of length {@code length} starting at the receiver's startIndex offset by
     * {@code offset}.
     */
    public SourceInterval subInterval(int offset, int length) {
        int newStartIndex = startIndex + offset;
        int newEndIndex = newStartIndex + length;
        return new SourceInterval(sourceString, newStartIndex, newEndIndex);
    }
    
    private void assertSameSource(SourceInterval interval) {
        if (!sourceString.equals(interval.sourceString)) {
            throw new RuntimeException("Interval sources don't match: \"%1$s\" != \"%2$s\""
                    .formatted(sourceString, interval.sourceString));
        }
    }
    
    /**
     * Returns whether {@code otherInterval} is a sub-interval of the receiver.
     */
    public boolean covers(SourceInterval otherInterval) {
        Objects.requireNonNull(otherInterval);
        assertSameSource(otherInterval);
        return startIndex <= otherInterval.startIndex && endIndex >= otherInterval.endIndex;
    }
    
    private void assertCovers(SourceInterval otherInterval) {
        if (startIndex > otherInterval.startIndex || endIndex < otherInterval.endIndex) {
            throw new RuntimeException("%1$s does not cover %2$s".formatted(this, otherInterval));
        }
    }
    
    /**
     * Returns the shortest interval that covers all argument intervals. All intervals must have the
     * same source string.
     */
    public static SourceInterval cover(SourceInterval firstInterval, SourceInterval... intervals) {
        int startIndex = firstInterval.startIndex;
        int endIndex = firstInterval.endIndex;
        
        for (var interval : intervals) {
            firstInterval.assertSameSource(interval);
            startIndex = Math.min(startIndex, interval.startIndex);
            endIndex = Math.max(endIndex, interval.endIndex);
        }
        
        return new SourceInterval(firstInterval.sourceString, startIndex, endIndex);
    }
}
