<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>欢迎您!</title>
<%@ include file="/common/js_css.jsp"%>
</head>
<body>
	<!--page-content-->
	<div class="page-content clearfix">
		<!--con-right-->

		<div class="c-right fl" style="padding-left: 50px">
			<div class="tablistbox">
				<span class="anas-type">调度引擎信息</span>
			</div>
			<c:if test="${sysInfo.state=='Master'}">
				<div style="padding-left: 10px">
					<b>引擎状态:</b><font color="green">${sysInfo.state}</font>
				</div>
			</c:if>
			<c:if test="${sysInfo.state=='Slave'}">
				<div style="padding-left: 10px">
					<b>引擎状态:</b><font color="yellow">${sysInfo.state}</font>
				</div>
			</c:if>
			<div>&nbsp;</div>
			<div style="padding-left: 10px">
				<b>启动时间:</b>${sysInfo.startDate}
			</div>
			<div>&nbsp;</div>
			<div style="padding-left: 10px">
				<b>当前主机:</b>${sysInfo.clusterInfo.currentAppAddress}
			</div>
			<div>&nbsp;</div>
			<div style="padding-left: 10px">
				<b>ZK&nbsp;地&nbsp;址:</b>${sysInfo.zkAddress}
			</div>
			<div>&nbsp;</div>
			<div style="padding-left: 10px">
				<b>内存使用:</b>${sysInfo.memoryUsed}
			</div>
			<div>&nbsp;</div>
			<hr></hr>
			<div>&nbsp;</div>
			<div>
				<table cellpadding="0" width="100%" cellspacing="1"
					class="tblresult">
					<thead class="rest-head">
						<tr bgcolor='#F5F5DC'>
							<th style="font-weight: bold;">节点名称</th>
							<th style="font-weight: bold;">类型</th>
							<th style="font-weight: bold;">状态</th>
							<th style="font-weight: bold;">地址</th>
						</tr>
					</thead>
					<tbody class="rest-bd">
						<c:if test="${!empty sysInfo.clusterInfo.engineList}">
							<c:forEach items="${sysInfo.clusterInfo.engineList}"
								var="nodeInfo">
								<tr
									<c:if test="${nodeInfo.state=='Master'}"> bgcolor='#F5F5F5' </c:if>>
									<td align="left">${nodeInfo.name}</td>
									<td align="left">${nodeInfo.type}</td>
									<td align="left">${nodeInfo.state}</td>
									<td align="left">${nodeInfo.address}</td>
								</tr>
							</c:forEach>
						</c:if>
					</tbody>
				</table>
			</div>

			<div class="tablistbox tm_adjust">
				<span class="anas-type">执行器信息</span>
			</div>
			<div>
				<table cellpadding="0" width="100%" cellspacing="1"
					class="tblresult">
					<thead class="rest-head">
						<tr bgcolor='#F5F5DC'>
							<th style="font-weight: bold;">节点名称</th>
							<th style="font-weight: bold;">类型</th>
							<th style="font-weight: bold;">状态</th>
							<th style="font-weight: bold;">地址</th>
							<th style="font-weight: bold;">启动时间</th>
						</tr>
					</thead>
					<tbody class="rest-bd">
						<c:forEach items="${sysInfo.clusterInfo.executorMap}"
							var="executorMap">
							<tr>
								<td align="left" rowspan="${fn:length(executorMap.value)+1}">
									<a
									href="${ctx }/jobplan?method=index&forward=job/jobplan_list.jsp&clusterID=${executorMap.key}">
										${executorMap.key} </a>
								</td>
							</tr>
							<c:forEach items="${executorMap.value}" var="nodeInfo">
								<tr
									<c:if test="${nodeInfo.state=='Master'}"> bgcolor='#F5F5F5' </c:if>>
									<td align="left">${nodeInfo.name}</td>
									<td align="left">${nodeInfo.state}</td>
									<td align="left">${nodeInfo.address}</td>
									<td align="left">${nodeInfo.createTime}</td>
								</tr>
							</c:forEach>

						</c:forEach>
					</tbody>
				</table>
			</div>

			<div class="tablistbox tm_adjust">
				<span class="anas-type">作业统计信息</span>
			</div>
			<div>
				<table cellpadding="0" width="100%" cellspacing="1"
					class="tblresult">
					<thead class="rest-head">
						<tr bgcolor='#F5F5DC'>
							<th style="font-weight: bold;">名称</th>
							<th style="font-weight: bold;">正常作业计划总数</th>
							<th style="font-weight: bold;">暂停作业计划总数</th>
							<th style="font-weight: bold;">最近7天运行成功作业次数</th>
							<th style="font-weight: bold;">最近7天运行失败作业次数</th>
							<th style="font-weight: bold;">正在运行作业总数</th>
						</tr>
					</thead>
					<tbody class="rest-bd">
						<c:if test="${!empty jobCountList}">
							<c:forEach items="${jobCountList}" var="jobCount">
								<tr>
									<td align="left">${jobCount.clusterID}</td>
									<td align="left"><c:if
											test="${jobCount.ableJobPlanNum!=0}">
											<a
												href="${ctx }/jobplan?method=index&forward=job/jobplan_list.jsp&clusterID=${jobCount.clusterID}&currentState=able">
												<font color="green" size="3">${jobCount.ableJobPlanNum}</font>
											</a>
										</c:if></td>
									<td align="left"><c:if
											test="${jobCount.disableJobPlanNum!=0}">
											<a
												href="${ctx }/jobplan?method=index&forward=job/jobplan_list.jsp&clusterID=${jobCount.clusterID}&currentState=disable">
												<font color="red" size="3">${jobCount.disableJobPlanNum}</font>
											</a>
										</c:if></td>
									<td align="left">${jobCount.successXjobsNum}</td>
									<td align="left"><c:if
											test="${jobCount.failXjobsNum!=0}">
											<a
												href="${ctx }/jobstate?method=index&forward=job/jobstate_list.jsp&clusterID=${jobCount.clusterID}&jobPlanNodeSubState=fail">
												<font color="red" size="3">${jobCount.failXjobsNum}</font>
											</a>
										</c:if></td>
									<td align="left"><c:if
											test="${jobCount.runningXjobsNum!=0}">
											<a
												href="${ctx }/jobstate?method=index&forward=job/jobstate_list.jsp&clusterID=${jobCount.clusterID}&jobPlanNodeSubState=running">
												<font color="green" size="3">${jobCount.runningXjobsNum}</font>
											</a>
										</c:if></td>
								</tr>
							</c:forEach>
						</c:if>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>
