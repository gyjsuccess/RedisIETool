package com.d5.tool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.crawler.Spider;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;

public class InitSeedsUtil {
	private static Logger log = LoggerFactory.getLogger(InitSeedsUtil.class);
	public static void run(Spider spider, String urlTemplate, String collectionName, Enums.SeedsTypeEnum type) {
		if(StringUtils.isBlank(collectionName) && Enums.SeedsTypeEnum.ADD.equals(type)){
			log.error("collect name is error.");
			return;
		}
    	Site site = spider.getSite();
    	List<Request> startRequests = site.getStartRequests();
    	startRequests.clear();
    	
    	List<String> startUrls = Lists.newArrayList();
    	if(Enums.SeedsTypeEnum.ADD.equals(type)){
    		genStartUrls(startUrls, urlTemplate, collectionName);
    		site.initStartRequest(startUrls);
        	spider.addRequest(startRequests);
    	}
    	if(Enums.SeedsTypeEnum.ALL.equals(type)){
    		genStartRequests(startRequests, spider, site.getSiteInfo());
    		spider.setExecuteTime(DateTime.now().toString(Constants.DATE_FORMATTER));
    	}
    }
	
	/**
	 * MethodName：genStartRequests
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午4:36:56
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param urlTemplate
	 * @param collectionName
	 * @param site
	 * @param spider
	 */
	private static void genStartRequests(List<Request> startRequests, Spider spider, JSONObject siteInfo) {
		JSONObject initData = JSONObject.parseObject("{}");
		if(siteInfo != null){
			initData.putAll(siteInfo);
		}
		
		Map<String, String> allData = Constants.dataRedisService.getAllInfoFromMap(
										Constants.REDIS_INDEX_4_H, Constants.KEY_DB_MERGE);
		for(Map.Entry<String, String> en : allData.entrySet()){
			JSONObject info = JSONObject.parseObject("{}");
			String[] vals = en.getValue().split("#");
			initData.put("entityId", vals[0]);
			if(vals.length == 2){
				initData.put("tv_model", vals[1]);
			}
	        info.put(Enums.JsonColums.InitData.toString(), initData.toString());
	        startRequests.add(new Request(en.getKey()).putExtra(Request.DATAS, info.toString()));
		}
        
        spider.addRequest(startRequests);
	}

	/** 
	 * 查询全量表，生成Url，加入队列抓取数据。
	 *  
	 * @param startUrls
	 * @param urlTemplate
	 * @param collectionName
	 */
	public static void genStartUrls(List<String> startUrls, String urlTemplate, String collectionName) {
		genStartUrls(startUrls, new Document(), urlTemplate, collectionName, 2000);
	}
	
	/**
	 * 查询全量表，生成Url，加入队列抓取数据。
	 * @param startUrls
	 * @param query
	 * @param urlTemplate
	 * @param collectionName
	 */
	public static void genStartUrls(List<String> startUrls, Document query, String urlTemplate, String collectionName,
			int limitNum) {
		Document projectDoc = new Document().append("name", "1").append("model", "1").append("_id", "0");
		
		List<Document> docList = Lists.newArrayList();
		try{
			Long lTotal = MongodbUtil.count(collectionName, query);
			int total = lTotal.intValue();
			int skipNum = 0;
			int i = 0;
			while(skipNum <= total/* && i < 1*/){
				docList.addAll(MongodbUtil.find(collectionName, query, projectDoc, skipNum, limitNum));
				log.info("genStartUrls, {}, docList size is :{}", i + 1, docList.size());
				String name = null;
				String model = null;
				for(Document doc : docList){
					model = doc.getString("model");
					if(!MatchUtil.contain(model, "tv|movie")){
						continue;
					}
					name = doc.getString("name");
					try {
						name = URLEncoder.encode(name, "utf-8");
					} catch (UnsupportedEncodingException e) {
						log.error("{}", e);
					}
					startUrls.add(urlTemplate.replace("{name}", name));
				}
				skipNum += limitNum;
				i ++;
				docList.clear();
			}
		}catch(Exception e){
			log.error("{}", e);
		}
	}
	
	/**
	 * 查询优化结果表，获取到entityID。再根据entityID查询到url。加入到队列中
	 * 
	 * @param startUrls
	 * @param urlTemplate
	 * @param collectionName
	 */
	public static void genStartUrlsOptimized(List<String> startUrls, String urlTemplate, String collectionName) {
		List<Document> docList = MongodbUtil.find(collectionName,
				Filters.and(Filters.regex("model", "tv|movie"), Filters.exists("d_entityId")),
				new Document().append("d_entityId", "1").append("_id", "0"));
		String name = null;
		String model = null;
		for(Document doc : docList){
			model = doc.getString("model");
			if(!MatchUtil.contain(model, "tv|movie")){
				continue;
			}
			name = doc.getString("name");
			try {
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error("{}", e);
			}
			startUrls.add(urlTemplate.replace("{name}", name));
		}
	}
}
