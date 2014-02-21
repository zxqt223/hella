<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="/css/cycas.css" type="text/css" rel="stylesheet">
<LINK href="/css/table.css" type="text/css" rel="stylesheet">
<script src="${pageContext.request.contextPath}/js/jquery-1.7.2.min.js" type="text/javascript"></script>
</head>
<script type="text/javascript">
function edit() {
    var jobExecutionPlanID = $("#jobExecutionPlanID").val();
  	var cronType = $("#cronTypeList").val();
	var cronExpression = $("#cronExpression").val();
	if (null== jobExecutionPlanID) {
 		alert("jobExecutionPlanID is null");
 		return;
 	}
 	if (null== cronType) {
 		alert("cronType is null");
 		return;
 	}
	if ( null== cronExpression) {
		alert("cronExpression is null");
 		return;
	}
 	var data = "jobExecutionPlanID=" + jobExecutionPlanID+ "&cronType=" + cronType + "&cronExpression=" + cronExpression;
	$.ajax({
		type : 'post',
		cache : false,
		dataType : 'json',
		async : false,
		url : '${ctx }/jobplan?method=edit',
		data :data,
		success : function(data) {
			var jsonData = toJsonObject(data);
			if (jsonData.success) {
				alert("操作成功");
				} else {
					alert(jsonData.msg);
			}
		},
		error : function(xhr, status, errMsg) {
			alert(errMsg);
		},
		complete : function() {
		}
	});
	}
	
function custom_close(){
if (confirm("您确定要关闭本页吗？")){
window.opener=null;
window.open('','_self');
window.close();
}
}
function toJsonObject(jsonString) {
	if (typeof jsonString == 'object')
		return jsonString;
	jsonString = jsonString.replace(
			/^(?:\<pre[^\>]*\>)?(\{.*\})(?:\<\/pre\>)?$/ig, "$1");
	return eval('(' + jsonString + ')');
}
</script>
<body>
	<div align="center">
	<table id="t1" cellspacing="0" height="100px"><tr>&nbsp;</tr></table>
	<table id="mytable" cellspacing="1" style="width:60%">
		<tr>
			<th colspan="12">执行计划信息</th>
			<input type="hidden" id="jobExecutionPlanID" name="jobExecutionPlanID" value="${jobExecutionPlan.id}"/>
		</tr>
		<tr>
			<td colspan="4"> 集群名称: </td>
			<td colspan="8">
			<c:if test="${! empty jobExecutionPlan.clusterID}">${jobExecutionPlan.clusterID}</c:if>
			 </td>
		</tr>
		<tr>
			<td colspan="4">计划类型: </td>
			<td colspan="8">
 			 <c:if test="${! empty jobExecutionPlan.jobPlanType}">${jobExecutionPlan.jobPlanType}</c:if>
			 </td>
		</tr>
		<tr>
			<td colspan="4"> 计划名称： </td>
			<td colspan="8">
			 <c:if test="${! empty jobExecutionPlan.jepName}">${jobExecutionPlan.jepName}</c:if>
			 </td>
		</tr>
		<tr>
			<td colspan="4">类型： </td>
			<td colspan="8">
			<c:if test="${! empty jobExecutionPlan.cronType}">
			<select id="cronTypeList" class="select">
			 	<option value="simple" <c:if test="${jobExecutionPlan.cronType=='simple'}"> selected="selected" </c:if> >简单型</option> 
			 	<option value="cron" <c:if test="${jobExecutionPlan.cronType=='cron'}"> selected="selected" </c:if>>复杂型</option>
			 	<option value="event" <c:if test="${jobExecutionPlan.cronType=='event'}"> selected="selected" </c:if>>事件型</option>
			</select>
			</c:if>
			 </td>
		</tr>
		
		<tr>
			<td colspan="4">周期： </td>
			<td colspan="8">
			<c:if test="${! empty jobExecutionPlan.cronExpression}">
			 	<input type="text" id="cronExpression" name="cronExpression" value="${jobExecutionPlan.cronExpression}">
			</c:if>
			 </td>
		</tr>
		
		<tr>
			<td colspan="4"> 是否容错： </td>
			<td colspan="8">
			 <c:if test="${! empty jobExecutionPlan.ignoreError}">${jobExecutionPlan.ignoreError}</c:if>
			 </td>
		</tr>
		<tr>
			<td colspan="4"> 创建时间： </td>
			<td colspan="8">
				 <c:if test="${! empty jobExecutionPlan.createDate}">${jobExecutionPlan.createDate}</c:if>
			 </td>
		</tr>
		<tr>
			<td colspan="12" align="center"> 
				 <input class="button" type="button" value="关闭" onclick="custom_close();"/>
				 <input class="button" type="button" value="提交" onclick="return edit()"/>
			 </td>
		</tr>
	</table>
	</div>
</body>
</html>
