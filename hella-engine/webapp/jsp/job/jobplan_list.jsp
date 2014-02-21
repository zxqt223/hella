<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/common/js_css.jsp"%>
</head>
<body>
	<div style="width: 100%; float: left">
		<div class="tablistbox">
			<span class="anas-type">作业计划上传</span>
		</div>
		<c:if test="${!empty msg}"><font color="blue" size="2">&nbsp;&nbsp;&nbsp;&nbsp;提示：${msg}</font></c:if>
		<form id="uploadForm" name="uploadForm" method="post"
			enctype="multipart/form-data">
			<div>
				<dd>
					&nbsp;&nbsp;&nbsp;&nbsp;执行器： <select id="cID" name="cID">
						<option value="">请选择</option>
						<c:if test="${!empty allExecutorClusterIDs}">
							<c:forEach items="${allExecutorClusterIDs}" var="cluster">
								<option value="${cluster}"
									<c:if test="${cluster==clusterID}"> selected=selected </c:if>>
									<c:out value="${cluster}" />
								</option>
							</c:forEach>
						</c:if>
					</select>
				</dd>
			</div>
			<c:if test="${engineState=='Slave'}">
				<div align="left">
				作业计划文件： <input type="file" name="file_upload" class="button button2 buttonnoicon" style="width: 40px" disabled="disabled" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="button" onclick="upload()" value="上传" class="button button2 buttonnoicon" style="width: 60px; margin-left: 200px" disabled="disabled"/>
			</div>
			</c:if>
			<c:if test="${engineState=='Master'}">
				<div align="left">
				作业计划文件： <input type="file" name="file_upload" class="button button2 buttonnoicon" style="width: 40px" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="button" onclick="upload()" value="上传" class="button button2 buttonnoicon" style="width: 60px; margin-left: 200px" />
			</div>
			</c:if>
		</form>
	</div>
	<div id="searchbar">
			<div style="width: 100%; padding-top: 30px; float: left">
				<div class="tablistbox">
					<span class="anas-type">作业计划查询</span>
				</div>
				<dd>
					&nbsp;&nbsp;&nbsp;&nbsp;执行器： <select id="clusterID" name="clusterID">
						<option value="">请选择</option>
						<c:if test="${!empty allocateJobPlanExecutorClusterIDs}">
							<c:forEach items="${allocateJobPlanExecutorClusterIDs}" var="cluster">
								<option value="${cluster}"
									<c:if test="${cluster==clusterID}"> selected=selected </c:if>>
									<c:out value="${cluster}" />
								</option>
							</c:forEach>
						</c:if>
					</select>
				</dd>
				<dd>
					作业计划状态： <select id="state" name="state">
						<option value="">请选择</option>
						<c:if test="${!empty stateList}">
							<c:forEach items="${stateList}" var="state">
								<option value="${state}"
									<c:if test="${state==currentState}"> selected=selected </c:if>>
									<c:out value="${state.stateName}" />
								</option>
							</c:forEach>
						</c:if>
					</select>
				</dd>
				<dd>
					作业计划名称：<input id="jepName" type="text" name="jepName"></input>
				</dd>
				<input type="button" value="搜索" onclick="return f_search_action()"
					class="button button2 buttonnoicon" style="width: 60px;">
			</div>

		</form>
	</div>
	<div id="maingrid" style="margin-top: 0px; float: left"></div>
</body>
</html>
<script type="text/javascript">
	function upload() {
		var cID = $("#cID").find("option:selected").val();
		if(cID==""){
			alert("请选择执行器!");
		}else{
			var url = "${ctx}/jobplan?method=fileUpLoad&cID=" + cID+ "";
			document.uploadForm.action = url;
			document.uploadForm.submit();	
		}
	}

	var manager;
	var selectedRow;

	function pushData(xjobs) {
		var data = {
			Rows : []
		};
		for ( var i = 0; i < xjobs.length; i++) {
			data.Rows.push(xjobs[i]);
		}
		return data;
	}

	$(function() {
		manager = $("#maingrid")
				.ligerGrid(
						{
							columns : [
									{
										display : '执行器',
										name : 'clusterID',
										align : 'center',
										width : '150',
										minWidth : 10
									},
									{
										display : '类型',
										name : 'jobPlanType',
										align : 'center',
										width : '60',
										minWidth : 10
									},
									{
										display : '状态',
										name : 'stateName',
										align : 'center',
										width : '50',
										minWidth : 5,
									},
									{
										display : 'JEP_ID',
										name : 'jepID',
										align : 'center',
										width : '50',
										minWidth : 15
									},
									{
										display : '名称',
										name : 'jepName',
										align : 'center',
										width : '250',
										minWidth : 15
									},

									{
										display : '周期',
										name : 'period',
										align : 'center',
										width : '150',
										minWidth : 10
									},
									{
										display : '产生事件',
										name : 'event',
										align : 'center',
										width : '100',
										minWidth : 10
									},
									{
										display : '描述',
										name : 'description',
										align : 'center',
										width : '200',
										minWidth : 20
									},
									{
										display : '最近一次运行信息',
										name : 'currentXjobState',
										align : 'center',
										width : '150',
										minWidth : 50
									},
									{
										display : '操作',
										align : 'center',
										width : '400',
										minWidth : 10,
										render : function(item) {
											//可用 
											var op = ""
											var gojobstate = "jobstate('"
													+ item.clusterID + "' , '"
													+ item.jepName + "')";
											if ($("#isAdmin")) {
												if('${engineState}'=='Master'){
													op = op+ '<input type="button" value="日志" onclick="'+gojobstate+'" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													var args = item.id + ",'"+ item.jepName + "'";

													if (item.state == 'able') {
														op = op
																+ '<input type="button" value="暂停" onclick="pauseJob('
																+ args
																+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													}
													//停用
													if (item.state == 'disable') {
														op = op
																+ '<input type="button" value="启动" onclick="resumeJob('
																+ args
																+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													}
													op = op
															+ '<input type="button" value="执行" onclick="executorQJob('
															+ args
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
		
													op = op
															+ '<input type="button" value="删除" onclick="deleteJobPlan('
															+ args
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													op = op
															+ '<input type="button" value="编辑"  onclick="enterEdit('
															+ item.id
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													op = op
															+ '<input type="button" value="详情"  onclick="jobDetail('
															+ item.id
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													return op;
												}else{
													op = op+ '<input type="button" value="日志"  disabled="disabled" onclick="'+gojobstate+'" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													var args = item.id + ",'"
													+ item.jepName + "'";

													if (item.state == 'able') {
														op = op
																+ '<input type="button" value="暂停"  disabled="disabled" onclick="pauseJob('
																+ args
																+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													}
													//停用
													if (item.state == 'disable') {
														op = op
																+ '<input type="button"  disabled="disabled" value="启动" onclick="resumeJob('
																+ args
																+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													}
													op = op
															+ '<input type="button" disabled="disabled" value="执行" onclick="executorQJob('
															+ args
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
		
													op = op
															+ '<input type="button"  disabled="disabled" value="删除" onclick="deleteJobPlan('
															+ args
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													op = op
															+ '<input type="button" disabled="disabled" value="编辑"  onclick="enterEdit('
															+ item.id
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													op = op
															+ '<input type="button"  disabled="disabled" value="详情"  onclick="jobDetail('
															+ item.id
															+ ')" class="button button2 buttonnoicon" style="width: 50px; float: left; margin-left: 10px"/>';
													return op;
												}
											}

										}
									}

							],

							width : '99%',
							height : '99%',
							heightDiff : -2,
							showTitle : true,
							colDraggable : true,
							isScroll : true,
							frozen : true,
							rownumbers : true,
							pageSize : 20,
							pageSizeOptions : [ 20, 30, 40, 50, 100 ],
							url : "${ctx }/jobplan?method=list",
							toolbar : {
								items : [ {
									text : '刷新',
									click : f_reload,
									icon : 'refresh'
								}, {
									line : true
								} ]
							}
						});

		setTimeout(function() {
			f_search_action();
		}, 1000);

	});

	function toJsonObject(jsonString) {
		if (typeof jsonString == 'object')
			return jsonString;
		jsonString = jsonString.replace(
				/^(?:\<pre[^\>]*\>)?(\{.*\})(?:\<\/pre\>)?$/ig, "$1");
		return eval('(' + jsonString + ')');
	}

	function jobDetail(jobExecutionPlanID) {
		var url = '${ctx }/jobplan?method=loadJEP&&jobExecutionPlanID='
				+ jobExecutionPlanID;
		window.open(url);
	}
	function enterEdit(jobExecutionPlanID) {
		var url = '${ctx }/jobplan?method=enterEdit&&jobExecutionPlanID='
				+ jobExecutionPlanID;
		window.open(url);
	}

	function jobstate(clusterID, jepName) {
		var url = '${ctx }/jobstate?method=index&forward=job/jobstate_list.jsp&&clusterID='
				+ clusterID + '&&jepName=' + jepName;
		window.location.href = url;
	}

	function pauseJob(jobExecutionPlanID, jepName) {
		$.ligerDialog.confirm("您确认暂停JOB计划 [ " + jepName + " ］ 吗?",
				function(yes) {
					if (yes) {
						$.ajax({
							type : 'post',
							cache : false,
							dataType : 'json',
							async : false,
							url : '${ctx }/jobplan?method=pauseJob',
							data : "jobExecutionPlanID=" + jobExecutionPlanID,
							success : function(data) {
								var jsonData = toJsonObject(data);
								if (jsonData.success) {
									LG.showSuccess(jsonData.msg);
									f_reload();
								} else {
									LG.showError(jsonData.msg);
								}

							},
							error : function(xhr, status, errMsg) {
								LG.showAjaxError(xhr, status, errMsg);
							},
							complete : function() {
								LG.hideLoading();
							}
						});
					}
				});

	}

	function resumeJob(jobExecutionPlanID, jepName) {
		$.ligerDialog.confirm("您确认启动该JOB计划 [ " + jepName + " ] 吗?", function(
				yes) {
			if (yes) {
				$.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					url : '${ctx }/jobplan?method=resumeJob',
					data : "jobExecutionPlanID=" + jobExecutionPlanID,
					success : function(data) {
						var jsonData = toJsonObject(data);
						if (jsonData.success) {
							LG.showSuccess(jsonData.msg);
							f_reload();
						} else {
							LG.showError(jsonData.msg);
						}
					},
					error : function(xhr, status, errMsg) {
						LG.showAjaxError(xhr, status, errMsg);
					},
					complete : function() {
						LG.hideLoading();
					}
				});
			}
		});

	}

	function executorQJob(jobExecutionPlanID, jepName) {
		$.ligerDialog.confirm("您确认手动执行该JOB计划 [ " + jepName
				+ " ] 吗?该操作会自动执行全部作业", function(yes) {
			if (yes) {
				$.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					url : '${ctx }/jobplan?method=executorQJob',
					data : "jobExecutionPlanID=" + jobExecutionPlanID,
					success : function(data) {
						var jsonData = toJsonObject(data);
						if (jsonData.success) {
							LG.showSuccess(jsonData.msg);
							f_reload();
						} else {
							LG.showError(jsonData.msg);
						}
					},
					error : function(xhr, status, errMsg) {
						LG.showAjaxError(xhr, status, errMsg);
					},
					complete : function() {
						LG.hideLoading();
					}
				});
			}
		});

	}

	function deleteJobPlan(jobPlanID, jepName) {
		$.ligerDialog.confirm("您确认删除该JOB计划 [ " + jepName + " ] 吗?", function(
				yes) {
			if (yes) {
				$.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					url : '${ctx }/jobplan?method=deleteJobPlan',
					data : "jobPlanID=" + jobPlanID,
					success : function(data) {
						var jsonData = toJsonObject(data);
						if (jsonData.success) {
							LG.showSuccess(jsonData.msg);
							f_reload();
						} else {
							LG.showError(jsonData.msg);
						}
					},
					error : function(xhr, status, errMsg) {
						LG.showAjaxError(xhr, status, errMsg);
					},
					complete : function() {
						LG.hideLoading();
					}
				});
			}
		});

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