package com.zhangyue.hella.common.job.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.HtmlTagFilter;
import com.zhangyue.hella.common.util.HttpUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.xjob.Xjob;

/**
 * @Descriptions The class HttpGetXjob.java's implementation：执行Http Get的job
 * @author scott 2013-8-19 上午11:06:40
 * @version 1.0
 */
public class HttpGetXjob implements Xjob {

    private static Logger LOG = LoggerFactory.getLogger(HttpGetXjob.class);

    private int progress = 0;
    private String runInfo;
    private Date jobRunDate;
    private JobPlanNodeState jobPlanNodeState;

    @Override
    public void execute(JobEvent jobEvent) {
        /** 数据校验 */
        if (null == jobEvent.executionContent || jobEvent.executionContent.equals("")) {
            LOG.error(" executionContent  is not find.");
            this.progress = Constant.PROGRESS_ERROR;
            this.runInfo = "executionContent is not find.";
            this.jobPlanNodeState = JobPlanNodeState.DISPATCH_FAIL;
            this.jobRunDate = new Date();
            return;
        }
        LOG.info("Execute Http Get  Xjob ... " + jobEvent.executorClusterID + "|" + jobEvent.executionContent);
        try {
            this.jobPlanNodeState = JobPlanNodeState.DISPATCH_SUCCESS;
            this.jobRunDate = new Date();
            String rs = HttpUtil.sendGet(jobEvent.executionContent);
            if (null != rs) {
                if (rs.length() > Constant.XJOB_LOG_MAX_LENGTH) {
                    this.runInfo = rs.toString().substring(0, Constant.XJOB_LOG_MAX_LENGTH).trim();
                } else {
                    this.runInfo = rs.toString().trim();
                }
            }
            this.runInfo = HtmlTagFilter.Html2Text(this.runInfo);// Html标签转型入mysql库
            this.progress = Constant.PROGRESS_SUCCESS;
            this.jobPlanNodeState = JobPlanNodeState.RESULT_SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            this.progress = Constant.PROGRESS_ERROR;
            this.jobPlanNodeState = JobPlanNodeState.RESULT_ERROR;
        }

        /** 设置执行完成时间 阻塞执行 */
        this.jobRunDate = new Date();
    }

    @Override
    public void destroy() {
        LOG.info(" Http Get Xjob not need destroy");
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public String getRunInfo() {
        return runInfo;
    }

    @Override
    public Date getJobRunDate() {
        return jobRunDate;
    }

    @Override
    public JobPlanNodeState getJobPlanNodeState() {
        return jobPlanNodeState;
    }

    public static void main(String args[]) {
        String url = "http://localhost:8080/acorn/alarm/executeAlarm.action";

        try {
            String rs = HttpUtil.sendGet(url);
            System.out.println(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
