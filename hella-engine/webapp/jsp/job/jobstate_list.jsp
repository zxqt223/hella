<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/common/js_css.jsp"%>
<script src="${ctx}/js/datepicker/WdatePicker.js" type="text/javascript"></script>
</head>
<body style="padding: 0px; overflow: hidden;">
	<div id="searchbar" >
		<form id="formsearch" class="l-form" name="caForm">
			<div style="width: 1200px; float: left">
				<dd>
					 执行器：
					 <select id="clusterID" name="clusterID">
				     <option value="">请选择</option>
				 	 <c:if test="${!empty clusterIDs}">
		                <c:forEach items="${clusterIDs}" var="cluster">
		                  <option value="${cluster}" <c:if test="${cluster==clusterID}"> selected=selected </c:if>>
		                    <c:out value="${cluster}"/>
		                  </option>
		                </c:forEach>
		              </c:if>
					 </select>	 
				</dd>
				<dd>
					计划名称：<input id="jepName" type="text" name="jepName" value="${jepName}"></input>
				</dd>
				<dd>
					作业名称：<input id="jobPlanNodeName" type="text" name="jobPlanNodeName"></input>
				</dd>
			</div>
			<div style="width: 1200px; float: left;padding-top: 10px">
				<dd>
					状&nbsp;&nbsp;&nbsp;态： 
					<select id="jobPlanNodeSubState" name="jobPlanNodeSubState">
					    <option value="">请选择</option>
 					    <option value="success" <c:if test="${jobPlanNodeSubState=='success'}">selected="selected"</c:if> >成功</option>
					    <option value="fail" <c:if test="${jobPlanNodeSubState=='fail'}">selected="selected"</c:if> >失败</option>
						<option value="running" <c:if test="${jobPlanNodeSubState=='running'}">selected="selected"</c:if>>运行中</option>
					</select>
				</dd>
				<dd>
					开始时间：<input name="jobRunDateBegin" onfocus="timeFocusBegin(this)"
						type="text" class="Wdate" id="d4311" />
				</dd>
				<dd>
					结束时间：<input name="jobRunDateEnd" onfocus="timeFocusEnd(this)"
						type="text" class="Wdate" id="d4312" />
				</dd>
				<dd>
				<input type="button" value="搜索" onclick="return f_search_action()"
					class="button button2 buttonnoicon" style="width: 60px;">
				</dd>
			</div>
		</form>
	</div>
	<div id="maingrid" style="margin-top:0px;float:left"></div>
</body>
</html>
<script type="text/javascript">
	function setFinishedPercent(obj) {
		 $("#finishedPercent").val(obj.value);
	}
	function timeFocusBegin(obj) {
		WdatePicker({
			isShowClear : false,
			dateFmt : 'yyyyMMddHH'
		});
	}
	function timeFocusEnd(obj) {
		WdatePicker({
			isShowClear : false,
			dateFmt : 'yyyyMMddHH'
		});
	}
	var manager;
	var selectedRow;
	
	
	
	$(function() {
		
		manager = $("#maingrid").ligerGrid({
			columns : [   
           {
				display : '执行器',
				name : 'executorClusterID',
				align : 'center',
				width : 80,
				minWidth : 50,
			}, 
			{
				display : '计划名称',
				name : 'jepName',
				align : 'center',
				width : 150,
				minWidth : 50,
			},
			 
			{
				display : '作业名称',
				name : 'jobPlanNodeName',
				align : 'center',
				width : 150,
				minWidth : 50,
			}, {
				display : '运行状态',
				name : 'jobPlanNodeState',
				align : 'center',
				width : 180,
				minWidth : 20,
			},
			 {
				display : '进度',
				name : 'finishedPercent',
				align : 'center',
				width : 20,
				minWidth : 50,
			},
			{
				display : '重复次数',
				name : 'executeTimes',
				align : 'center',
				width : 60,
				minWidth : 20,
			},
			{
				display : '运行时间',
				name : 'runTime',
				align : 'center',
				width : 150,
				minWidth : 110,
			},
			{
				display : '日志',
				name : 'runInfo',
				align : 'center',
				width : 260,
				minWidth : 110,
			},
			 ],

			usePager : true,
			pageSize : 20,
			pageSizeOptions : [20, 30, 40, 50, 100 ],
			autoCheckChildren : false,
			alternatingRow : false,
			checkbox : false,
			rownumbers : true,
			url : "${ctx }/jobstate?method=list",
			width : '99%',
			height : '99%',
			heightDiff : -2,
			showTitle : true,
			colDraggable : true,
			isScroll : true,
			frozen : true,
			onSelectRow : rowSelected,
			toolbar : {
				items : [ {
					text : '刷新',
					click : f_reload,
					icon : 'refresh'
				}, {
					line : true
				} ]
			},
		});
		
		
		setTimeout(function(){f_search_action();},1000);
		
	});

	function rowSelected(rowdata, rowindex, rowDomElement) {
		if (null != rowdata.runInfo&& rowdata.runInfo.trim().length > 0) {
				LG.showSuccess(rowdata.runInfo);
		}
	}

	function f_reload() {
		manager.loadData();
	}

	function f_search_action() {
		$("#form").trigger("checkInput");
		var form = $("#formsearch");
		LG.searchForm(form, manager);
	}
</script>
