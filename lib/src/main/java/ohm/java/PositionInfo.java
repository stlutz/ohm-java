package ohm.java;

import java.util.*;

import ohm.java.pexprs.Apply;

public class PositionInfo {
	// stack top at last
	private final List<String> applicationMemoKeyStack = new ArrayList<>();
	private final Map<String, MemoizationRecord> memo = new HashMap<>();
	private int maxExaminedLength = 0;
	private int maxRightmostFailureOffset = -1;
	private MemoizationRecord currentLeftRecursion = null;

	PositionInfo() {
		super();
	}

	public MemoizationRecord getCurrentLeftRecursion() {
		return currentLeftRecursion;
	}

	public boolean isActive(Apply application) {
		return applicationMemoKeyStack.contains(application.toMemoKey());
	}

	public void enter(Apply application) {
		applicationMemoKeyStack.add(application.toMemoKey());
	}

	public void exit() {
		applicationMemoKeyStack.remove(applicationMemoKeyStack.size() - 1);
	}

	public void startLeftRecursion(Apply headApplication, MemoizationRecord memoRec) {
		memoRec.setLeftRecursion(true);
		memoRec.setHeadApplication(headApplication);
		memoRec.setNextLeftRecursion(currentLeftRecursion);
		currentLeftRecursion = memoRec;

		int indexOfFirstInvolvedRule = applicationMemoKeyStack.indexOf(headApplication.toMemoKey()) - 1;
		List<String> involvedApplicationMemoKeys = new ArrayList<>(
				applicationMemoKeyStack.subList(indexOfFirstInvolvedRule, applicationMemoKeyStack.size()));

		// TODO: This seems like it could be done much cleaner differently. For now just
		// copied 1:1 from ohm-js
		memoRec.setIsInvolved((applicationMemoKey) -> {
			return involvedApplicationMemoKeys.contains(applicationMemoKey);
		});

		memoRec.setUpdateInvolvedApplicationMemoKeys(() -> {
			for (int i = indexOfFirstInvolvedRule; i < applicationMemoKeyStack.size(); i++) {
				String applicationMemoKey = applicationMemoKeyStack.get(i);
				if (memoRec.isInvolved(applicationMemoKey)) {
					involvedApplicationMemoKeys.add(applicationMemoKey);
				}
			}
		});
	}

	public void endLeftRecursion() {
		currentLeftRecursion = currentLeftRecursion.getNextLeftRecursion();
	}

	public boolean shouldUseMemoizedResult(MemoizationRecord memoRec) {
		// Note: this method doesn't get called for the "head" of a left recursion --
		// for LR heads, the memoized result (which starts out being a failure) is
		// always used.
		if (!memoRec.isLeftRecursion()) {
			return true;
		}

		for (String applicationMemoKey : applicationMemoKeyStack) {
			if (memoRec.isInvolved(applicationMemoKey)) {
				return false;
			}
		}

		return true;
	}

	public MemoizationRecord remember(String memoKey) {
		return memo.get(memoKey);
	}

	public MemoizationRecord memoize(String memoKey) {
		MemoizationRecord memoRec = new MemoizationRecord();
		memo.put(memoKey, memoRec);
		return memoRec;
	}

	public MemoizationRecord memoize(String memoKey, MemoizationRecord memoRec) {
		memo.put(memoKey, memoRec);
		return memoRec;
	}

	public void forget(String memoKey) {
		memo.remove(memoKey);
	}

}
