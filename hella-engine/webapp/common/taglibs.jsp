<%@ page language="java" errorPage="/common/error.jsp" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%request.setAttribute("randomData", new java.util.Random().nextInt());%>
<c:set var="random" value="${randomData}"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%response.setHeader("Pragma","No-cache");
  response.setHeader("Cache-Control","no-cache");
  response.setDateHeader("Expires",0);%> 
  