<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<HTML>
<HEAD>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <TITLE>Hella-掌阅任务调度系统</TITLE>
  <LINK href="/css/User_Login.css" type="text/css" rel="stylesheet">
  <script>
    function doCheck(){
      if(document.getElementById('userName').value==''){
        alert("Please input user name!");
        return false;
      }
      if(document.getElementById('password').value==''){
        alert("Please input password!");
        return false;
      }
      return true;
    }
  </script>
</HEAD>
<BODY id="userlogin_body">
<FORM method="post" action="/jsp/index.jsp" name="loginForm" onsubmit="return doCheck()">
<DIV></DIV>
<DIV id="user_login">
<DL>
  
  <DD id="user_top">
    <UL>
      <LI class="user_top_l"></LI>
      <LI class="user_top_c"></LI>
      <LI class="user_top_r"></LI>
    </UL>
    <DD id="user_main">
    <UL>
      <LI class="user_main_l"></LI>
      <LI class="user_main_c">
        <DIV class="user_main_box">
		    <%
		      String msg=request.getParameter("msg");
		      if(msg!=null){
		          if("0".equals(msg)){
		    %>
		    <UL><LI><font color="blue" size="2">提示：请重新登陆</font></LI></UL>
		    <%    }else if("1".equals(msg)){%>
		    <UL><LI><font color="red" size="2">提示：密码和用户不对，请重新输入</font></LI></UL>
		    <%    } 
		      }
		    %>
          <UL>
            <LI class="user_main_text">用户名</LI>
            <LI class="user_main_input"><INPUT class="TxtUserNameCssClass" id="userName" maxLength="20" name="userName"></LI>
          </UL>
          <UL>
            <LI class="user_main_text">密&nbsp;&nbsp;&nbsp;&nbsp;码</LI>
            <LI class="user_main_input"><INPUT class="TxtPasswordCssClass" id="password" type="password" name="password"></LI>
          </UL>
          <UL>
            <LI class=user_main_text>Cookie</LI>
            <LI class=user_main_input>
              <SELECT id="DropExpiration" name="DropExpiration"> 
                <OPTION value="None selected">不保存</OPTION>
                <OPTION value="Day">保存一天</OPTION> 
                <OPTION value="Month">保存一月</OPTION>
                <OPTION value="Year">保存一年</OPTION>
             </SELECT>
            </LI>
          </UL>
         </DIV>
      </LI>
      <LI class="user_main_r"><INPUT class="IbtnEnterCssClass" id="IbtnEnter" style="BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px" 
             type="image" src="/images/login/user_botton.gif" name="IbtnEnter">
      </LI>
   </UL>
  <DD id="user_bottom">
    <UL>
      <LI class="user_bottom_l"></LI>
      <LI class="user_bottom_c"></LI>
    <LI class="user_bottom_r"></LI>
    </UL>
  </DD>
</DL>
</DIV>
<SPAN id="ValrUserName" style="DISPLAY: none; COLOR: red"></SPAN>
<SPAN id="ValrPassword" style="DISPLAY: none; COLOR: red"></SPAN>
<SPAN id="ValrValidateCode" style="DISPLAY: none; COLOR: red"></SPAN>
<DIV id=ValidationSummary1 style="DISPLAY: none; COLOR: red"></DIV>
<DIV></DIV>
</FORM>
</BODY>
</HTML>
