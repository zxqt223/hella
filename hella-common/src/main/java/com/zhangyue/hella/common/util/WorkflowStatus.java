package com.zhangyue.hella.common.util;

public enum WorkflowStatus {
	PREP(false), RUNNING(false), SUSPENDED(false), SUCCEEDED(true), FAILED(true);
	private boolean isEndState;

	private WorkflowStatus(boolean isEndState) {
		this.isEndState = isEndState;
	}

	public boolean isEndState() {
		return isEndState;
	}
}
