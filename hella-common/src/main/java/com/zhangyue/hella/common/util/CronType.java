package com.zhangyue.hella.common.util;


/**
 * QJob类型
 * 
 */
public enum CronType {
 
	simple("简单定时"), cron("复杂定时"),event("事件触发");
	/** 类型名称 */
	private final String typeName;
 
	/**
	 * @param typeName {@link #typeName}
	 */
	private CronType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return {@link #typeName}
	 */
	public String getTypeName() {
		return typeName;
	}
}
