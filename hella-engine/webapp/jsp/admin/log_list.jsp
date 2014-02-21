<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/common/js_css.jsp"%>
<script src="${ctx}/js/datepicker/WdatePicker.js" type="text/javascript"></script>
</head>
<body style="padding: 0px; overflow: hidden;">
	<div id="searchbar" ">
		<form id="formsearch" class="l-form" name="caForm">
			<div style="width: 800px; float: left">
				<dd>
					类&nbsp;&nbsp;&nbsp;型：<select name="systemLogType" id="systemLogType" style="width:152px">
						<option value="">全部</option>
						<c:if test="${!empty logType}">
							<c:forEach items="${logType}" var="entry">
								<option value="${entry}">${entry.logType}</option>
							</c:forEach>
						</c:if>
					</select>
				</dd>
				<dd>
					开始时间：<input name="dateBegin" onfocus="timeFocusBegin(this)"
						type="text" class="Wdate" id="dateBegin" />
				</dd>
				<dd>
					结束时间：<input name="dateEnd" onfocus="timeFocusEnd(this)" type="text"
						class="Wdate" id="dateEnd" />
				</dd>
			</div>
			<div style="width: 800px; float: left">
				<dd>
					IP地址：<input id="ip" type="text" name="ip"></input>
				</dd>
				<dd>
					操&nbsp;作&nbsp;&nbsp;人：<input id="operatorName" type="text" name="operatorName"></input>
				</dd>
				<dd>
				  <input type="button" value="搜索" onclick="return f_search_action()"
					class="button button2 buttonnoicon" style="width: 60px;left:250px">
				</dd>
			</div>
		</form>
	</div>
	<div id="maingrid" style="margin-top:0px;float:left"></div>
</body>
</html>
<script type="text/javascript">
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
	$(function() {
		manager = $("#maingrid").ligerGrid({
			columns : [  {
				display : '操作者',
				name : 'operatorName',
				align : 'center',
				width : 80,
				minWidth : 50,
				
			}, {
				display : '类型',
				name : 'logTypeMsg',
				align : 'center',
				width : 80,
				minWidth : 20
			}, {
				display : 'IP地址',
				name : 'ip',
				align : 'center',
				width : 100,
				minWidth : 50,
			} , {
				display : '内容',
				name : 'logContent',
				align : 'left',
				width : 800,
				minWidth : 50,
			}, {
				display : '时间',
				name : 'createDate',
				align : 'center',
				width : 150,
				minWidth : 50,
			}  ],

			usePager : true,
			pageSize : 20,
			pageSizeOptions : [20, 30, 40, 50, 100 ],
			autoCheckChildren : false,
			alternatingRow : false,
			checkbox : false,
			rownumbers : true,
				url : "${ctx }/admin/sys?method=logList",
			width : '99%',
			height : '99%',
			heightDiff : -2,
			showTitle : true,
			colDraggable : true,
			isScroll : true,
			frozen : true,
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
	});

	function f_reload() {
		manager.loadData();
	}

	function f_search_action() {
		$("#form").trigger("checkInput");
		var form = $("#formsearch");
		LG.searchForm(form, manager);
	}
</script>