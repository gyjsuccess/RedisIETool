// 时间戳转换成刚刚、几分钟前、几小时前、几天前

//刚刚
var just = new Date().getTime();

// 几分钟前
var afewminutesago = new Date("Nov 29, 2016 00:50:00").getTime();

// 几周前
var afewweekago = new Date("Nov 29, 2016 00:50:00").getTime();

// 几年前
var someday = new Date("Nov 21, 2012 01:15:00").getTime();

var helloData = {
	time : afewweekago
}

function getDateDiff(dateTimeStamp) {
	var result;
	var minute = 1000 * 60;
	var hour = minute * 60;
	var day = hour * 24;
	var halfamonth = day * 15;
	var month = day * 30;
	var now = new Date().getTime();
	var diffValue = now - dateTimeStamp;
	if (diffValue < 0) {
		return;
	}
	var monthC = diffValue / month;
	var weekC = diffValue / (7 * day);
	var dayC = diffValue / day;
	var hourC = diffValue / hour;
	var minC = diffValue / minute;
	if (monthC >= 1) {
		if (monthC <= 12)
			result = "" + parseInt(monthC) + "月前";
		else {
			result = "" + parseInt(monthC / 12) + "年前";
		}
	} else if (weekC >= 1) {
		result = "" + parseInt(weekC) + "周前";
	} else if (dayC >= 1) {
		result = "" + parseInt(dayC) + "天前";
	} else if (hourC >= 1) {
		result = "" + parseInt(hourC) + "小时前";
	} else if (minC >= 1) {
		result = "" + parseInt(minC) + "分钟前";
	} else {
		result = "刚刚";
	}

	return result;
};
