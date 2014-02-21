<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="/css/cycas.css" type="text/css" rel="stylesheet">
<LINK href="/css/table.css" type="text/css" rel="stylesheet">
 
</head>
<body>
		<table id="mytable" cellspacing="0">
		<tr>
			<th colspan="8">调度系统配置</th>
		</tr>
		<c:if test="${!empty sysconfMap}">
		<c:forEach items="${sysconfMap}" var="entry">
			<tr>
				<td colspan="4"><c:out value="${entry.key}" /></td>
				<td colspan="4"><c:out value="${entry.value}" /></td>
			</tr>
		</c:forEach>
		</c:if>
	</table>
	
</body>

</html>
