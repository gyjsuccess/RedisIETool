package com.d5.data.transfer;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.d5.common.Constants;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;

public class MergeMongodbDatas {
	private static Logger log = LoggerFactory.getLogger(MergeMongodbDatas.class);
	private static Integer indexFindNull = 0;
	private static Integer indexFindMore = 0;
	public static void mergeData(String collectionNameSource,
			String collectionNameTarget, String collectionNameResult, String redisKeyName, int index) {
		if(StringUtils.isBlank(collectionNameSource)
				|| StringUtils.isBlank(collectionNameTarget)
				|| StringUtils.isBlank(collectionNameResult)){
			log.error("collect name is error.");
			return;
		}
		
		indexFindNull = 0;
		indexFindMore = 0;
		
		//循环数据，查询目标数据		
		//将查询到的目标数据放入Result中
		List<Document> sDocListAll = Lists.newArrayList();
		Integer indexData = 0;
		int limitNum = 2000; //每次取出的数据量
		Document query = new Document();
		try{
			Long lTotal = MongodbUtil.count(collectionNameSource, query);
			int total = lTotal.intValue();
			int skipNum = 0;
			while(skipNum <= total ){
				sDocListAll.addAll(MongodbUtil.find(collectionNameSource, query, skipNum, limitNum));
				log.info("merge data, {}, sDocListAll size is :{}   ",+sDocListAll.size());
				skipNum += limitNum;
				mergeDeal(collectionNameTarget, collectionNameResult, sDocListAll, indexData, redisKeyName, index);
				sDocListAll.clear();
				log.info("merged data total is :{}", indexData);    //合并的数据
			}
		}catch(Exception e){
			log.error("{}", e);
		} finally {
			log.info("not find data, total is :{}", indexFindNull);//未检索到的数据数
			log.info("find more than one, total is :{}", indexFindMore);//检索到多条
		}
	}
	private static void mergeDeal(String collectionNameTarget, String collectionNameResult
			, List<Document> sDocListAll, int indexData
			, String redisKeyName, int index) {
		List<Document> tDocList  = Lists.newArrayList();
		Map<String, String> idUrlMap = Maps.newHashMap();
		for(Document sDoc : sDocListAll){
			try {
				String model = sDoc.getString("model");
				if(!model.matches("tv|movie")){
					continue;
				}
				tDocList.clear();
				findInMongodb(tDocList, sDoc, collectionNameTarget);
				for(Document tDoc : tDocList){
					//融合数据
					fillData(sDoc, tDoc, idUrlMap);
					//热度计算
					long heatValue = calculateHeatValue(tDoc);
	        		//写入评分接口数据,生成豆瓣评分接口数据
					GenScoreInterfaceData.genData(Constants.DEAL_SI_RESULT_COLL_NAME, sDoc, heatValue);
	            	//写明星作品权重队列
					Document docHeat = SerializationUtils.clone(tDoc);
            		docHeat.put("heatValue", heatValue);
            		docHeat.put("tv_model", sDoc.getString("model"));
            		DataDeal.writeWeightQueue(docHeat);
            		docHeat.clear();
					
					indexData++;
				}
			} catch(Exception e) {
				log.error("{}", e);
			}
		}
		MongodbUtil.insertMany(collectionNameResult, sDocListAll);
		//写融合redis
		if(idUrlMap.size() > 0){
			Constants.dataRedisService.addInfo2Map(index, redisKeyName, idUrlMap);
		}
	}
	/**
	 * MethodName：calculateHeatValue
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午4:05:38
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param tDoc
	 * @param heatValue
	 */
	private static long calculateHeatValue(Document tDoc) {
		//提取数值信息
    	Document docDigital = new Document();
    	DataDeal.getDigitalInfo(tDoc, docDigital);
    	//MongodbUtil.insertOne(Constants.COLL_NAME_4_DIGITAL, docDigital);
    	//热度计算
    	return DataDeal.calculateHeatValue(docDigital);
	}
	/**
	 * MethodName：fillData
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:17:09
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sDoc
	 * @param tDoc
	 * @param idUrlMap
	 */
	private static void fillData(Document sDoc, Document tDoc, Map<String, String> idUrlMap) {
		DataDeal.putStringExists("d_issue", "issue", sDoc, tDoc, "\\d{4}-\\d{2}-\\d{2}|\\d{4}-\\d{2}|\\d{4}");   //豆瓣上映日期
		DataDeal.putStringExists("d_grade_score", "ratingNum", sDoc, tDoc);   //豆瓣评分
		String tag = sDoc.containsKey("tag")?sDoc.getString("tag"):"";
		String tags = tDoc.containsKey("tags")?tDoc.getString("tags"):"";
		if(StringUtils.isBlank(tags)){
			sDoc.put("tag", tag);
		} else if (StringUtils.isBlank(tag)) {
			sDoc.put("tag", tags.replaceAll(" ", "/"));
		} else {
			sDoc.put("tag", StringUtils.join(tag, "/", tags.replaceAll(" ", "/")));
		}
		sDoc.append("semantemeTag", "");  //语义标签
		sDoc.append("tagWeight", "");    //标签权重
		DataDeal.putStringExists("year", "year", sDoc, tDoc); //年份
		DataDeal.putStringExists("director", "director", sDoc, tDoc); //导演
		DataDeal.putStringExists("writer", "writer", sDoc, tDoc); //编剧
		DataDeal.putStringExists("cast", "cast", sDoc, tDoc); //主演
		DataDeal.putStringExists("country", "countryArea", sDoc, tDoc); //制片国家
		DataDeal.putStringExists("summary", "summariness", sDoc, tDoc); //剧情
		DataDeal.putStringExists("language", "language", sDoc, tDoc); //语言
		DataDeal.putStringExists("status", "episodeCount", sDoc, tDoc, " +", ""); //集数
		DataDeal.putStringExists("d_entityId", "entityId", sDoc, tDoc); //entityId号 
		
		DataDeal.putIntegerExists("duration", "duration", sDoc, tDoc, "(\\d+){1}"); //片长
		DataDeal.putStringExists("anotherName", "anotherName", sDoc, tDoc); //又名
		DataDeal.putStringExists("imdbLink", "imdbLink", sDoc, tDoc); //IMDB编号
		DataDeal.putIntegerExists("ratingPeopleNum", "ratingPeopleNum", sDoc, tDoc, "\\d+"); //评论人数
		//  在看人数，TV有。
		DataDeal.putIntegerExists("collectionsNum", "collectionsNum", sDoc, tDoc, "\\d+"); //看过的人
		DataDeal.putIntegerExists("wishesNum", "wishesNum", sDoc, tDoc, "\\d+"); //想看的人
		DataDeal.putIntegerExists("commentCount", "commentCount", sDoc, tDoc, "\\d+"); //影评数
		
		DataDeal.putStringExists("refereredNames", "refereredNames", sDoc, tDoc, " +", "/"); //关联影片名称
		List<String> refereredDetail = Lists.newArrayList();
		if(tDoc.containsKey("refereredDetail")){
			List<String> aList = MatchUtil.findAll(tDoc.getString("refereredDetail"), "<a href=\"[\\w\\d\\./\\?=\\-:]*page\"");
			for(String aStr : aList){
				refereredDetail.add(MatchUtil.find(aStr, "\\d+", "", 0));
			}
		}
		sDoc.append("refereredDetail", StringUtils.join(refereredDetail, "/"));   //电影链接编号
		
		if(tDoc.containsKey("entityId") && tDoc.containsKey("entityUrl")){
			idUrlMap.put(tDoc.getString("entityUrl"), tDoc.getString("entityId") + "#" + sDoc.getString("model"));
		}
	}
	/**
	 * MethodName：findInMongodb
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:09:34
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param tDocList
	 * @param sDoc
	 */
	private static void findInMongodb(List<Document> tDocList, Document sDoc, String collectionName) {
		if(Pattern.compile("第.*季").matcher(sDoc.getString("name")).find()){  //规范化电视剧名称，使保持一致
			findDataTwo(tDocList, collectionName, sDoc,"name","第.*季", " "+MatchUtil.find(sDoc.getString("name"), "第.*季", ""),"director","/"," / ");
		}else if(Pattern.compile("麦田电影院.+").matcher(sDoc.getString("name")).find()){
			return;
		}else{
			findDataTwo(tDocList, collectionName, sDoc, "name", "\\(.*|\\[.*| .*", "","director","/"," / ");  //名称+导演进行第一次匹配，规范化名称和导演分隔
		}
		if(tDocList.isEmpty()){
			findDataTwo(tDocList, collectionName, sDoc, "name", "name", "\\(.*|\\[.*| .*", "", ".*", "year", "", "", "\\d{4}");  //名称+年份进行第二次匹配，排除导演无法匹配的情况，年份必须放在第二个
			if(tDocList.isEmpty()){
				findDataTwo(tDocList, collectionName, sDoc, "cast", "/", " / ", "director", "/", " / ");   //主演+导演进行第三次匹配，排除名称无法匹配的情况
				if(tDocList.isEmpty()){
					findDataTwo(tDocList, collectionName, sDoc, "anotherName", "name", "\\(.*|\\[.*| .*", "", ".*", "director", "/", " / ", ".*");  //又名+导演第四次匹配，用于部分又名对应的情况
					if(tDocList.isEmpty()){
						findDataOne(tDocList, collectionName, sDoc, "name", "\\(.*|\\[.*| .*", "");  //只使用名称进行第五次匹配，对仍不能匹配的数据筛选
						if(tDocList.size()!=1){
							indexFindNull ++;
							tDocList.clear();
							return;
						}
					}
				}
			}
		}
		if(tDocList.size() > 1){
			findDataMore(tDocList, collectionName, sDoc); //对于匹配出多条的数据同时使用名称+导演+年份进行匹配
			if(tDocList.size() != 1){
				indexFindMore ++;
				tDocList.clear();
				return;
			}
		}
	}
	private static void findDataOne(List<Document> tDocList, String collectionNameTarget
			, Document sDoc, String column1, String regex1, String toRegex1) {
		
		List<Document> tDocLists  = MongodbUtil.find(collectionNameTarget, new Document()
				.append(column1, Pattern.compile(".*" + sDoc.getString(column1).replaceAll(regex1, toRegex1) + ".*")));	 //处理部分具有格式要求数据
		tDocList.addAll(tDocLists);
	}
	private static void findDataTwo(List<Document> tDocList, String collectionNameTarget, Document sDoc
			, String column1, String regex1, String toRegex1
			, String column2, String regex2, String toRegex2) {
		if(sDoc.containsKey(column1) && sDoc.containsKey(column2)){
				List<Document> tDocLists  = MongodbUtil.find(collectionNameTarget, new Document()
						.append(column1, Pattern.compile(".*" + sDoc.getString(column1).replaceAll(regex1, toRegex1)+ ".*")) //处理部分具有格式要求数据
						.append(column2, Pattern.compile(".*" + sDoc.getString(column2).replaceAll(regex2, toRegex2) + ".*")));
				tDocList.addAll(tDocLists);
		}
				
	}
	private static void findDataTwo(List<Document> tDocList, String collectionNameTarget, Document sDoc
			,String frormColumn1, String column1, String regex1, String toRegex1, String patternStr1
			, String column2, String regex2, String toRegex2, String patternStr2) {
		List<Document> tDocLists  = MongodbUtil.find(collectionNameTarget, new Document()
					.append(frormColumn1, Pattern.compile(".*" + MatchUtil.find(sDoc.containsKey(column1)?sDoc.getString(column1).replaceAll(regex1, toRegex1):"", patternStr1, "") + ".*")) //处理部分具有格式要求数据
					.append(column2, Pattern.compile(".*" + MatchUtil.find(sDoc.containsKey(column2)?sDoc.get(column2).toString().replaceAll(regex2, toRegex2):"", patternStr2, "") + ".*")));
		tDocList.addAll(tDocLists);
	}
	private static void findDataMore(List<Document> tDocList, String collectionNameTarget, Document sDoc) {
		
			List<Document> tDocLists  = MongodbUtil.find(collectionNameTarget, new Document()
					.append("name", Pattern.compile(".*" + sDoc.getString("name").replaceAll("\\(.*|\\[.*| .*", "") + ".*")) //处理部分具有格式要求数据
					.append("director", Pattern.compile(".*" + sDoc.getString("director").replaceAll("/", " / ") + ".*"))
					.append("year", Pattern.compile(".*" + MatchUtil.find(sDoc.containsKey("year")?sDoc.get("year").toString():"", "\\d{4}", "") + ".*")));		
			tDocList.addAll(tDocLists);
	}
	
	public static void main(String[] args) {
		mergeData("chiq_video_converge", "douban_media_basic_info"
				, "chiq_video_optimization20170117", "HASH_4_DBAN_ENTITY_ID_URL", 8);
	}
}
