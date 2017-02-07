package com.d5.data.transfer;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;

public class GenScoreInterfaceData {
	private static Logger log = LoggerFactory.getLogger(GenScoreInterfaceData.class);
	
	/**
	 * 
	 * MethodName：mergeData
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:35:45
	 * @Description 从融合结果中生成豆瓣评分接口数据
	 * @param collectionNameSource
	 * @param collectionNameResult
	 */
	public static void genGradeScore(String collectionNameSource, String collectionNameResult, int limitNum) {
		if(StringUtils.isBlank(collectionNameSource) || StringUtils.isBlank(collectionNameResult)){
			log.error("collect name is error.");
			return;
		}
		//循环数据，查询目标数据		
		//将查询到的目标数据放入Result中
		Integer dataCount = 0;
		List<Document> sDocListAll = Lists.newArrayList();
		Document query = new Document();
		try{
			Long lTotal = MongodbUtil.count(collectionNameSource, query);
			int total = lTotal.intValue();
			int skipNum = 0; 
			while(skipNum <= total){
				sDocListAll.addAll(MongodbUtil.find(collectionNameSource
						, new Document().append("d_entityId", Pattern.compile(".*" + "\\d+" + ".*"))
						, new Document().append("_id", "1").append("d_entityId", "1").append("pp_tencent", "1")
						.append("d_grade_score", "1").append("ratingPeopleNum", "1")
						.append("d_issue", "1")
						, skipNum, limitNum));
				if(sDocListAll.size() == 0){
					skipNum = total+1;
				}
				dataCount += sDocListAll.size();
//				List<Document> sDocListAll = MongodbUtil.find(collectionNameSource, Filters.regex("model", "(tv|movie)"));
				genData(collectionNameResult, sDocListAll, dataCount);
				sDocListAll.clear();
				skipNum += limitNum;
				log.debug("dataCount is : {}  "+dataCount);
			}
		}catch(Exception e){
			log.error("{}", e);
		}
	}
	/**
	 * 
	 * MethodName：genData
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:27:54
	 * @Description 融合程序调用，单条生成豆瓣评分接口数据
	 * @param collectionNameResult
	 * @param sDoc
	 */
	public static void genData(String collectionNameResult, Document sDoc, long heatValue) {
		Document doc = new Document();
		try{
			doc.append("_id", sDoc.get("_id"))
			.append("d_entityId", sDoc.getString("d_entityId"))
			.append("pp_tencent", sDoc.get("pp_tencent"))
			.append("d_grade_score", sDoc.getString("d_grade_score"))
			.append("ratingPeopleNum", String.valueOf(sDoc.get("ratingPeopleNum")))
			.append("d_issue", sDoc.getString("d_issue"));
			DataDeal.dealData(doc, heatValue);
		}catch(Exception e){
			log.error("{}", e);
		} finally {
			doc.remove("d_issue");
			doc.remove("ratingPeopleNum");
			doc.remove("pp_tencent");
		}
		MongodbUtil.insertOne(collectionNameResult, doc);
	}
	/**
	 * 
	 * MethodName：genData
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:27:37
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param collectionNameResult
	 * @param sDocListAll
	 * @param dataCount
	 */
	private static void genData(String collectionNameResult,
			List<Document> sDocListAll, Integer dataCount) {
		List<Document> tDocLists = Lists.newArrayList();
		try{
			for(Document sDoc : sDocListAll){
				double heat1 = 0;
				String ratingPeopleNum = sDoc.containsKey("ratingPeopleNum")?sDoc.get("ratingPeopleNum").toString():"0";
				String issue = sDoc.containsKey("d_issue")?sDoc.getString("d_issue"):"";
				issue = MatchUtil.find(issue, "(\\d{4}\\-\\d{1,2}\\-\\d{1,2})", "", 1);
				if(!issue.isEmpty()){
					Integer diff = Math.abs(Months.monthsBetween(DateTime.now(), DateTime.parse(issue)).getMonths());
					if(!diff.equals(0)){
						heat1 = Long.parseLong(ratingPeopleNum)/diff;
					}
				}
				long heat = Math.round(Math.log(heat1 + 1) / 16 * 1000);
				DataDeal.dealData(sDoc, heat);
				dataCount ++;
				tDocLists.add(sDoc);
			}
		}catch(Exception e){
			log.error("{}"+e);
		}
		MongodbUtil.insertMany(collectionNameResult, tDocLists);
	}
	
	public static void main(String[] args) {
		genGradeScore("chiq_video_optimization", "chiq_video_converge_score_2", 2000);
	}
}
