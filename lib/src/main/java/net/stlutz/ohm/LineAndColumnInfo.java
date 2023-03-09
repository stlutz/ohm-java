package net.stlutz.ohm;

public class LineAndColumnInfo {

  public int offset;
  public int lineNum;
  public int columnNum;
  public String line;
  public String previousLine;
  public String nextLine;

  public LineAndColumnInfo(int offset, int lineNum, int columnNum, String line, String previousLine,
      String nextLine) {
    // TODO: replace with message
    super();
    this.offset = offset;
    this.lineNum = lineNum;
    this.columnNum = columnNum;
    this.line = line;
    this.previousLine = previousLine;
    this.nextLine = nextLine;
  }

  @Override
  public String toString() {
    return "LineAndColumnInfo [offset=" + offset + ", lineNum=" + lineNum + ", columnNum="
        + columnNum + ", line=" + line + ", previousLine=" + previousLine + ", nextLine=" + nextLine
        + "]";
  }

  public static LineAndColumnInfo from(String str, int offset) {
    int lineNum = 1;
    int columnNum = 1;

    int currentOffset = 0;
    int lineStartOffset = 0;

    String nextLine = null;
    String previousLine = null;
    int previousLineStartOffset = -1;

    while (currentOffset < offset) {
      char c = str.charAt(currentOffset++);
      if (c == '\n') {
        lineNum++;
        columnNum = 1;
        previousLineStartOffset = lineStartOffset;
        lineStartOffset = currentOffset;
      } else if (c != '\r') {
        columnNum++;
      }
    }

    // Find the end of the target line
    int lineEndOffset = str.indexOf('\n', lineStartOffset);
    if (lineEndOffset == -1) {
      lineEndOffset = str.length();
    } else {
      // Get the next line
      int nextLineEndOffset = str.indexOf('\n', lineEndOffset + 1);
      nextLine = nextLineEndOffset == -1 ? str.substring(lineEndOffset)
          : str.substring(lineEndOffset, nextLineEndOffset);
      // Strip leading and trailing EOL char(s)
      nextLine = nextLine.replaceAll("^\r?\n", "");
      nextLine = nextLine.replaceAll("\r&", "");
    }

    // Get the previous line
    if (previousLineStartOffset >= 0) {
      previousLine = str.substring(previousLineStartOffset, lineStartOffset);
      // Strip trailing EOL char(s)
      previousLine = previousLine.replaceAll("\r?\n$", "");
    }

    // Get the target line
    String line = str.substring(lineStartOffset, lineEndOffset);
    // Strip a trailing carriage return if necessary
    line = line.replaceAll("\r$", "");

    return new LineAndColumnInfo(offset, lineNum, columnNum, line, previousLine, nextLine);
  }
}
