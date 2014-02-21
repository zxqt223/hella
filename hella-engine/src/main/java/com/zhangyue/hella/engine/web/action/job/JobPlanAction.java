package com.zhangyue.hella.engine.web.action.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.core.LogCollector;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.engine.web.action.BaseAction;
import com.zhangyue.hella.engine.web.service.JobPlanService;
import com.zhangyue.hella.engine.web.service.ServiceFactory;

public class JobPlanAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(JobPlanAction.class);
    private JobPlanService jobPlanService = ServiceFactory.getServiceInstance(JobPlanService.class);

    public void index(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String forward = req.getParameter("forward");
        String clusterID = req.getParameter("clusterID");
        req.setAttribute("clusterID", clusterID);
        String currentState = req.getParameter("currentState");

        req.setAttribute("stateList", JobExecutionPlan.State.values());
        req.setAttribute("currentState", currentState);
        req.setAttribute("engineState", jobPlanService.getEngineNodeState());

        List<String> clusterIDs = jobPlanService.getClusterIDs();
        req.setAttribute("allocateJobPlanExecutorClusterIDs", clusterIDs);
        List<NodeInfo> executorNodes = jobPlanService.getExecutorClusterInfoList();
        List<String> allExecutorClusterIDs = new ArrayList<String>();
        if (executorNodes != null) {
            for (NodeInfo ni : executorNodes) {
                allExecutorClusterIDs.add(ni.getClusterID());
            }
        }
        req.setAttribute("allExecutorClusterIDs", allExecutorClusterIDs);
        req.getRequestDispatcher("/jsp/" + forward).forward(req, res);
    }

    public void list(HttpServletRequest req, HttpServletResponse res) {
        String pageNum = req.getParameter("page");
        String pageSize = req.getParameter("pagesize");
        int pageNumInt = 1;
        int pageSizeInt = 15;

        if (StringUtils.isNotBlank(pageNum)) {
            pageNumInt = Integer.valueOf(pageNum);
        }
        if (StringUtils.isNotBlank(pageSize)) {
            pageSizeInt = Integer.valueOf(pageSize);
        }

        Page page = new Page(pageNumInt, pageSizeInt);

        Map<String, Object> parameters = super.getQueryParameters(req);
        String clusterID = (String) parameters.get("clusterID");
        String jepName = (String) parameters.get("jepName");
        String state = (String) parameters.get("state");
        JobExecutionPlan.State stateEnum = null;
        if (StringUtils.isNotBlank(state)) {
            stateEnum = JobExecutionPlan.State.valueOf(state);
        }
        page = jobPlanService.queryJobPlans(page, clusterID, jepName, stateEnum);
        @SuppressWarnings("unchecked")
        List<JobExecutionPlan> jplist = page.getResult();
        JSONObject dateObj = new JSONObject();
        JSONArray data = new JSONArray();
        if (null != jplist && jplist.size() > 0) {
            for (JobExecutionPlan jp : jplist) {
                JSONObject jpJSON = new JSONObject();
                jpJSON.put("id", jp.getId());
                jpJSON.put("clusterID", jp.getClusterID());
                jpJSON.put("jobPlanType", jp.getJobPlanTypeEnum().getTypeName());
                jpJSON.put("jepID", jp.getJepID());
                jpJSON.put("jepName", jp.getJepName());
                jpJSON.put("period", jp.getCronTypeEnum().getTypeName() + ":" + jp.getCronExpression());
                jpJSON.put("event", jp.getEvent());
                jpJSON.put("createDate", DateUtil.parseCnDate(jp.getCreateDate()));
                jpJSON.put("executePlanVersion", jp.getExecutePlanVersion());
                jpJSON.put("state", jp.getState());
                jpJSON.put("stateName", jp.getStateEnum().getStateName());
                if (StringUtils.isNotBlank(jp.getCurrentXjobDate()) && StringUtils.isNotBlank(jp.getCurrentXjobState())) {
                    jpJSON.put("currentXjobState", jp.getCurrentXjobDateFormater() + "-"
                                                   + jp.getCurrentXjobStateEnum().getStateName());
                } else {
                    jpJSON.put("currentXjobState", "--");
                }
                jpJSON.put("ignoreError", jp.isIgnoreError() == true ? "YSE" : "NO");
                jpJSON.put("currentXjob", jp.getCurrentNode());
                jpJSON.put("description", jp.getDescription());
                data.add(jpJSON);
            }
        }
        dateObj.put("Rows", data);
        dateObj.put("Total", page.getTotalCount());
        LOG.debug(dateObj.toString());
        try {
            super.ajaxOutPutJson(res, dateObj.toString());
        } catch (Exception e) {
            LOG.error("Fail to do output!", e);
        }
    }

    public void loadJEP(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (null != req.getParameter("jobExecutionPlanID")) {
            JobExecutionPlan jobExecutionPlan;
            try {
                jobExecutionPlan =
                        jobPlanService.loadJobExecutionPlan(Integer.valueOf(req.getParameter("jobExecutionPlanID")));
                String createDate = jobExecutionPlan.getCreateDate();
                if (StringUtils.isNotBlank(createDate)) {
                    jobExecutionPlan.setCreateDate(DateUtil.parseCnDate(createDate));
                }
                req.setAttribute("jobExecutionPlan", jobExecutionPlan);
            } catch (Exception e) {
                LOG.error("Fail to load job execution plan.", e);
            }
        }
        req.getRequestDispatcher("/jsp/job/jobplan.jsp").forward(req, res);
    }

    public void enterEdit(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (null != req.getParameter("jobExecutionPlanID")) {
            JobExecutionPlan jobExecutionPlan;
            try {
                jobExecutionPlan =
                        jobPlanService.loadJobExecutionPlan(Integer.valueOf(req.getParameter("jobExecutionPlanID")));
                String createDate = jobExecutionPlan.getCreateDate();
                if (StringUtils.isNotBlank(createDate)) {
                    jobExecutionPlan.setCreateDate(DateUtil.parseCnDate(createDate));
                }
                req.setAttribute("jobExecutionPlan", jobExecutionPlan);
            } catch (Exception e) {
                LOG.error("Fail to load job execution plan from database!", e);
            }
        }
        req.getRequestDispatcher("/jsp/job/jobplan_edit.jsp").forward(req, res);
    }

    public void edit(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
            return;
        }
        if (null == req.getParameter("jobExecutionPlanID")) {
            super.ajaxOutJsonResult(res, false, "请选择要编辑的作业计划");
            return;
        }
        int jobExecutionPlanID = Integer.valueOf(req.getParameter("jobExecutionPlanID"));
        CronType cronType = CronType.valueOf(req.getParameter("cronType"));
        String cronExpression = req.getParameter("cronExpression");

        if (0 == jobExecutionPlanID) {
            super.ajaxOutJsonResult(res, false, "请选择要编辑的作业计划");
            return;
        }
        if (null == cronType) {
            super.ajaxOutJsonResult(res, false, "请选择要编辑的作业计划触发类型cronType");
            return;
        }
        if (StringUtils.isBlank(cronExpression)) {
            super.ajaxOutJsonResult(res, false, "请选择要编辑的作业计划触发时间cronExpression");
            return;
        }
        try {
            jobPlanService.changeJobExecutionPlanCronExpression(jobExecutionPlanID, cronType, cronExpression);
            super.ajaxOutJsonResult(res, true, "操作成功");
        } catch (Exception e) {
            LOG.error("Fail to change job execution plan cron expression.", e);
            super.ajaxOutJsonResult(res, false, "操作失败,请联系管理员");
        }
    }

    public void pauseJob(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String jobExecutionPlanID = req.getParameter("jobExecutionPlanID");
        if (StringUtils.isNotBlank(jobExecutionPlanID)) {
            boolean rs = false;
            rs = jobPlanService.pauseJob(Integer.valueOf(jobExecutionPlanID));
            if (rs) {
                super.ajaxOutJsonResult(res, true, "操作成功");
                String useName = (String) req.getSession(true).getAttribute(EngineConstant.USER_NAME);
                LogCollector.getSchedCollector().addSystemLog(
                    new SystemLog(useName, req.getRemoteAddr(), SystemLogType.userOperator.name(),
                        "pauseJob:jobExecutionPlanID=" + jobExecutionPlanID, DateUtil.dateFormaterBySeconds(new Date())));
            } else {
                super.ajaxOutJsonResult(res, false, "操作失败,请检查作业计划版本是否过期");
            }
        }
    }

    public void resumeJob(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String jobExecutionPlanID = req.getParameter("jobExecutionPlanID");
        if (StringUtils.isNotBlank(jobExecutionPlanID)) {
            boolean rs = false;
            rs = jobPlanService.resumeJob(Integer.valueOf(jobExecutionPlanID));
            if (rs) {
                super.ajaxOutJsonResult(res, true, "操作成功");
                String useName = (String) req.getSession(true).getAttribute(EngineConstant.USER_NAME);
                LogCollector.getSchedCollector().addSystemLog(
                    new SystemLog(useName, req.getRemoteAddr(), SystemLogType.userOperator.name(),
                        "resumeJob:jobExecutionPlanID=" + jobExecutionPlanID,
                        DateUtil.dateFormaterBySeconds(new Date())));

            } else {
                super.ajaxOutJsonResult(res, false, "操作失败,请检查作业计划版本是否过期");
            }
        }
    }

    public void executorQJob(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String jobExecutionPlanID = req.getParameter("jobExecutionPlanID");

        if (StringUtils.isNotBlank(jobExecutionPlanID)) {
            boolean rs = false;
            try {
                rs = jobPlanService.executeQJob(Integer.valueOf(jobExecutionPlanID));
            } catch (InitializationException e) {
                LOG.error("Fail to execute qjob,jobExecutionPlanID:" + jobExecutionPlanID, e);
            } catch (NumberFormatException e) {
                LOG.error("Fail to execute qjob,jobExecutionPlanID:" + jobExecutionPlanID, e);
            } catch (Exception e) {
                LOG.error("Fail to execute qjob,jobExecutionPlanID:" + jobExecutionPlanID, e);
            }
            if (rs) {
                super.ajaxOutJsonResult(res, true, "操作成功");
                String useName = (String) req.getSession(true).getAttribute(EngineConstant.USER_NAME);
                LogCollector.getSchedCollector().addSystemLog(
                    new SystemLog(useName, req.getRemoteAddr(), SystemLogType.userOperator.name(),
                        "executorQJob:jobExecutionPlanID=" + jobExecutionPlanID,
                        DateUtil.dateFormaterBySeconds(new Date())));

            } else {
                super.ajaxOutJsonResult(res, false, "操作失败,请检查该集群客户端是否正常");
            }
        }
    }

    public void executorXJob(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String jobPlanNodeID = req.getParameter("jobPlanNodeID");
        String args = req.getParameter("args");
        if (StringUtils.isNotBlank(jobPlanNodeID)) {
            boolean rs = false;
            try {
                rs = jobPlanService.executeJobPlanNode(Integer.valueOf(jobPlanNodeID), args);
            } catch (Exception e) {
                LOG.error("Fail to execute xjob,jobPlanNodeID:" + jobPlanNodeID, e);
            }
            if (rs) {
                super.ajaxOutJsonResult(res, true, "操作成功");
                String useName = (String) req.getSession(true).getAttribute(EngineConstant.USER_NAME);
                LogCollector.getSchedCollector().addSystemLog(
                    new SystemLog(useName, req.getRemoteAddr(), SystemLogType.userOperator.name(),
                        "executorXJob:jobPlanNodeID=" + jobPlanNodeID, DateUtil.dateFormaterBySeconds(new Date())));
            } else {
                super.ajaxOutJsonResult(res, false, "操作失败,请检查该集群是否正常");
            }
        }
    }

    public void deleteJobPlan(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String jobPlanID = req.getParameter("jobPlanID");
        if (StringUtils.isNotBlank(jobPlanID)) {
            boolean rs = false;
            try {
                int jid = Integer.valueOf(jobPlanID);
                String jobPlanState = jobPlanService.queryJobPlanState(jid);
                if (jobPlanState == null || jobPlanState.equals(JobExecutionPlan.State.able.toString())) {
                    super.ajaxOutJsonResult(res, false, "请先暂停执行计划后再删除");
                    return;
                }
                rs = jobPlanService.delJobExecutionPlan(Integer.valueOf(jobPlanID));
                if (rs) {
                    super.ajaxOutJsonResult(res, true, "操作成功");
                    String useName = (String) req.getSession(true).getAttribute(EngineConstant.USER_NAME);
                    LogCollector.getSchedCollector().addSystemLog(
                        new SystemLog(useName, req.getRemoteAddr(), SystemLogType.userOperator.name(),
                            "deleteJobPlan:JobExecutionPlanID=" + jobPlanID, DateUtil.dateFormaterBySeconds(new Date())));
                } else {
                    super.ajaxOutJsonResult(res, false, "操作失败,请检查该集群客户端是否正常");
                }
            } catch (Exception e) {
                LOG.error("Fail to delete job plan.", e);
            }
        }
    }

    // //////////////////
    private String uploadPath = "/upload/"; // 上传文件的目录
    private String tempPath = "/uploadtmp/"; // 临时文件目录
    private String serverPath = null;
    private int sizeMax = 3;
    private String[] fileType = new String[] { ".xml" };

    public void fileUpLoad(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        String clusterID = (String) request.getParameter("cID");
        if (StringUtils.isBlank(clusterID)) {
            request.setAttribute("msg", "clusterID is null");
            request.getRequestDispatcher("/jobplan?method=index&forward=job/jobplan_list.jsp").forward(request,
                response);
            return;
        }
        serverPath = getServletContext().getRealPath("/").replace("\\", "/");

        String jobPlanPath = serverPath + uploadPath + clusterID + "/";
        // Servlet初始化时执行,如果上传文件目录不存在则自动创建
        if (!new File(jobPlanPath).isDirectory()) {
            new File(jobPlanPath).mkdirs();
        }
        if (!new File(serverPath + tempPath).isDirectory()) {
            new File(serverPath + tempPath).mkdirs();
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(5 * 1024); // 最大缓存
        factory.setRepository(new File(serverPath + tempPath));// 临时文件目录

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(sizeMax * 1024 * 1024);// 文件最大上限

        String filePath = null;
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                // 获得文件名，这个文件名包括路径
                if (!item.isFormField()) {
                    // 文件名
                    String fileName = item.getName().toLowerCase();
                    if (fileName.endsWith(fileType[0])) {
                        filePath = jobPlanPath + DateUtil.dateFormaterBySeconds(new Date()) + "_" + fileName;
                        item.write(new File(filePath));
                        boolean validateRs = false;
                        boolean submitJobPlanRs = false;
                        try {
                            validateRs = jobPlanService.validateXMLJobPlan(filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!validateRs) {
                            request.setAttribute("msg", "fail to validate XML");
                        }
                        try {
                            if (validateRs) {
                                submitJobPlanRs = jobPlanService.submitJobPlan(clusterID, filePath);
                                if (submitJobPlanRs) {
                                    request.setAttribute("msg", "success to submit JobPlan");
                                } else {
                                    request.setAttribute("msg", "fail to submit JobPlan");
                                }
                            }
                        } catch (Exception e) {
                            LOG.warn("Fail to submit JobPlan,the exception : ", e);
                            request.setAttribute("msg",
                                "fail to submit JobPlan.this JobPlan may be exist or it happens error!");
                        }
                    } else {
                        request.setAttribute("msg", "upload fail");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Fail to upload file.", e);
            request.setAttribute("msg", "upload fail ,the sizeMax:" + sizeMax + "M");
        }
        request.getRequestDispatcher("/jobplan?method=index&forward=job/jobplan_list.jsp").forward(request, response);
    }

}
