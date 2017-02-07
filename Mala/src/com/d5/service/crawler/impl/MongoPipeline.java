package com.d5.service.crawler.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.crawler.ResultItems;
import com.d5.data.transfer.DataDeal;
import com.d5.service.crawler.Pipeline;
import com.d5.service.crawler.Task;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;

public class MongoPipeline implements Pipeline {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/*
	 * MethodName：process
	 * @Author: Administrator
	 * @Date: 2017年1月18日 上午11:14:54
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @see com.d5.service.crawler.Pipeline#process(java.util.List, com.d5.service.crawler.Task)
	 */
	@Override
	public void process(List<ResultItems> resultItemsList, Task task) {
		Map<String, JSONObject> categorys = task.getSite().getCategorys();
		for(ResultItems reItems : resultItemsList){
			for (Document doc : reItems.getItems()) {
				String collectName = reItems.getCollectName();
				cleanData(doc, "detailInfo");
				//附加分类信息，同分类ID查找。
				appendCategory(doc, categorys);
	            if("douban_media_basic_info".equals(collectName)){
	            	//1.提取数值信息
	            	Document docDigital = new Document();
	            	DataDeal.getDigitalInfo(doc, docDigital);
	            	MongodbUtil.insertOne(Constants.COLL_NAME_4_DIGITAL, docDigital);
	            	log.debug("提取并存储数值信息结束");
		            //2.判断新、老数据
		            //3.新数据，原来的融合程序，抓取后批量处理；计算热度，生成评分接口数据，生成融合数据，写入redis集合；
		            //4.老数据，新的融合程序，抓取时单个处理；计算热度，更新评分接口数据，更新融合数据，写入redis集合；
	            	if(doc.containsKey("isNew") && "0".equals(doc.getString("isNew"))){
	            		//计算热度
	            		long heatValue = DataDeal.calculateHeatValue(docDigital);
	            		log.debug("计算热度结束");
	            		//写明星作品权重计算队列
	            		Document docHeat = SerializationUtils.clone(doc);
	            		docHeat.put("heatValue", heatValue);
	            		DataDeal.writeWeightQueue(docHeat);
	            		docHeat.clear();
	            		log.debug("写明星作品权重计算队列结束");
	            		//更新评分接口数据
	            		updateScoreInfo(docDigital, heatValue);
	            		log.debug("更新评分接口数据结束");
	            		//更新融合数据
	            		updateOptimization(docDigital);
	            		log.debug("更新融合数据结束");
	            		
	            		return;
	            	}
	            }
	            
	            MongodbUtil.insertOne(collectName, doc);
	            //满足条件多写一个表
	            if(StringUtils.isNotBlank(Constants.C_NAME_4_ADD)
	            		&& Constants.C_NAME_4_ADD.contains(collectName)){
	            	MongodbUtil.insertOne(Constants.C_NAME_4_ADD, doc);
	            	log.debug("写ADD表结束");
	            }
	        }
		}
	}

	/**
	 * MethodName：updateOptimization
	 * @author: Administrator
	 * @Date: 2017年1月17日 下午5:40:01
	 * @Description 更新融合数据
	 * @param docDigital
	 */
	private void updateOptimization(Document docDigital) {
		MongodbUtil.updateOne(Constants.DEAL_M_RESULT_COLL_NAME,
			new Document().append("d_entityId", docDigital.getString("entityId")),
			new Document().append("d_grade_score", docDigital.getString("ratingNum"))
				.append("ratingPeopleNum", docDigital.getInteger("ratingPeopleNum"))
				.append("collectionsNum", docDigital.getInteger("collectionsNum"))
				.append("wishesNum", docDigital.getInteger("wishesNum"))
				.append("commentCount", docDigital.getInteger("commentCount")));
	}

	/**
	 * MethodName：updateScoreInfo
	 * @author: Administrator
	 * @Date: 2017年1月17日 下午5:39:16
	 * @Description 更新评分接口的数据
	 * @param docDigital
	 * @param hotValue
	 */
	private void updateScoreInfo(Document docDigital, long heatValue) {
		MongodbUtil.updateOne(Constants.DEAL_SI_RESULT_COLL_NAME,
				new Document().append("d_entityId", docDigital.getString("entityId")),
				new Document().append("heat", heatValue).append("d_grade_score", docDigital.getString("ratingNum")));
	}

	/**
	 * 
	 * MethodName：appendCategory
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:14:02
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param doc
	 * @param categorys
	 */
	private void appendCategory(Document doc, Map<String, JSONObject> categorys) {
		if(doc.containsKey("categoryId") && !categorys.isEmpty()){
			String categoryId = doc.getString("categoryId");
			doc.remove("categoryId");
			if(categorys.containsKey(categoryId)){
				try {
					JSONObject categoryInfo = new JSONObject();
					categoryInfo.putAll(categorys.get(categoryId));
					categoryInfo.remove("url");
					categoryInfo.remove("siteKindId");
					categoryInfo.remove("siteKindName");
					doc.putAll(categoryInfo);
				} catch (Exception e) {
					log.error("{}", e.getMessage());
				}
			}
		}
	}

	/**
	 * 
	 * MethodName：cleanData
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:13:36
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param info
	 * @param cleanColName
	 */
	private void cleanData(Document info, String cleanColName){
    	if(info.containsKey(cleanColName)){
    		String value = info.getString(cleanColName);
    		log.debug(cleanColName + " value is :" + value);
    		info.remove(cleanColName);
    		fillListV2(info, value);
    	}
    }

	/**
	 * 
	 * MethodName：fillListV0
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:13:40
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param value
	 * @return
	 */
	private List<String[]> fillListV0(String value) {
		List<String[]> valueList = new ArrayList<String[]>(5);
		String temp = SerializationUtils.clone(value);
		while(temp.length() > 0){
			String[] kV = new String[2];
			int pos1 = temp.lastIndexOf(":");
			String v = temp.substring(pos1 + 1);
			if(MatchUtil.contain(v, "^//(\\w+\\.)+\\w+.*")){
				pos1 = pos1 - 5;
			}
			kV[1] = temp.substring(pos1 + 1);
			temp = temp.substring(0, pos1);
			int pos2 = temp.lastIndexOf(" ");
			if(pos2 < 1){
				kV[0] = temp;
				temp = "";
				break;
			}else{
				kV[0] = temp.substring(pos2 + 1);
				temp = temp.substring(0, pos2);
			}
			valueList.add(kV);
		}
		return valueList;
	}
	
	/**
	 * 
	 * MethodName：fillListV1
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:13:46
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param info
	 * @param value
	 */
	public void fillListV1(Document info, String value){
		String pattern = "([导演编剧类型片长官方网站主又名制片国家/地区上映日期语言链接家庭成员更多中文名更多外文名职业出生地出生日期星座性别编号生卒日期季数小集数单集片长首播]|imdb|IMDb)+:";
		String[] aa = value.split(pattern);
        List<String> values = Arrays.asList(Arrays.copyOfRange(aa, 1, aa.length));
        List<String> keys = MatchUtil.findAll(value, pattern);
        int ix = 0;
        for(String key : keys){
			String k = key.replace(":", "");
			if(Constants.PARAM_ATTR_RELA_MAP.containsKey(k)){
				k = Constants.PARAM_ATTR_RELA_MAP.get(k);
			} else {
				log.info(k + " not has map reflected name.");
			}
			info.put(k, values.get(ix));
			ix ++;
        }
	}
	
	/**
	 * 
	 * MethodName：fillListV2
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:13:50
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param info
	 * @param value
	 */
	public void fillListV2(Document info, String value){
		//用<br>分割成数组
		String[] tagGroup = value.split("<br[ /]*?>");
		for(String tagStr : tagGroup){
			//清理掉html标签
			String temp = tagStr.replaceAll("(<.*?>)|\n", "");
			String[] keyValueArr = StringUtils.split(temp, ":", 2);
			String key = keyValueArr[0].trim();
			String val = keyValueArr[1].trim();
			if(Constants.PARAM_ATTR_RELA_MAP.containsKey(key)){
				key = Constants.PARAM_ATTR_RELA_MAP.get(key);
			} else {
				log.info(key + " not has map reflected name.");
			}
			info.put(key, val);
		}
		
	}
}
