package net.stlutz.ohm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputStream {
	private final String source;
	private int position = 0;
	private int examinedLength = 0;

	public InputStream(String source) {
		super();
		this.source = source;
	}

	public int getExaminedLength() {
		return examinedLength;
	}

	public void setExaminedLength(int examinedLength) {
		this.examinedLength = examinedLength;
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

	public boolean atEnd() {
		// TODO: why? why here?
		examinedLength = Math.max(examinedLength, position + 1);
		return position >= source.length();
	}

	public char next() {
		examinedLength = Math.max(examinedLength, position + 1);
		return source.charAt(position++);
	}

	public int nextCharCode() {
		return (int) next();
	}

	public int nextCodePoint() {
		int codePoint = source.codePointAt(position);
		position += Character.charCount(codePoint);
		examinedLength = Math.max(examinedLength, position);
		return codePoint;
	}

	public int match(Pattern pattern) {
		if (position >= source.length()) {
			return -1;
		}
		Matcher matcher = pattern.matcher(source).region(position, source.length());
		if (matcher.lookingAt()) {
			return matcher.toMatchResult().end() - position;
		} else {
			return -1;
		}
	}

	public boolean matches(String str) {
		return matches(str, false);
	}

	public boolean matches(String str, boolean ignoreCase) {
		return source.regionMatches(ignoreCase, position, str, 0, str.length());
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
