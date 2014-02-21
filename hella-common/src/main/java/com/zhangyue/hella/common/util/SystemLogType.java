package com.zhangyue.hella.common.util;

/**
 * 
 * @Descriptions The class SystemLogType.java's implementation：系统日志枚举类
 * @author scott 2013-8-19 上午11:44:37
 * @version 1.0
 */
public enum SystemLogType {
	sysInfo("system info"),
	sysException("system exception"),
	sysMail("mail notify"),
	sysMsg("mobile notify "), 
	adminOperator("admin operate"),
	userOperator("user operate");

	/** 类型名称 */
	private final String logType;

	/**
	 * @param stateName
	 *            {@link #logType}
	 */
	private SystemLogType(String logType) {
		this.logType = logType;
	}

	/**
	 * @return {@link #logType}
	 */
	public String getLogType() {
		return logType;
	}
}
