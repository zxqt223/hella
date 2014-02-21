package com.zhangyue.hella.common.util;


/**
 * 系统状态
 * 
 */
public enum SchedState {
 
	standBy("初始化服务"), neutral("暂停服务"),active("正在服务");
	/** 类型名称 */
	private final String stateName;
 
	/**
	 * @param stateName {@link #stateName}
	 */
	private SchedState(String stateName) {
		this.stateName = stateName;
	}

	/**
	 * @return {@link #typeName}
	 */
	public String getStateName() {
		return stateName;
	}
}
