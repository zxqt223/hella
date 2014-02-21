(function($){
    var uriReg = "^((https|http|ftp|rtsp|mms)?://)"
        + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
        + "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
        + "|" // 允许IP和DOMAIN（域名）
        + "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
        + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
        + "[a-z]{2,6})" // first level domain- .com or .museum
        + "(:[0-9]{1,4})?" // 端口- :80
        + "((/?)|" // a slash isn't required if there is no file name
        + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$"
        
    $.fn.validationEngineLanguage_cycas = function(){
    };
    $.validationEngineLanguage_cycas = {
        newLang: function(){
            $.validationEngineLanguage_cycas.allRules = {
			   "mobile": {
                    "regex": /^1[3|4|5|8][0-9]\d{8}$/,
                    "alertText": "* 手机号码格式不正确"
                },
                "extphonenum":{
                	   "regex": /^\d{3,}$/,
                       "alertText": "* 分机号码格式不正确"
                },
                "uri":{
                	 "regex": new RegExp(uriReg),
                	"alertText": "* URI格式不正确"
                },
                "guid":{
                	"regex":/^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})$/, 
                	"alertText": "* GUID格式不正确"
                },
                "idCard":{
                	"regex":/(^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[A-Z])$)/, 
                	"alertText": "* 身份证格式不正确"
				},
				"floatnum":{
					"regex": /^[0-9]+([.]\d{1,2})?$/,
                    "alertText": "* 数字格式不正确"
				}
            };
            
        }
    };
    $.validationEngineLanguage_cycas.newLang();
})(jQuery);