package com.zhangyue.hella.engine.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.engine.web.service.SystemAdministrator;

public class LoginFilter extends HttpServlet implements Filter {

    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(LoginFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        String user = req.getParameter("userName");
        String password = req.getParameter("password");
        HttpSession session = req.getSession();
        String url = req.getRequestURI();
        if (url.endsWith(".css") || url.endsWith(".gif")
            || url.endsWith(".jpg") || url.endsWith(".png")||url.endsWith("login.jsp")||url.equals("/")) {
            chain.doFilter(req, resp);
            return;
        }
        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            user = (String) session.getAttribute(EngineConstant.USER_NAME);
            password = (String) session.getAttribute(EngineConstant.PASSWORD);
        }
        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            resp.sendRedirect("/login.jsp?msg=0");
            return;
        }
        try {
            if (SystemAdministrator.doValidation(user, password)) {
                session.setAttribute(EngineConstant.USER_NAME, user);
                session.setAttribute(EngineConstant.PASSWORD, password);
                chain.doFilter(req, resp);
            }else{
                resp.sendRedirect("/login.jsp?msg=1");
            }
        } catch (Exception e) {
            LOG.error("Fail to validate user. userName:" + user, e);
        }
    }
}
