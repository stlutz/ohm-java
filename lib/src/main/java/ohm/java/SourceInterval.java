package ohm.java;

import java.util.Objects;

public class SourceInterval {
	public String sourceString;
	public int startIndex;
	public int endIndex;
	private String contents;

	public SourceInterval(String sourceString, int startIndex, int endIndex) {
		super();
		this.sourceString = sourceString;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SourceInterval))
			return false;
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
		return "SourceInterval [startIndex=" + startIndex + ", endIndex=" + endIndex + ", contents=" + getContents()
				+ "]";
	}

	public String getContents() {
		if (Objects.isNull(contents)) {
			contents = sourceString.substring(startIndex, endIndex);
		}
		return contents;
	}

	public int length() {
		return endIndex - startIndex;
	}

	public SourceInterval coverageWith(SourceInterval... intervals) {
		return coverage(this, intervals);
	}

	public SourceInterval collapsedLeft() {
		return new SourceInterval(sourceString, startIndex, startIndex);
	}

	public SourceInterval collapsedRight() {
		return new SourceInterval(sourceString, endIndex, endIndex);
	}

	public LineAndColumnInfo getLineAndColumn() {
		return LineAndColumnInfo.from(sourceString, startIndex);
	}

	public String getLineAndColumnMessage() {
		// TODO
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Subtract the specified interval from the receiver.
	 * 
	 * @param the interval to be subtracted. Must reference the same source string.
	 * @return an array of 0, 1, or 2 intervals
	 */
	public SourceInterval[] minus(SourceInterval sub) {
		assertSameSource(sub);

		if (startIndex >= sub.startIndex && endIndex <= sub.endIndex) {
			// we are a subset of the subtrahend
			return new SourceInterval[0];
		} else if (endIndex <= sub.startIndex || startIndex >= sub.endIndex) {
			// no overlap with the subtrahend
			return new SourceInterval[] { this };
		} else if (startIndex < sub.startIndex && endIndex > sub.endIndex) {
			// we are split by the subtrahend
			return new SourceInterval[] { new SourceInterval(sourceString, startIndex, sub.startIndex),
					new SourceInterval(sourceString, sub.endIndex, endIndex), };
		} else if (startIndex >= sub.startIndex) {
			// subtrahend overlaps with our start
			return new SourceInterval[] { new SourceInterval(sourceString, sub.endIndex, endIndex) };
		} else {
			// subtrahend overlaps with our end
			return new SourceInterval[] { new SourceInterval(sourceString, startIndex, sub.startIndex) };
		}
	}

	/**
	 * @param interval
	 * @return new interval with the receiver's extent, but relative to
	 *         {@code interval}
	 */
	public SourceInterval relativeTo(SourceInterval interval) {
		assertSameSource(interval);
		interval.assertCoverage(this);

		return new SourceInterval(sourceString, startIndex - interval.startIndex, endIndex - interval.startIndex);
	}

	/**
	 * Returns a new Interval which contains the same contents as the receiver, but
	 * with whitespace trimmed from both ends.
	 */
	public SourceInterval trimmed() {
		int left = startIndex;
		int right = endIndex;

		while (left < right && Character.isWhitespace(sourceString.charAt(right - 1)))
			right--;

		while (left < right && Character.isWhitespace(sourceString.charAt(left)))
			left++;

		return new SourceInterval(sourceString, left, right);
	}

	public SourceInterval subInterval(int offset, int length) {
		int newStartIndex = startIndex + offset;
		return new SourceInterval(sourceString, newStartIndex, newStartIndex + length);
	}

	protected void assertSameSource(SourceInterval interval) {
		if (!sourceString.equals(interval.sourceString)) {
			throw new RuntimeException(
					"Interval sources don't match: \"%s\" != \"%s\"".formatted(sourceString, interval.sourceString));
		}
	}

	/**
	 * Assert that the receiver covers {@code interval}
	 */
	protected void assertCoverage(SourceInterval interval) {
		if (startIndex > interval.startIndex || endIndex < interval.endIndex) {
			throw new RuntimeException("%s does not cover %s".formatted(this.toString(), interval.toString()));
		}
	}

	public static SourceInterval coverage(SourceInterval firstInterval, SourceInterval... intervals) {
		int startIndex = firstInterval.startIndex;
		int endIndex = firstInterval.endIndex;

		for (SourceInterval interval : intervals) {
			firstInterval.assertSameSource(interval);
			startIndex = Math.min(startIndex, interval.startIndex);
			endIndex = Math.max(endIndex, interval.endIndex);
		}

		return new SourceInterval(firstInterval.sourceString, startIndex, endIndex);
	}
}
