package com.zhangyue.hella.engine.web.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.entity.JobCount;
import com.zhangyue.hella.engine.web.service.JobPlanService;
import com.zhangyue.hella.engine.web.service.ServiceFactory;
import com.zhangyue.hella.engine.web.service.SysService;

/**
 * @Descriptions The class WelcomeAction.java's implementation：欢迎页面action
 * @author scott
 * @date 2013-8-19 下午2:59:03
 * @version 1.0
 */
public class WelcomeAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG =
            Logger.getLogger(WelcomeAction.class);

    public void info(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        req.setAttribute("sysInfo", ServiceFactory.getServiceInstance(SysService.class).getSysInfo());
        List<JobCount> jobCountList = null;
        try {
            jobCountList = ServiceFactory.getServiceInstance(JobPlanService.class).countJobInfo();
        } catch (Exception e) {
            LOG.error("Fail to count job info.",e);
        }
        req.setAttribute("jobCountList", jobCountList);
        req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, res);
    }

}
