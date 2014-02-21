package com.zhangyue.hella.common.util;

/**
 * @Descriptions The class XjobMode.java's
 *               implementation：xjob执行类型：自动则会触发下级依赖job执行，一次则是之执行本次job.
 * @author scott 2013-8-19 上午11:45:54
 * @version 1.0
 */
public enum XjobMode {

    script("脚本", "com.renren.sched.common.job.impl.ShellXjob"), httpGet("http get请求",
                                                                     "com.renren.sched.common.job.impl.HttpGetXjob"),
    httpPost("http post请求", "com.renren.sched.common.job.impl.HttpPostXjob"), hive("hive查询", null);

    /** 类型名称 */
    private final String typeName;
    private final String jobClassName;

    /**
     * @param stateName {@link #stateName}
     */
    private XjobMode(String typeName, String jobClassName){
        this.typeName = typeName;
        this.jobClassName = jobClassName;
    }

    /**
     * @return {@link #typeName}
     */
    public String getTypeName() {
        return typeName;
    }

    public String getJobClassName() {
        return jobClassName;
    }

}
