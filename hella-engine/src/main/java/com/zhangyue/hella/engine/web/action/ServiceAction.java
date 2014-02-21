package com.zhangyue.hella.engine.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.entity.JobPlanState;
import com.zhangyue.hella.engine.web.action.job.JobPlanAction;
import com.zhangyue.hella.engine.web.service.JobPlanService;
import com.zhangyue.hella.engine.web.service.ServiceFactory;

public class ServiceAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    protected final transient Logger logger =
            Logger.getLogger(JobPlanAction.class);

    public void queryJobPlanState(HttpServletRequest req,
        HttpServletResponse res) {
        String clusterID = req.getParameter("clusterID");
        String jepIDs = req.getParameter("jepIDs");
        if (StringUtils.isBlank(clusterID)) {
            super.ajaxOutJsonResult(res, false, "clusterID is null");
            return;
        }
        if (StringUtils.isBlank(jepIDs)) {
            super.ajaxOutJsonResult(res, false, "jepIDs is null");
            return;
        }
        try {
            List<JobPlanState> jobPlanStates =
                    ServiceFactory.getServiceInstance(JobPlanService.class).queryJobPlanStates(clusterID,
                        jepIDs.split(","));
            JSONArray data = new JSONArray();
            if (null != jobPlanStates && jobPlanStates.size() > 0) {
                for (JobPlanState jobPlanState : jobPlanStates) {
                    JSONObject jpJSON = new JSONObject();
                    jpJSON.put("clusterID", jobPlanState.getClusterID());
                    jpJSON.put("jepID", jobPlanState.getJepID());
                    jpJSON.put("jepName", jobPlanState.getJepName());
                    jpJSON.put("date", jobPlanState.getCurrentXjobDate());
                    jpJSON.put("state", jobPlanState.getCurrentXjobState());
                    data.add(jpJSON);
                }
            }
            logger.debug(data.toString());
            super.ajaxOutJsonResult(res, true, data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
