package com.d5.thread.main;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.crawler.Spider;
import com.d5.service.data.IDataRedisService;
import com.d5.util.CommonUtil;

public class InterfaceMainThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private int sleepTime;
	private IDataRedisService dataService = Constants.dataRedisService;
	private Spider task;
	public InterfaceMainThread(){
	}
	
	public InterfaceMainThread(int sleepTime){
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void run() {
		logger.debug("接口程序 启动了。");
		while(true){
			String jsonStr = null;
			try{
				jsonStr = dataService.getInfoFromList(Constants.REDIS_INDEX_4_L, Constants.KEY_4_WARNNING_QUEUE);
				//logger.debug("接口程序运行中...，获取到的预警数据为：{}", jsonStr);
				JSONObject jsonObj = null;
				if(jsonStr != null){
					jsonObj = JSONObject.parseObject(jsonStr);
					
					if(!StringUtils.equals(jsonObj.getString(Constants.WarnningConstants.siteId),
							task.getSite().getSiteInfo().getString(Constants.SiteConstants.siteId))){
						continue;
					}
					
					//读取执行时间
					String execTime = null;
					try {
						execTime = jsonObj.getString(Constants.WarnningConstants.excuteTime);
					} catch(JSONException e) {
					}
					if(execTime == null){//没有执行时间，是新的预警数据
						//计算执行时间
						execTime = getExecTime(jsonObj.getString(Constants.WarnningConstants.planExcuteTime),
								jsonObj.getIntValue(Constants.WarnningConstants.warnningLevel));
						
						jsonObj.put(Constants.WarnningConstants.excuteTime, execTime);
						dataService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.KEY_4_WARNNING_QUEUE, jsonObj.toString());
					} else {
						//判断是否到了执行的时间
						if(isAfterNow(execTime)){//未到执行时间,放回预警队列中
							dataService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.KEY_4_WARNNING_QUEUE, jsonObj.toString());
						} else {//到了执行时间,放入预警任务队列中
							//为了防止预警之后，未爬取之前等待的这段时间里，人工取消预警。故而判断要加在这个位置
							//判断是否已经在页面上解除了预警。存在数据，返回true，已经解除预警了；否则，未人工解除预警。
							if (dataService.existInHash(Constants.REDIS_INDEX_4_H, Constants.MAN_UNWARNNING, jsonObj.getString(Constants.WarnningConstants.id))) {
								continue;
							}
							
							//dataService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.KEY_4_WARNNING_QUEUE, "");
							jsonObj.put("isNew", "2");
							genAndSaveRequest(jsonObj, task);
						}
					}
					
					jsonStr = null;
					jsonObj = null;
				}
			} catch (Exception e) {
				logger.error("json格式串为：{}", jsonStr, "---错误信息为：{}", e);
			}
			
			try { //休眠50毫秒
				new Thread().sleep(sleepTime);
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
			}
		}
	}
	
	private void genAndSaveRequest(JSONObject jsonObj, Spider task) {
		JSONObject jsonObj_ = (JSONObject) jsonObj.clone();
		Site site = task.getSite();
		Request request = new Request(jsonObj_.getString(Constants.WarnningConstants.entityUrl));
		CommonUtil.addInitData2Request(request, site.getSiteInfo(), jsonObj_);
        
		task.getScheduler().push(request, task);
	}

	private String getExecTime(String planExecTime, int warnningLevel) {
		//预警级别只有1、2、3、4级别。数值越小，预警等级越大/*小*/，时间间隔越短/*长*/。
		//int delayMins = warnningLevel/*(5 - warnningLevel)*/ * Utils.intervalTime;
		int delayMins = Constants.LEVEL_MINUTES_MAP.get(warnningLevel);
		DateTimeFormatter format = DateTimeFormat.forPattern(Constants.DATE_FORMATTER);
        //时间解析
        DateTime dateTime = DateTime.parse(planExecTime, format).withSecondOfMinute(0);
        dateTime = dateTime.plusMinutes(delayMins);
        
        return dateTime.toString(Constants.DATE_FORMATTER);
	}
	
	private static boolean isAfterNow(String startTime){
		DateTime startDateTime = DateTime.parse(startTime,
				DateTimeFormat.forPattern(Constants.DATE_FORMATTER));
		//时间轴，位置与当前时间比较
		return startDateTime.isAfterNow();
	}

}
