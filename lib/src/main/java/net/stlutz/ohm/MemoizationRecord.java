package net.stlutz.ohm;

import java.util.function.Predicate;

import net.stlutz.ohm.pexprs.Apply;

public class MemoizationRecord {
	private int matchLength = 0;
	private int examinedLength = 0;
	private ParseNode value = null;
	private int rightmostFailureOffset = -1;

	private boolean isLeftRecursion = false;
	private Apply headApplication;
	private MemoizationRecord nextLeftRecursion;
	private Runnable updateInvolvedApplicationMemoKeys;
	private Predicate<String> isInvolved;

	public MemoizationRecord() {
		super();
	}

	public int getMatchLength() {
		return matchLength;
	}

	public void setMatchLength(int matchLength) {
		this.matchLength = matchLength;
	}

	public int getExaminedLength() {
		return examinedLength;
	}

	public void setExaminedLength(int examinedLength) {
		this.examinedLength = examinedLength;
	}

	public ParseNode getValue() {
		return value;
	}

	public void setValue(ParseNode value) {
		this.value = value;
	}

	public int getRightmostFailureOffset() {
		return rightmostFailureOffset;
	}

	public void setRightmostFailureOffset(int rightmostFailureOffset) {
		this.rightmostFailureOffset = rightmostFailureOffset;
	}

	public boolean isInvolved(String memoKey) {
		return isInvolved.test(memoKey);
	}

	public void setIsInvolved(Predicate<String> isInvolved) {
		this.isInvolved = isInvolved;
	}

	public void updateInvolvedApplicationMemoKeys() {
		updateInvolvedApplicationMemoKeys.run();
	}

	public void setUpdateInvolvedApplicationMemoKeys(Runnable updateInvolvedApplicationMemoKeys) {
		this.updateInvolvedApplicationMemoKeys = updateInvolvedApplicationMemoKeys;
	}

	public boolean isLeftRecursion() {
		return isLeftRecursion;
	}

	public void setLeftRecursion(boolean isLeftRecursion) {
		this.isLeftRecursion = isLeftRecursion;
	}

	public Apply getHeadApplication() {
		return headApplication;
	}

	public void setHeadApplication(Apply headApplication) {
		this.headApplication = headApplication;
	}

	public MemoizationRecord getNextLeftRecursion() {
		return nextLeftRecursion;
	}

	public void setNextLeftRecursion(MemoizationRecord nextLeftRecursion) {
		this.nextLeftRecursion = nextLeftRecursion;
	}
}
