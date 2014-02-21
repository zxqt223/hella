package com.zhangyue.hella.common.job.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.xjob.Xjob;

/**
 * 执行shell脚本的job
 * 
 * @author scott 2013-8-19 上午11:07:19
 * @version 1.0
 */
public class ShellXjob implements Xjob {

    private static Logger LOG = LoggerFactory.getLogger(ShellXjob.class);
    private int progress = Constant.PROGRESS_INIT;
    private String runInfo;
    private Date jobRunDate;
    private JobPlanNodeState jobPlanNodeState = JobPlanNodeState.INIT;
    private Process process;

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

    public void execute(JobEvent jobEvent) {
        String shellFile = jobEvent.executionContent;
        String args = jobEvent.argsValue;
        /** 数据校验 */
        if (null == shellFile || shellFile.equals("")) {
            LOG.error("ShellCommand shellFile is not find.");
            this.progress = Constant.PROGRESS_ERROR;
            this.runInfo = "It does not find shellFile for ShellCommand.";
            this.jobPlanNodeState = JobPlanNodeState.DISPATCH_FAIL;
            this.jobRunDate = new Date();
            return;
        }

        LOG.info("It executes shell Xjob.executorClusterID:" + jobEvent.executorClusterID + ",executeUser:"
                 + jobEvent.executeUser + "," + "args" + jobEvent.argsValue + ",script:" + shellFile);
        executeshell(jobEvent.jobPlanNodeName, jobEvent.executeUser, shellFile, args);

        /** 设置执行完成时间 阻塞执行 */
        jobRunDate = new Date();
    }

    public void destroy() {
        if (null != process) {
            process.destroy();
            try {
                process.getErrorStream().close();
            } catch (IOException e) {
                LOG.error("Fail to close error stream!", e);
            }
            try {
                process.getInputStream().close();
            } catch (IOException e) {
                LOG.error("Fail to close input stream!", e);
            }
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                LOG.error("Fail to close output stream!", e);
            }
        }
    }

    @SuppressWarnings("finally")
    private void executeshell(String jobPlanNodeName, String executeUser, String shellFile, String args) {
        StringBuilder infoDesc = new StringBuilder();
        StringBuilder echoDesc = new StringBuilder();
        // 1.获取脚本文件
        LOG.info("[" + jobPlanNodeName + "]:Step one gets shell file.");
        File shell = null;
        try {
            shell = new File(shellFile);
        } catch (Exception e) {
            this.progress = Constant.PROGRESS_ERROR;
            this.runInfo = " shellFile is not find.";
            this.jobPlanNodeState = JobPlanNodeState.DISPATCH_FAIL;
            this.jobRunDate = new Date();
            LOG.error("not find this shell,please check out shell path");
            return;
        }

        // 2.组装脚本命令
        LOG.info("[" + jobPlanNodeName + "]:Step two gets shell command.");
        StringBuffer command = new StringBuffer();
        if (null != executeUser && !"".equals(executeUser)) {
            command.append(" sudo -u " + executeUser + " ");
        }
        command.append(" bash " + shellFile);
        if (null != args && !"".equals(args)) {
            command.append(" " + args);
        }
        LOG.info("[" + jobPlanNodeName + "]:Step two command:" + command.toString());

        // 3.执行脚本命令
        LOG.info("[" + jobPlanNodeName + "]:Step three executes shell command.");
        try {
            this.process = Runtime.getRuntime().exec(command.toString(), null, shell.getParentFile());
            this.jobPlanNodeState = JobPlanNodeState.DISPATCH_SUCCESS;
            this.jobRunDate = new Date();
        } catch (Exception e) {
            LOG.error("Execute " + shellFile + " exception:", e.getMessage());
            /** 设置JOB进度 **/
            this.progress = Constant.PROGRESS_ERROR;
            infoDesc.append(e.getMessage());
            return;
        }

        // 4.异步输出脚本执行信息
        LOG.info("[" + jobPlanNodeName + "]:Step four gets shell InputStream.");
        while (true) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                for (String str = br.readLine(); str != null; str = br.readLine()) {
                    LOG.info("[" + jobPlanNodeName + "]" + str);
                    echoDesc.append(str);
                    if (this.canCollector(str)) {
                        infoDesc.append(str.substring(Constant.JOB_LOG_START.length(), str.length()));
                    }
                }
                if ("".equals(infoDesc.toString())) {
                    infoDesc = echoDesc;
                }
                LOG.info("[" + jobPlanNodeName + "],Input info : " + echoDesc);
            } catch (IOException e) {
                LOG.error("Fail to read info from input stream!", e);
                /** 设置JOB进度 **/
                this.progress = Constant.PROGRESS_ERROR;
                infoDesc.append(e.getMessage());
            } finally {
                try {
                    if (null != br) {
                        br.close();
                    }
                } catch (IOException e) {
                }
                break;
            }
        }

        // 5.获取脚本执行状态 非0则脚本异常并获取脚本异常信息
        LOG.info("[" + jobPlanNodeName + "]:Step five gets the exit value of the process.");
        BufferedReader brError = null;
        try {
            int iretCode = process.waitFor();

            LOG.info("[" + jobPlanNodeName + "], The exit value:" + iretCode);

            /** 出现错误收集错误信息 */
            if (iretCode != 0) {
                brError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorDesc = new StringBuilder();
                for (String str = brError.readLine(); str != null; str = brError.readLine()) {
                    errorDesc.append(str);
                }
                /** 设置JOB进度 **/
                this.progress = Constant.PROGRESS_ERROR;
                this.jobPlanNodeState = JobPlanNodeState.RESULT_ERROR;
                infoDesc.append("[" + jobPlanNodeName + "] shellCode:" + iretCode + "|" + errorDesc.toString());
                LOG.error(infoDesc.toString());
            } else {
                /** 设置JOB进度 **/
                this.progress = Constant.PROGRESS_SUCCESS;
                this.jobPlanNodeState = JobPlanNodeState.RESULT_SUCCESS;
            }
        } catch (Exception e) {
            LOG.error("IOException:" + e.getMessage());
            /** 设置JOB进度 **/
            this.progress = Constant.PROGRESS_ERROR;
            infoDesc.append(e.getMessage());
        } finally {
            try {
                if (null != brError) {
                    brError.close();
                }
                process.getInputStream().close();
                process.getOutputStream().close();
                process.getErrorStream().close();
            } catch (IOException e) {
                // IGNORE
            }

        }

        if (infoDesc.length() > Constant.XJOB_LOG_MAX_LENGTH) {
            runInfo = infoDesc.toString().substring(0, Constant.XJOB_LOG_MAX_LENGTH);
        } else {
            runInfo = infoDesc.toString();
        }
    }

    /***
     * 过滤该内容是否可以收集
     * 
     * @param content
     * @return
     */
    private boolean canCollector(String content) {
        if (null != content && !"".equals(content)) {
            if (content.trim().startsWith(Constant.JOB_LOG_START)) {
                return true;
            }
        }
        return false;
    }

    public JobPlanNodeState getJobPlanNodeState() {
        return jobPlanNodeState;
    }
}
