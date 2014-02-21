package com.zhangyue.hella.common.util;
 
 
public enum JobPlanType {
 
	local("本地"), distributed("分布式");
	/** 类型名称 */
	private final String typeName;
 
	/**
	 * @param stateName {@link #stateName}
	 */
	private JobPlanType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return {@link #typeName}
	 */
	public String getTypeName() {
		return typeName;
	}
}
