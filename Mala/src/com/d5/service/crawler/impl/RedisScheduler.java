package com.d5.service.crawler.impl;

import com.alibaba.fastjson.JSON;
import com.d5.common.Constants;
import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.service.crawler.Scheduler;
import com.d5.service.crawler.Task;
import com.d5.util.CommonUtil;

public class RedisScheduler implements Scheduler {

	@Override
	public void push(Request request, Task task) {
		Site site = task.getSite();
		
		/*String dataStr = String.valueOf(request.getExtra(Request.DATAS));
		if(StringUtils.isBlank(dataStr)){//种子
			JSONObject info = JSONObject.parseObject("{}");
			SiteInfo siteInfo = site.getSiteInfo();
	        info.put(Constants.JsonColums.InitData.toString(), JSON.toJSONString(siteInfo));
	        request.putExtra(Request.DATAS, info.toString());
		}*/
		CommonUtil.addInitData2Request(request, site.getSiteInfo(), null);
        
		Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L,
				site.getRedisKeyName4Downloader(),
				JSON.toJSONString(request));
	}

	@Override
	public Request poll(Task task) {
		Site site = task.getSite();
		String infoStr = Constants.dataRedisService.getInfoFromList(Constants.REDIS_INDEX_4_L,
				site.getRedisKeyName4Downloader());
		
		Request request = JSON.parseObject(infoStr, Request.class);
		return request;
	}

}
