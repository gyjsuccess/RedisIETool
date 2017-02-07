var system = require('system');
var url = system.args[1];

var page = require('webpage').create();
page.settings.loadImages = false;
page.settings.resourceTimeout = 5000;
phantom.outputEncoding="utf8";

page.settings.userAgent='Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36';
page.customHeaders.accept="text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
page.customHeaders.acceptLanguage="zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3";
page.customHeaders.acceptEncoding="gzip, deflate, sdch";

function isX(url, target){
	var result = "";
	var myregexp = /(\w+\.)+\w+/;
	var match = myregexp.exec(url);
	if (match != null) {
		result = match[0];
	}
	if(result.indexOf(target) >= 0){
		return true;
	} else {
		return false;
	}
}

var isWeibo = isX(url, "weibo.com");

page.open(url, function (status) {
    if (status != 'success') {
        console.log("HTTP request failed!");
    } else {
        console.log(page.content);
		if(isWeibo){
			/*var sc = page.evaluate(function(){
				return document.body.innerHTML;
			});*/
			window.setTimeout(function(){
				//console.log(sc);
				console.log(page.content);
				page.release();
				page.close();
				phantom.exit();
			},5000);
		}
    }
	if(!isWeibo){
		page.release();
		page.close();
		phantom.exit();
	}
});