/**
* jQuery ligerUI 1.1.9
* 
* http://ligerui.com
*  
* Author daomi 2012 [ gd_star@163.com ] 
* 
*/
(function ($)
{
	var myitems="";
    $.fn.ligerToolBar = function (options)
    {
        return $.ligerui.run.call(this, "ligerToolBar", arguments);
    };

    $.fn.ligerGetToolBarManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetToolBarManager", arguments);
    };

    $.ligerDefaults.ToolBar = {};

    $.ligerMethos.ToolBar = {};

    $.ligerui.controls.ToolBar = function (element, options)
    {
        $.ligerui.controls.ToolBar.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.ToolBar.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'ToolBar';
        },
        __idPrev: function ()
        {
            return 'ToolBar';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.ToolBar;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.toolBar = $(this.element);
            g.toolBar.addClass("l-toolbar");
            g.set(p);
        },
        _setItems: function (items)
        {myitems=items;
            var g = this;
            $(items).each(function (i, item)
            {
                g.addItem(item);
            });
        },
        addItem: function (item)
        {
            var g = this, p = this.options;
            if (item.line)
            {
                g.toolBar.append('<div class="l-bar-separator"></div>');
                return;
            }
            var ditem = $('<div class="l-toolbar-item l-panel-btn"><span></span><div class="l-panel-btn-l"></div><div class="l-panel-btn-r"></div></div>');
            g.toolBar.append(ditem);
            item.id && ditem.attr("toolbarid", item.id);
            if (item.img)
            {
                ditem.append("<img src='" + item.img + "' />");
                ditem.addClass("l-toolbar-item-hasicon");
            }
            else if (item.icon)
            {
               // ditem.append("<div class='l-icon l-icon-" + item.icon + "'></div>");
               // ditem.addClass("l-toolbar-item-hasicon");
            }
            item.text && $("span:first", ditem).html(item.text);
            if(item.disable){
            	if (item.icon)
                {
                    ditem.append("<div class='l-icon l-icon-" + item.icon + "_disabled'></div>");
                    ditem.addClass("l-toolbar-item-hasicon");
                }
            }else{
            	if (item.icon)
                {
                    ditem.append("<div class='l-icon l-icon-" + item.icon + "'></div>");
                    ditem.addClass("l-toolbar-item-hasicon");
                }
                item.click && ditem.click(function () { item.click(item); });
                ditem.hover(function ()
                {
                    $(this).addClass("l-panel-btn-over");
                }, function ()
                {
                    $(this).removeClass("l-panel-btn-over");
                });
            }
           // item.disable && ditem.addClass("l-toolbar-item-disable");
      
        },
		updateItem:function(operators){
    		var toolBars = $(".l-toolbar-item");
    		  for(var j=0;j<operators.length;j++){
    		        var ope = operators[j];
    				if(ope){
    					//$(toolBars[j]).unbind("hover");
    					$(toolBars[j]).unbind('mouseenter mouseleave');

    						$(toolBars[j]).hover(function ()
    							{   $(this).removeClass("l-toolbar-item-disable");
    								$(this).addClass("l-panel-btn-over");
    							}, function ()
    							{
    								$(this).removeClass("l-panel-btn-over");
    								$(this).removeClass("l-toolbar-item-disable");
    							});
    						 $(toolBars[j]).unbind("click");	 
    						$(toolBars[j]).bind("click",myitems[j*2].click);
    							 $(".l-icon", $(toolBars[j])).removeClass("l-icon-"+ myitems[j*2].icon + "_disabled").addClass("l-icon-"+ myitems[j*2].icon );
    				}else{
							$(".l-icon", $(toolBars[j])).addClass("l-icon-"+ myitems[j*2].icon + "_disabled").removeClass("l-icon-"+ myitems[j*2].icon );
    				   // $(toolBars[j]).unbind("hover");
							$(toolBars[j]).unbind('mouseenter mouseleave');
    					 $(toolBars[j]).unbind("click");
    				}
    		  }
    		}
    });
})(jQuery);