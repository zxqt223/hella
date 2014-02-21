package com.zhangyue.hella.engine.web.action.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.entity.XjobStateView;
import com.zhangyue.hella.engine.web.action.BaseAction;
import com.zhangyue.hella.engine.web.service.JobPlanService;
import com.zhangyue.hella.engine.web.service.JobStateService;
import com.zhangyue.hella.engine.web.service.ServiceFactory;

public class JobStateAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    protected final transient Logger logger = Logger.getLogger(JobStateAction.class);

    private JobStateService jobStateService = ServiceFactory.getServiceInstance(JobStateService.class);
    private JobPlanService jobPlanService = ServiceFactory.getServiceInstance(JobPlanService.class);

    public void index(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String forward = req.getParameter("forward");
        String clusterID = req.getParameter("clusterID");
        String jepName = req.getParameter("jepName");
        String jobPlanNodeSubState = req.getParameter("jobPlanNodeSubState");

        List<NodeInfo> executorNodes = jobPlanService.getExecutorClusterInfoList();
        List<String> clusterIDs = new ArrayList<String>();
        if (executorNodes != null) {
            for (NodeInfo ni : executorNodes) {
                clusterIDs.add(ni.getClusterID());
            }
        }

        req.setAttribute("clusterIDs", clusterIDs);
        req.setAttribute("clusterID", clusterID);
        req.setAttribute("jepName", jepName);
        req.setAttribute("jobPlanNodeSubState", jobPlanNodeSubState);

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
        String jobPlanNodeName = (String) parameters.get("jobPlanNodeName");
        String jobPlanNodeState = (String) parameters.get("jobPlanNodeState");

        JobPlanNodeState[] jobPlanNodeStateList = null;

        String jobPlanNodeSubState = (String) parameters.get("jobPlanNodeSubState");

        if ("fail".equals(jobPlanNodeSubState)) {
            jobPlanNodeStateList = JobPlanNodeState.FAIL_JOB_PLAN_NODE_STATES;
        }
        if ("success".equals(jobPlanNodeSubState)) {
            jobPlanNodeStateList = new JobPlanNodeState[] { JobPlanNodeState.RESULT_SUCCESS };
        }

        if ("running".equals(jobPlanNodeSubState)) {
            jobPlanNodeStateList = JobPlanNodeState.RUNNING_JOB_PLAN_NODE_STATES;
        }
        if (StringUtils.isNotBlank(jobPlanNodeState)) {
            jobPlanNodeStateList = new JobPlanNodeState[] { JobPlanNodeState.valueOf(jobPlanNodeState) };
        }

        String currentExecutorKey = (String) parameters.get("currentExecutorKey");
        String jobRunDateBegin = (String) parameters.get("jobRunDateBegin");
        String jobRunDateEnd = (String) parameters.get("jobRunDateEnd");

        if (StringUtils.isNotBlank(jobRunDateBegin)) {
            jobRunDateBegin = jobRunDateBegin + "0000";
        }
        if (StringUtils.isNotBlank(jobRunDateEnd)) {
            jobRunDateEnd = jobRunDateEnd + "0000";
        }
        Integer finishedPercent = null;
        if (null != parameters.get("finishedPercent")) {
            finishedPercent = Integer.valueOf(parameters.get("finishedPercent").toString());
        }

        try {
            page =
                    jobStateService.queryJobStates(page, clusterID, null, jepName, jobPlanNodeName,
                        jobPlanNodeStateList, jobRunDateBegin, jobRunDateEnd, finishedPercent, currentExecutorKey);
            @SuppressWarnings("unchecked")
            List<XjobStateView> xsList = page.getResult();
            JSONObject dateObj = new JSONObject();
            JSONArray data = new JSONArray();
            if (null != xsList && xsList.size() > 0) {

                for (XjobStateView xs : xsList) {
                    JSONObject obj = new JSONObject();

                    obj.put("id", xs.getId());
                    obj.put("clusterID", xs.getClusterID());
                    obj.put("jobPlanType", xs.getJobPlanTypeEnum().getTypeName());
                    obj.put("executorClusterID", xs.getExecutorClusterID());
                    // obj.put("jobPlanVersion", xs.getJobPlanVersion());
                    obj.put("jepName", xs.getJepName());
                    obj.put("jobPlanNodeName", xs.getDisplayJobPlanNodeName());
                    obj.put("jobPlanNodeType", xs.getJobPlanNodeTypeEnum().getTypeName());
                    obj.put("jobPlanNodeState", xs.getJobPlanNodeStateName());
                    obj.put("executeTimes", xs.getDisplayExecuteTimes());
                    obj.put("finishedPercent", xs.getDisplayFinishedPercent());
                    obj.put("runInfo", xs.getRunInfo());
                    obj.put("runTime", xs.getRunTimeFormater());
                    obj.put("currentExecutorKey", xs.getCurrentExecutorKey());
                    data.add(obj);
                }
            }
            dateObj.put("Rows", data);
            dateObj.put("Total", page.getTotalCount());
            logger.debug(dateObj.toString());
            super.ajaxOutPutJson(res, dateObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
