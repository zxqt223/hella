package com.zhangyue.hella.common.util;
 
/**
 * 
 * @Descriptions The class XjobExecutorType.java's implementation： xjob执行类型：自动则会触发下级依赖job执行，一次则是之执行本次job.
 * @author scott 2013-8-19 上午11:45:19
 * @version 1.0
 */
public enum XjobExecutorType {
 
	Auto("自动"), Once("一次");
	/** 类型名称 */
	private final String typeName;
 
	/**
	 * @param stateName {@link #stateName}
	 */
	private XjobExecutorType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return {@link #typeName}
	 */
	public String getTypeName() {
		return typeName;
	}
}
