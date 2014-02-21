package com.zhangyue.hella.engine.web.action.admin;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.core.LogCollector;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.engine.web.action.BaseAction;
import com.zhangyue.hella.engine.web.service.ServiceFactory;
import com.zhangyue.hella.engine.web.service.SysService;

/**
 * @Descriptions The class PlanSubscribeAction.java's implementation：系统异常订阅
 * @author scott
 * @date 2013-8-19 下午2:59:38
 * @version 1.0
 */
public class PlanSubscribeAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PlanSubscribeAction.class);
    private SysService sysService = ServiceFactory.getServiceInstance(SysService.class);

    public void list(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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
        try {
            page = sysService.queryJobPlanSubscribe(page, clusterID);
            @SuppressWarnings("unchecked")
            List<JobPlanSubscribe> jpsList = page.getResult();
            JSONObject dateObj = new JSONObject();
            JSONArray data = new JSONArray();
            if (null != jpsList && jpsList.size() > 0) {
                for (JobPlanSubscribe jps : jpsList) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", jps.getId());
                    obj.put("clusterID", jps.getClusterID());
                    obj.put("userEmail", jps.getUserEmail());
                    obj.put("userPhoneNumber", jps.getUserPhoneNumber());
                    obj.put("state", jps.isState() == true ? "可用" : "禁用");
                    data.add(obj);
                }
            }
            dateObj.put("Rows", data);
            dateObj.put("Total", page.getTotalCount());
            LOG.debug(dateObj.toString());
            super.ajaxOutPutJson(res, dateObj.toString());
        } catch (Exception e) {
            LOG.error("Fail to query job plan subscribe.",e);
        }

    }

    public void add(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String clusterID = null;
        String userEmail = null;
        String userPhoneNumber = null;
        if (StringUtils.isBlank(req.getParameter("clusterID"))) {
            super.ajaxOutJsonResult(res, false, "操作失败,请选择集群编号");
            return;
        }
        clusterID = req.getParameter("clusterID");

        if (StringUtils.isNotBlank(req.getParameter("userEmail"))) {
            userEmail = req.getParameter("userEmail");
        }
        if (StringUtils.isNotBlank(req.getParameter("userPhoneNumber"))) {
            userPhoneNumber = req.getParameter("userPhoneNumber");
        }

        if (StringUtils.isBlank(userEmail) && StringUtils.isBlank(userPhoneNumber)) {
            super.ajaxOutJsonResult(res, false, "操作失败,请填写邮箱或者手机号码");
            return;
        }
        boolean rs = true;
        try {
            sysService.addJobPlanSubscribe(clusterID, userEmail, userPhoneNumber);
        } catch (Exception e) {
            LOG.error("Fail to add job plan subscribe.",e);
            rs = false;
        }
        if (rs) {
            super.ajaxOutJsonResult(res, true, "操作成功");
        } else {
            super.ajaxOutJsonResult(res, false, "操作失败");
        }

    }

    public void del(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }

        String ids = (String) req.getParameter("ids");
        if (StringUtils.isBlank(ids)) {
            super.ajaxOutJsonResult(res, false, "操作失败,请选择要删除的记录");
            return;
        }
        String[] idsStr = ids.split(",");
        int[] idsInt = new int[idsStr.length];
        for (int i = 0; i < idsStr.length; i++) {
            if (StringUtils.isNotBlank(idsStr[i])) {
                idsInt[i] = Integer.valueOf(idsStr[i]);
            }
        }
        boolean rs = sysService.deleteJobPlanSubscribeByID(idsInt);
        if (rs) {
            super.ajaxOutJsonResult(res, true, "操作成功");

            LogCollector.getSchedCollector().addSystemLog(
                new SystemLog(String.valueOf(req.getSession(true).getAttribute(EngineConstant.USER_NAME)),
                    req.getRemoteAddr(), SystemLogType.userOperator.name(), "del Subscribe, ids=" + ids,
                    DateUtil.dateFormaterBySeconds(new Date())));

        } else {
            super.ajaxOutJsonResult(res, false, "操作失败");
        }

    }

    public void changeState(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (null == req.getSession(true).getAttribute(EngineConstant.USER_NAME)) {
            super.ajaxOutJsonResult(res, false, "请先登录，再进行操作，操作过程系统将会记录日志");
        }
        String ids = (String) req.getParameter("ids");
        String stateStr = (String) req.getParameter("state");
        if (StringUtils.isBlank(ids)) {
            super.ajaxOutJsonResult(res, false, "操作失败,请选择要修改的记录");
            return;
        }
        String[] idsStr = ids.split(",");
        int[] idsInt = new int[idsStr.length];
        for (int i = 0; i < idsStr.length; i++) {
            if (StringUtils.isNotBlank(idsStr[i])) {
                idsInt[i] = Integer.valueOf(idsStr[i]);
            }
        }
        try {
            sysService.changeJobPlanSubscribeState(idsInt, Boolean.valueOf(stateStr));
            super.ajaxOutJsonResult(res, true, "操作成功");

            LogCollector.getSchedCollector().addSystemLog(
                new SystemLog(String.valueOf(req.getSession(true).getAttribute(EngineConstant.USER_NAME)),
                    req.getRemoteAddr(), SystemLogType.userOperator.name(), "change  Subscribe State, ids=" + ids,
                    DateUtil.dateFormaterBySeconds(new Date())));
        } catch (Exception e) {
            LOG.error("Fail to change job plan subscribe.",e);
            super.ajaxOutJsonResult(res, false, "操作失败");
        }

    }

    public void getClusterIDList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<String> list;
        try {
            list = sysService.getClusterIDList();
            JSONObject data = new JSONObject();
            if (list != null && list.size() > 0) {
                JSONArray array = JSONArray.fromObject(list);
                data.put("data", array);
            }
            super.ajaxOutPutJson(res, data.toString());
        } catch (Exception e) {
            LOG.error("Fail to get cluster id list.",e);
        }
    }
}
