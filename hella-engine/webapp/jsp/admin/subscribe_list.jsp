<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/common/js_css.jsp"%>
<script src="${ctx}/js/datepicker/WdatePicker.js" type="text/javascript"></script>
</head>
<script>
	$(document).ready(function() {
		getClusterIDList();
	});
</script>
<body style="padding: 1px; overflow: hidden;">
	<div id="searchbar">
		<form id="addForm" class="l-form" name="caForm">
			<div class="infoContent" 
				style="text-align: left; float: none; padding: 10px;">
				<div class="col6" style="float: left; width: 90%; clear: left;">
					<dd>执行器名称：<select id="clusterIDList" name="clusterIDList"></select></dd>
					<dd>邮箱地址：<input id="userEmail" type="text" name="userEmail"></input></dd>
					<dd>手机号码：<input id="userPhoneNumber" type="text" name="userPhoneNumber"></input></dd>
					<dd><input type="button" class="button button2 buttonnoicon" style="width: 60px;" value="添加"
						onclick="return addForm_action()" /></dd>
				</div>

			</div>
		</form>
	</div>
	<div id="maingrid" style="margin: 0; padding: 0"></div>
</body>
</html>
<script type="text/javascript">
	var manager;
	function getClusterIDList() {
		$
				.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					url : '${ctx }/admin/subscribe?method=getClusterIDList',
					success : function(data) {
						var jsonData = toJsonObject(data);
						if (null != jsonData.data) {
							$("#clusterIDList").append(
									"<option value=''>请选择</option>");
							for ( var i = 0; i < jsonData.data.length; i++) {
								$("#clusterIDList").append(
										"<option value='"+jsonData.data[i]+"'>"
												+ jsonData.data[i]
												+ "</option>");
							}
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

	function toJsonObject(jsonString) {
		if (typeof jsonString == 'object')
			return jsonString;
		jsonString = jsonString.replace(
				/^(?:\<pre[^\>]*\>)?(\{.*\})(?:\<\/pre\>)?$/ig, "$1");
		return eval('(' + jsonString + ')');
	}

	$(function() {
		manager = $("#maingrid").ligerGrid({
			columns : [ {
				display : 'ID',
				name : 'id',
				align : 'center',
				width : 100,
				minWidth : 20
			}, {
				display : '执行器',
				name : 'clusterID',
				align : 'center',
				width : 100,
				minWidth : 20
			}, {
				display : '邮箱地址',
				name : 'userEmail',
				align : 'center',
				width : 300,
				minWidth : 50,
			}, {
				display : '手机号码',
				name : 'userPhoneNumber',
				align : 'center',
				width : 300,
				minWidth : 50,
			}, {
				display : '状态',
				name : 'state',
				align : 'center',
				width : 80,
				minWidth : 50,
			} ],
			url : "${ctx }/admin/subscribe?method=list",
			usePager : true,
			pageSize : 15,
			pageSizeOptions : [ 15, 20, 30, 40, 50, 100 ],
			autoCheckChildren : false,
			alternatingRow : false,
			checkbox : true,
			rownumbers : true,
			width : '99%',
			height : '99%',
			heightDiff : -2,
			showTitle : true,
			colDraggable : true,
			isScroll : true,
			frozen : true,
			toolbar : {
				items : [ {
					text : '删除',
					click : del,
					icon : 'delete'
				}, {
					line : true
				}, {
					text : '可用',
					click : ableState,
					icon : 'edit'
				}, {
					line : true
				}, {
					text : '禁用',
					click : disAbleState,
					icon : 'edit'
				}, {
					line : true
				},

				{
					text : '刷新',
					click : f_reload,
					icon : 'refresh'
				}, {
					line : true
				}

				]
			},
		});
	});

	function f_reload() {
		manager.loadData();
	}

	function addForm_action() {
		var clusterID = $("#clusterIDList").val();
		var userEmail = $("#userEmail").val();
		var userPhoneNumber = $("#userPhoneNumber").val();
		var data = "clusterID=" + clusterID;
		if (null != userEmail) {
			data = data + "&userEmail=" + userEmail;
		}
		if (null != userPhoneNumber) {
			data = data + "&userPhoneNumber=" + userPhoneNumber;
		}
		$.ajax({
			type : 'post',
			cache : false,
			dataType : 'json',
			async : false,
			data : data,
			url : '${ctx }/admin/subscribe?method=add',
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

	function del() {
		var ids = "";
		var data = manager.getCheckedRows();
		if (data.length <= 0) {
			LG.showError("请选择要删除的记录.");
			return;
		}
		for ( var i = 0; i < data.length; i++) {
			ids = ids + data[i].id + ",";
		}
		$.ligerDialog.confirm("您确认删除该数据吗?", function(yes) {
			if (yes) {
				$.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					data : "ids=" + ids,
					url : '${ctx }/admin/subscribe?method=del',
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

	function disAbleState() {
		changeState(false);
	}
	function ableState() {
		changeState(true);
	}
	function changeState(state) {
		var ids = "";
		var data = manager.getCheckedRows();
		if (data.length <= 0) {
			LG.showError("请选择要修改的记录.");
			return;
		}
		for ( var i = 0; i < data.length; i++) {
			ids = ids + data[i].id + ",";
		}
		$.ligerDialog.confirm("您确认要修改该数据吗?", function(yes) {
			if (yes) {
				$.ajax({
					type : 'post',
					cache : false,
					dataType : 'json',
					async : false,
					data : "state=" + state + "&&ids=" + ids,
					url : '${ctx }/admin/subscribe?method=changeState',
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
</script>