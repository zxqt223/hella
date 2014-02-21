package com.zhangyue.hella.common.util;


/**
 * 
 * Description: xjob执行类型：自动则会触发下级依赖job执行，一次则是之执行本次job.<br>
 * 
 * Copyright: Copyright (c) 2012 <br>
 * Company: www.renren.com
 * 
 * @author zhuhui{hui.zhu@renren-inc.com} 2012-8-17
 * @version 1.0
 */
public enum JobPlanNodeType {

	start("开始"), action("活动"), fork("分支"), join("合并"), fail("失败"), end("结束");

	/** 类型名称 */
	private final String typeName;

	public final static JobPlanNodeType[] finalTypes = { action, fail, end };

	/**
	 * @param typeName
	 *            {@link #typeName}
	 */
	private JobPlanNodeType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return {@link #typeName}
	 */
	public String getTypeName() {
		return typeName;
	}

	public boolean isFinalType() {
		for (JobPlanNodeType t : finalTypes) {
			if (this == t) {
				return true;
			}
		}
		return false;
	}

}
