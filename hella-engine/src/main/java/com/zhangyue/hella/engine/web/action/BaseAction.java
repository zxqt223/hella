package com.zhangyue.hella.engine.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.engine.util.EngineConstant;

public class BaseAction extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String method = "method";

    protected String message;

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        String forward = req.getParameter("forward");
        if (StringUtils.isBlank(req.getParameter(method))
            && StringUtils.isNotBlank(forward)) {
            if (forward.equals("exit")) {// 管理员退出
                HttpSession session = req.getSession();
                session.removeAttribute(EngineConstant.USER_NAME);
                session.removeAttribute(EngineConstant.PASSWORD);
                session.invalidate();
                session = null;
                req.getRequestDispatcher("/login.jsp").forward(req, res);
                return;
            }
            req.getRequestDispatcher("/jsp/" + forward).forward(req, res);
            return ;
        }

        if (StringUtils.isNotBlank(req.getParameter(method))) {
            Method service = null;
            try {
                service =
                        this.getClass().getMethod(req.getParameter(method),
                            javax.servlet.http.HttpServletRequest.class,
                            javax.servlet.http.HttpServletResponse.class);
                service.invoke(this, req, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.sendError(500, e.getMessage());
                return;
            }

        }
    }

    public Map<String, Object> getQueryParameters(HttpServletRequest req) {
        String where = req.getParameter("where");
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(where)) {
            JSONObject dateObj = JSONObject.fromObject(where);
            JSONArray jsonArray = JSONArray.fromObject(dateObj.get("rules"));
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject o = jsonArray.getJSONObject(j);
                String field = (String) o.get("field");
                Object value = o.get("value");
                parameters.put(field, value);

            }
        }
        return parameters;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        doPost(req, res);
    }

    private void ajaxOutPut(HttpServletResponse res, String outputStyle,
        Object outputString) throws IOException {
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-cache");

        if ("json".equals(outputStyle)) res.setContentType("text/json;charset=UTF-8");
        else if ("xml".equals(outputStyle)) res.setContentType("text/xml");
        else {
            res.setContentType("text/plain");
        }
        PrintWriter out = res.getWriter();
        out.write((outputString == null) ? "" : outputString.toString());
        out.flush();
        out.close();
    }

    protected void ajaxOutPutText(HttpServletResponse res, Object outputString)
        throws IOException {
        ajaxOutPut(res, null, outputString);
    }

    protected void ajaxOutPutJson(HttpServletResponse res, Object outputString)
        throws IOException {
        ajaxOutPut(res, "json", outputString);
    }

    protected void ajaxOutPutXml(HttpServletResponse res, Object outputString)
        throws IOException {
        ajaxOutPut(res, "xml", outputString);
    }

    protected void ajaxOutJsonResult(HttpServletResponse res,
        boolean isSuccess, String msg) {
        JSONObject dateObj = new JSONObject();
        dateObj.put("success", isSuccess);
        dateObj.put("msg", msg);
        try {
            ajaxOutPut(res, "json", dateObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
