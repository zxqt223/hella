<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link href="${pageContext.request.contextPath}/css/cycas.css" rel="stylesheet" type="text/css" /> 
<LINK href="/css/table.css" type="text/css" rel="stylesheet">
<script src="${pageContext.request.contextPath}/js/jquery-1.7.2.min.js" type="text/javascript"></script>
</head>
<script type="text/javascript">
function executorXJob(jobPlanNodeID) {
		var args = $("#"+jobPlanNodeID+"_args").val();
		$.ajax({
			type : 'post',
			cache : false,
			dataType : 'json',
			async : false,
			url : '${ctx }/jobplan?method=executorXJob',
			data : "jobPlanNodeID=" + jobPlanNodeID+"&args="+args,
			success : function(data) {
				var jsonData = toJsonObject(data);
				if (jsonData.success) {
					alert("操作成功，服务器正在发送调度通知");
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
		<table id="mytable" cellspacing="0" style="width: 90%">
			<tr>
				<th colspan="12">执行计划信息</th>
			</tr>
			<tr>
				<td colspan="4"><b>集群名称:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.clusterID}">${jobExecutionPlan.clusterID}</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="4"><b>计划类型:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.jobPlanType}">${jobExecutionPlan.jobPlanType}</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="4"><b>计划名称:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.jepName}">${jobExecutionPlan.jepName}</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="4"><b>周&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;期:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.cronExpression}">${jobExecutionPlan.cronExpression}</c:if>
				</td>
			</tr>

			<tr>
				<td colspan="4"><b>是否容错:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.ignoreError}">${jobExecutionPlan.ignoreError}</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="4"><b>创建日期:</b></td>
				<td colspan="8"><c:if
						test="${! empty jobExecutionPlan.createDate}">${jobExecutionPlan.createDate}</c:if>
				</td>
			</tr>
			<tr>
				<th colspan="12">作业流步骤</th>
			</tr>
			<tr>
				<td width="50px"></td>
				<td><b>执行器</b></td>
				<td><b>作业名称</b></td>
				<td><b>fork节点</b></td>
				<td><b>join节点</b></td>
				<td><b>next节点</b></td>
				<td><b>成功转向</b></td>
				<td><b>失败转向</b></td>
				<td><b>延迟处理</b></td>
				<td><b>延迟时间</b></td>
				<td><b>最大容错次数</b></td>
				<td width="80px"><b>操作</b></td>
			</tr>
			<c:forEach items="${jobExecutionPlan.jobPlanNodeList}"
				var="jobPlanNode" step="1" begin="0" varStatus="index">
				<tr>
					<td width="10px"><font>第${index.count}步</font></td>
					<td><c:if test="${! empty jobPlanNode.executorClusterID}">${jobPlanNode.executorClusterID}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.name}">${jobPlanNode.name}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.forkName}">${jobPlanNode.forkName}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.joinName}">${jobPlanNode.joinName}</c:if></td>

					<td><c:if test="${! empty jobPlanNode.toNode}">${jobPlanNode.toNode}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.okNode}">${jobPlanNode.okNode}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.errorNode}">${jobPlanNode.errorNode}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.delayType}">${jobPlanNode.delayType}</c:if></td>
					<td><c:if test="${! empty jobPlanNode.delayTime}">${jobPlanNode.delayTime}</c:if></td>

					<td><c:if test="${! empty jobPlanNode.errorMaxRedoTimes}">${jobPlanNode.errorMaxRedoTimes}</c:if></td>
					<c:choose>
						<c:when test="${!empty jobPlanNode.xjobMeta}">
							<td>&nbsp;<c:if test="${!empty jobPlanNode.xjobMeta}"><input type="button" style="width:70px;" class="button button2 buttonnoicon" onclick="executorXJob(${jobPlanNode.id})" value="执行"></c:if></td>
						</c:when>
						<c:otherwise>
						     <td>&nbsp;</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<c:if test="${!empty jobPlanNode.xjobMeta}">
					<tr>
						<td style="border-bottom: 0px;"></td>
						<td class='alt'>类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</td>
						<td class='alt' colspan="9"><c:if
								test="${! empty jobPlanNode.xjobMeta.mode}">${jobPlanNode.xjobMeta.mode}</c:if>
							<c:if test="${! empty jobPlanNode.xjobMeta.executeUser}">执行用户:${jobPlanNode.xjobMeta.executeUser}</c:if>
						</td>
						<td style="border-bottom: 0px;"></td>
					</tr>
					<tr>
						<td style="border-bottom: 0px;"></td>
						<td class='alt'>参&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数：</td>
						<td class='alt' colspan="9">
						 <c:if test="${! empty jobPlanNode.xjobMeta.executionContent}">${jobPlanNode.xjobMeta.executionContent}</c:if>
							&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="${jobPlanNode.id}_args" value="<c:if test="${! empty jobPlanNode.xjobMeta.args}">${jobPlanNode.xjobMeta.args}</c:if>">
						</td>
						<td style="border-bottom: 0px;"></td>
					</tr>
					<tr>
						<td style="border-bottom: 0px;"></td>
						<td class='alt'>执&nbsp;&nbsp;行&nbsp;&nbsp;类：</td>
						<td class='alt' colspan="9"><c:if
								test="${! empty jobPlanNode.xjobMeta.jobClassName}">${jobPlanNode.xjobMeta.jobClassName}</c:if>
						</td>
						<td style="border-bottom: 0px;"></td>
					</tr>
					<tr>
						<td></td>
						<td class='alt'>作业描述：</td>
						<td class='alt' colspan="9"><c:if
								test="${! empty jobPlanNode.xjobMeta.description}">${jobPlanNode.xjobMeta.description}</c:if>
						</td>
						<td></td>
					</tr>
				</c:if>
			</c:forEach>
			<tr>
				<td colspan="12" class="button" align="center"><input type="button"
					value="关闭" onclick="custom_close();" /></td>
			</tr>
		</table>
	</div>
</body>
</html>
