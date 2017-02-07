//当前时间的时间戳减去 需要计算的时间差  
function parseTime(diff){
	 var content = null;
     if(diff<5*60*1000 /* 5分钟内 */){
         content ="刚刚";
     }else if(diff<60*60*1000 /* 60分钟内 */){
         content = Math.floor(diff/60/1000)+"分钟前";
     }else if(diff<24*60*60*1000 /* 24小时内 */){
         content = Math.floor(diff/60/60/1000)+"小时前";
     }else{
         content = Math.floor(diff/24/60/60/1000)+"天前";
     }
     return content;
 }