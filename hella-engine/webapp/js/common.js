String.prototype.trim=function(){
   var reSpace=/^\s*(.*?)\s*$/;
   return this.replace(reSpace,"$1");
};
/**
 * 验证邮件地址是否正确
 */
String.prototype.isEmail = function() {
	return this.match(/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/g);
};

/**
 * 判断字符由字母和数字，下划线组成.且开头的只能是字母
 */
String.prototype.isValidName = function() {
	return this.match(/^([a-zA-z]{1})([a-zA-z_0-9]*)$/g);
};

/**
 * 判断字符串是不是JSON对象字符串形式
 */
String.prototype.isJsonObjectString = function() {
	return this.match(/^(?:\<pre[^\>]*\>)?(\{.*\})(?:\<\/pre\>)?$/ig);
};

/**
 * 把JSON字符串转为JSON对象
 * 
 * @param jsonString JSON对象字符串形式
 */
function toJsonObject(jsonString){
  if(typeof jsonString == 'object')
    return jsonString;
	jsonString = jsonString.replace(/^(?:\<pre[^\>]*\>)?(\{.*\})(?:\<\/pre\>)?$/ig,"$1");
	return eval('(' + jsonString + ')');
}

var loadingIco = "<img src=\"${base}/images/reglog/data-loading.gif\" align=\"absmiddle\" /> ";
// 定义String的startsWith函数
String.prototype.startsWith = function(str) {
	return (this.match('^' + str) == str);
};

// 定义String的endsWith函数
String.prototype.endsWith = function(str) {
	return (this.match(str + '$') == str);
};

// 定义数组的移除函数
Array.prototype.erase = function(item){
	for (var i = this.length; i--; i){
		if (this[i] === item) this.splice(i, 1);
	}
	return this;
};

function Event_stopPropagation(e){
	if (e.stopPropagation) e.stopPropagation();
	else e.cancelBubble = true;
	return e;
}
function Event_preventDefault(e){
	if (e.preventDefault) e.preventDefault();
	else e.returnValue = false;
	return e;
}
function Event_stop(e){
	e = Event_stopPropagation(e);
	e = Event_preventDefault(e);
	return e;
}
function Event_target(e){
	return e.target || e.srcElement;;
}

/**
 * 是否JSON字符串
 * 
 * @param {String} str 字符串
 */
function isJsonString(str) {
	return str.startsWith('{') && str.endsWith('}');
}
 
/**
 * 跨浏览器“复制到粘贴板”
 * 
 * @param {String} txt 要复制的内容
 */
function copyToClipboard(txt) {
	if (window.clipboardData) {
		window.clipboardData.clearData();
		window.clipboardData.setData("Text", txt);
		alert("复制成功，您可以马上粘贴给好友了！");
	} else if (navigator.userAgent.indexOf("Opera") != -1) {
		window.location = txt;
	} else if (window.netscape) {
		try {
			netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
		} catch (e) {
			alert("如果您正在使用FireFox！\n请在浏览器地址栏输入'about:config'并回车\n然后将'signed.applets.codebase_principal_support'设置为'true'");
		}
		var clip = Components.classes['@mozilla.org/widget/clipboard;1']
				.createInstance(Components.interfaces.nsIClipboard);
		if (!clip)
			return;
		var trans = Components.classes['@mozilla.org/widget/transferable;1']
				.createInstance(Components.interfaces.nsITransferable);
		if (!trans)
			return;
		trans.addDataFlavor('text/unicode');
		var str = new Object();
		var len = new Object();
		var str = Components.classes["@mozilla.org/supports-string;1"]
				.createInstance(Components.interfaces.nsISupportsString);
		var copytext = txt;
		str.data = copytext;
		trans.setTransferData("text/unicode", str, copytext.length * 2);
		var clipid = Components.interfaces.nsIClipboard;
		if (!clip)
			return false;
		clip.setData(trans, null, clipid.kGlobalClipboard);
		alert("复制成功，您可以马上粘贴给好友了！");
	}
}

/**
 * 禁止粘贴(非IE浏览器)
 */
function fncKeyStop(evt) {
	if (!window.event) {
		var keycode = evt.keyCode;
		var key = String.fromCharCode(keycode).toLowerCase();
		if (evt.ctrlKey && key == "v") {
			evt.preventDefault();
			evt.stopPropagation();
		}
	}
}

 
 

function IsDigit(evt){
  var keyCode = evt.keyCode || evt.which;
  return ((keyCode >= 48) && (keyCode <= 57)||keyCode == 13);
}

function addBookmark(title,url) {
	if (window.sidebar) { 
		window.sidebar.addPanel(title, url,""); 
	} else if( document.all ) {
		window.external.AddFavorite( url, title);
	} else if( window.opera && window.print ) {
		return true;
	}
} 	