package com.zhangyue.hella.engine.web.action.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.engine.web.action.BaseAction;
import com.zhangyue.hella.engine.web.service.ServiceFactory;
import com.zhangyue.hella.engine.web.service.SysService;

/**
 * @Descriptions The class SysAction.java's implementation：系统信息ACTION 包括：集群/配置/日志
 * @author scott
 * @date 2013-8-19 下午3:01:05
 * @version 1.0
 */
public class SysAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    protected final transient Logger logger = Logger.getLogger(SysAction.class);

    private SysService sysService = ServiceFactory.getServiceInstance(SysService.class);

    public void info(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        Configuration conf=new Configuration();
        conf.initialize(new String[]{EngineConstant.QUARTZ_PROPERTIES,EngineConstant.ENGINE_PROPERTIES});
        req.setAttribute("sysconfMap", conf.getAllParameters());
        req.getRequestDispatcher("/jsp/admin/sys_info.jsp").forward(req, res);
    }

    public void log(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        req.setAttribute("logType", SystemLogType.values());
        req.getRequestDispatcher("/jsp/admin/log_list.jsp").forward(req, res);

    }

    public void logList(HttpServletRequest req, HttpServletResponse res) {
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

        String ip = (String) parameters.get("ip");
        String operatorName = (String) parameters.get("operatorName");
        String systemLogTypeStr = (String) parameters.get("systemLogType");
        String dateBegin = (String) parameters.get("dateBegin");
        String dateEnd = (String) parameters.get("dateEnd");

        if (StringUtils.isNotBlank(dateBegin)) {
            dateBegin = dateBegin + "0000";
        }
        if (StringUtils.isNotBlank(dateEnd)) {
            dateEnd = dateEnd + "0000";
        }

        SystemLogType systemLogType = null;
        if (StringUtils.isNotBlank(systemLogTypeStr)) {
            systemLogType = SystemLogType.valueOf(systemLogTypeStr);
        }

        try {
            page =
                    sysService.querySystemLog(page, ip, operatorName,
                        systemLogType, dateBegin, dateEnd);
            @SuppressWarnings("unchecked")
            List<SystemLog> logList = page.getResult();
            JSONObject dateObj = new JSONObject();
            JSONArray data = new JSONArray();
            if (null != logList && logList.size() > 0) {
                for (SystemLog log : logList) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", log.getId());
                    obj.put("operatorName", log.getOperatorName());
                    obj.put("ip", log.getIp());
                    obj.put("logType", log.getLogType());
                    obj.put("logTypeMsg", log.getLogTypeMsg());
                    obj.put("logContent", log.getLogContent());
                    obj.put("createDate", log.getCreateDateFormater());
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
