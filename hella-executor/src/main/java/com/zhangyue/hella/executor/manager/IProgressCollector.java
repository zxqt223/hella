/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.executor.manager;

import java.util.List;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.executor.client.impl.XjobRunner;

/**
 * Descriptions of the class IProgressCollector.java's implementation：TODO described the implementation of class
 * @date 2014-1-26
 * @author scott
 */
public interface IProgressCollector {

    /**
     * 获取作业进度列表
     * @return
     */
    public List<JobProgress> getJobProgressList();
    /**
     * 注册Xjob运行器
     * @param xjobRunner
     */
    public void add(XjobRunner xjobRunner);
    
    /**
     * 清理作业进度信息
     * @param jps
     */
    public void clear(List<JobProgress> jps);
    
    /**
     * 清理指定事件的作业进度信息
     * @param eventKey
     */
    public void clear(String eventKey);
}
