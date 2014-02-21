<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Hella-任务调度系统</title>
<%@ include file="/common/js_css.jsp"%>
<script src="${ctx }/js/ligerUI/js/ligerui.min.js"
	type="text/javascript"></script>
<link href="${ctx }/css/index.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">
	var tab = null;
	var tabidcounter = 0;
	var accordion = null;
	var tree = null;
	var menuManager;
	$(function() {
		//布局
		$("#layout1").ligerLayout({
			leftWidth : 220,
			bottomHeight : 35,
			height : '100%',
			heightDiff : 0,
			onHeightChanged : f_heightChanged
		});
		var height = $(".l-layout-center").height();
		//Tab
		$("#framecenter").ligerTab({
			height : height
		});
		//面板
		$("#accordion1").ligerAccordion({
			height : height - 24,
			speed : null
		});
		$(".l-link").hover(function() {
			$(this).addClass("l-link-over");
		}, function() {
			$(this).removeClass("l-link-over");
		});
		//树
		tab = $("#framecenter").ligerGetTabManager();

		$("#pageloading").html(height);
		$("#pageloading").show();

		accordion = $("#accordion1").ligerGetAccordionManager();
		tree = $("#tree1").ligerGetTreeManager();
		$("#pageloading").hide();

		LG.prevDialogImage();

		//菜单初始化
		$("ul.menulist li").live('click', function() {
			var jitem = $(this);
			var tabid = jitem.attr("tabid");
			var url = jitem.attr("url");
			if (!url)
				return;
			if (!tabid) {
				tabidcounter++;
				tabid = "tabid" + tabidcounter;
				jitem.attr("tabid", tabid);

				//给url附加menuno
				if (url.indexOf('?') > -1)
					url += "&";
				else
					url += "?";
				url += "MenuNo=" + jitem.attr("menuno");
				jitem.attr("url", url);
			}
			f_addTab(tabid, $("span:first", jitem).html(), url);
		}).live('mouseover', function() {
			var jitem = $(this);
			jitem.addClass("over");
		}).live('mouseout', function() {
			var jitem = $(this);
			jitem.removeClass("over");
		});

		var ran = Math.random();
		$("#maintree").ligerTree({
			checkbox : false,
			nodeWidth : 130,
			textFieldName : 'text',
			data : [ {
				text : '我的调度系统',
				id : '',
				url : '',
				icon : '${ctx}/images/server.png',
				children : [ 
				{
					text : '作业计划管理',
					id : 'jobplan',
					url : "${ctx }/jobplan?method=index&forward=job/jobplan_list.jsp",
					icon : '${ctx}/images/register.gif'
				}, 
				{
					text : '作业执行日志',
					id : 'jobstate',
					url : "${ctx }/jobstate?method=index&forward=job/jobstate_list.jsp",
					icon : '${ctx}/images/searchtool.gif'
				}, 
				{
					text : '报警通知设置',
					id : 'subscribe',
					url : "${ctx }/admin/subscribe?forward=admin/subscribe_list.jsp",
					icon : '${ctx}/images/memeber.gif' 
				}, 
				{
					text : '系统配置查询',
					id : 'cluster',
 					url : "${ctx }/admin/sys?method=info",
					icon : '${ctx}/images/sysnCtml.gif' 
				}, 
				{
					text : '系统运行日志',
					id : 'log',
 					url : "${ctx }/admin/sys?method=log",
					icon : '${ctx}/images/view_detail.gif' 
				}
				]
			}

			],
			onSelect : selectNode
		});
	});

	function selectNode(node) {
		if (node.data == null || node.data === undefined)
			return false;
		if (node.data.url != "") {
			f_addTab(node.data.id, node.data.text, node.data.url);
		}
	}

	function f_heightChanged(options) {
		if (tab)
			tab.addHeight(options.diff);
		if (accordion && options.middleHeight - 24 > 0)
			accordion.setHeight(options.middleHeight - 24);
	}
	function f_addTab(tabid, text, url) {
		var flag = false;
		var links = $("li", tab.tab.links);
		for ( var i = 0; i < links.length; i++) {
			if (tabid == $(links[i]).attr("tabid")) {
				flag = true;
				break;
			}
		}
		if (flag) {
			new_tabid = tabid;
			new_text = text;
			new_url = url;
			$.ligerDialog.confirm("页面 '" + text + "' 已经打开,是否要替换当前打开的页面?",
					function(yes) {
						if (yes)
							tab.reload(tabid);
						tab.selectTabItem(tabid);
					});
		} else {
			tab.addTabItem({
				tabid : tabid,
				text : text,
				url : url
			});
		}
	}

	function f_closeIt(tabid) {
		tab.removeTabItem(tabid);
	}

	function closeWindow() {
		$.ligerDialog.confirm("您确认退出吗?", function(yes) {
			if (yes) {
				document.muf.submit();
			}
		});

	}
</script>
<style>
.l-dialog-tc {
	background: url('${ctx}/images/dialog-tr.gif') repeat-x
}
</style>
</head>
<input id="isAdmin" type="hidden" name="isAdmin" value="${isAdmin}"></input>
<body>
	<div id="pageloading"></div>
	<div id="topmenu" class="l-topmenu">
		<div class="l-topmenu-logo"></div>
		<div class="l-topmenu-welcome">
			<a href="#" onclick="closeWindow()"></a>
		</div>
	</div>
	<div id="layout1" style="width: 100%; margin: 0 auto; margin-top: 2px;">
		<div position="left" title="主要菜单" id="accordion1"
			style="height: 600px; overflow-y: auto">
			<ul id="maintree" style="width: 106px; margin-top: 12px">
			</ul>
		</div>
		<div position="center" id="framecenter">
			<div tabid="home" title="Dashboard" style="height: 300px">
				<iframe frameborder="0" name="home" id="home"
					src="${ctx}/welcome?method=info"></iframe>
			</div>
		</div>
		<div position="bottom">
			<div class="foot">版权所有：掌中浩阅科技有限公司：新出网证(京)字117号　文网文(2010)267号　京ICP备11008516号　京ICP证090653号 (建议使用1024*768及以上分辨率)</div>
		</div>
	</div>
	<form name="muf" method="post" action="${ctx}/bs?forward=exit" id="aa"></form>
</body>
</html>
