/******************************************************************
 *    ProjectName: Mala
 *    FileName: DataDeal.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月18日 上午11:33:31
 *    Revision:
 *    2017年1月18日 上午11:33:31
 *        - first revision
 *****************************************************************/
package com.d5.data.transfer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.tool.CleanIssue;
import com.d5.tool.HeatCalculate;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;

/**
 * PackageName: com.d5.data.transfer
 * @ClassName DataDeal
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月18日 上午11:33:31
 */
public class DataDeal {
	private static Logger log = LoggerFactory.getLogger(DataDeal.class);
	/**
	 * 
	 * MethodName：dealData
	 * @author: Administrator
	 * @Date: 2017年1月18日 上午11:33:45
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param doc
	 * @param heat
	 */
	public static void dealData(Document doc, long heat){
		String tencentId = "";
		if(doc.containsKey("pp_tencent")){
			Document _doc = doc.get("pp_tencent", Document.class);
			if(_doc != null && _doc.containsKey("tencentId")){
				tencentId = _doc.getString("tencentId");
			}
		}
		doc.append("tencentId",tencentId); 
		doc.append("heat", heat);
		doc.remove("d_issue");
		doc.remove("ratingPeopleNum");
		doc.remove("pp_tencent");
	}
	
	/**
	 * 
	 * MethodName：putIntegerExists
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:32:33
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sColName
	 * @param tColName
	 * @param sDoc
	 * @param tDoc
	 * @param regex
	 * @param replaceStr
	 */
	public static void putIntegerExists(String sColName, String tColName, Document sDoc, Document tDoc, String regex, String replaceStr) {
		if(tDoc.containsKey(tColName)){
			try {
				sDoc.put(sColName, Integer.parseInt(tDoc.getString(tColName).replaceAll(regex, "")));  
			} catch(Exception e){
				log.error("tColName '{}' column error. entiryUrl is :{},cause is :", tColName, tDoc.getString("entityUrl"), e);
			}
		}
	}
	/**
	 * 
	 * MethodName：putStringExists
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:32:37
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sColName
	 * @param tColName
	 * @param sDoc
	 * @param tDoc
	 */
	public static void putStringExists(String sColName, String tColName, Document sDoc, Document tDoc) {
		if(tDoc.containsKey(tColName)){
			sDoc.put(sColName, tDoc.getString(tColName));
		}
	}
	/**
	 * 
	 * MethodName：putStringExists
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:32:42
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sColName
	 * @param tColName
	 * @param sDoc
	 * @param tDoc
	 * @param regex
	 */
	public static void putStringExists(String sColName, String tColName, Document sDoc, Document tDoc, String regex) {
		if(tDoc.containsKey(tColName)){
			sDoc.put(sColName, MatchUtil.find(tDoc.getString(tColName), regex, ""));
		}
	}
	
	/**
	 * 
	 * MethodName：putStringExists
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:32:45
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sColName
	 * @param tColName
	 * @param sDoc
	 * @param tDoc
	 * @param regex
	 * @param replaceStr
	 */
	public static void putStringExists(String sColName, String tColName, Document sDoc, Document tDoc,
			String regex, String replaceStr) {
		if(tDoc.containsKey(tColName)){
			sDoc.put(sColName, tDoc.getString(tColName).replaceAll(regex, replaceStr));
		}
	}
	
	/**
	 * 
	 * MethodName：putIntegerExists
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午1:33:53
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param sColName
	 * @param tColName
	 * @param sDoc
	 * @param tDoc
	 * @param regex
	 */
	public static void putIntegerExists(String sColName, String tColName, Document sDoc, Document tDoc, String regex){
		if(tDoc.containsKey(tColName)){
			try {
				sDoc.put(sColName, Integer.parseInt(MatchUtil.find(tDoc.get(tColName).toString(), regex, "", 0)));  
			} catch(Exception e){
				log.error("tColName '{}' column error. entiryUrl is :{},cause is :", tColName, tDoc.getString("entityUrl"), e);
			}
		}
	}
	
	/**
	 * MethodName：getDigitalInfo
	 * @author: Administrator
	 * @Date: 2017年1月17日 下午4:57:20
	 * @Description 从一个数据集里提取部分数值信息到新的数据集里
	 * @param doc
	 * @return
	 */
	public static void getDigitalInfo(Document doc, Document docDigital) {
		DataDeal.putIntegerExists("ratingPeopleNum", "ratingPeopleNum", docDigital, doc, "\\d+"); //评论人数
		DataDeal.putIntegerExists("collectionsNum", "collectionsNum", docDigital, doc, "\\d+");  //看过的人数
		DataDeal.putIntegerExists("wishesNum", "wishesNum", docDigital, doc, "\\d+");  //想看的人
		DataDeal.putIntegerExists("commentCount", "commentCount", docDigital, doc, "\\d+");  //影评数
		docDigital.append("ratingNum", doc.getString("ratingNum"));  //评分
		
		docDigital.append("entityId", doc.getString("entityId"));
		docDigital.append("entityUrl", doc.getString("entityUrl"));
		docDigital.append("getDateTime", doc.getString("getDateTime"));
		docDigital.append("name", doc.getString("name"));
		docDigital.append("issue", doc.getString("issue"));
	}
	
	/**
	 * MethodName：calculateHeatValue
	 * @author: Administrator
	 * @Date: 2017年1月17日 下午5:36:11
	 * @Description 调用热度算法，计算出热度值
	 * @param docDigital
	 * @return
	 */
	public static long calculateHeatValue(Document docDigital) {
		List<Document> digitalDocList = MongodbUtil.find(Constants.COLL_NAME_4_DIGITAL,
				new Document().append("entityId", docDigital.getString("entityId"))
							.append("entityUrl", docDigital.getString("entityUrl")));
		//计算上映日期和现在时间的相差天数
		String issueDate = docDigital.getString("issue");
		//日期格式化 yyyy-MM-dd HH:mm:ss
		issueDate = CleanIssue.cleanDateTime(issueDate);
		int days = calculateDays(issueDate);
		if(digitalDocList.size() > 2){
			log.error("热度计算出错，数值数据大于2条。url是：{}", docDigital.getString("entityUrl"));
			return 0;
		} else if(digitalDocList.size() == 2){
			long numberOld = getStr2Long(digitalDocList.get(0), "ratingPeopleNum");
			long numberNew = getStr2Long(digitalDocList.get(1), "ratingPeopleNum");
			double heatValue = HeatCalculate.calHeat(numberOld, numberNew, days, 90);
			return Math.round(heatValue);
		} else if(digitalDocList.size() == 1){
			if(days < 28){
				long ratingPeopleNum = getStr2Long(docDigital, "ratingPeopleNum");
				long wishesNum = docDigital.getLong("wishesNum");
				double heatValue = HeatCalculate.calHeatNew(ratingPeopleNum, wishesNum, days, 90);
				return Math.round(heatValue);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * MethodName：getStr2XDataType
	 * @author: Administrator
	 * @Date: 2017年2月7日 上午10:55:39
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param long1
	 * @param class1
	 * @return
	 */
	private static long getStr2Long(Document doc, String key) {
		if(doc.containsKey(key)){
			String kValue = String.valueOf(doc.get(key));
			if(StringUtils.isNumeric(kValue)){
				return Long.parseLong(kValue);
			}
		}
		return 0;
	}

	/**
	 * MethodName：calculateDays
	 * @author: Administrator
	 * @Date: 2017年1月19日 上午10:10:10
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param issueDate
	 * @return
	 */
	private static int calculateDays(String issueDate) {
		DateTime issueD = DateTime.parse(cleanDateTime(issueDate));
		DateTime tD = DateTime.now();
		return Days.daysBetween(issueD, tD).getDays();
	}

	/**
	 * MethodName：writeWeightQueue
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午3:15:39
	 * @Description 写明星作品权重计算队列
	 * @param doc
	 */
	public static void writeWeightQueue(Document doc) {
		Constants.dataRedisService.addInfo2List(
				Constants.REDIS_INDEX_4_L, Constants.KEY_WEIGHT_QUEUE, doc.toJson());
	}
	
	/**
	 * 日期清理格式规范化
	 */
	public static String cleanDateTime(String issue) {
		try {
			String timeData = issue;
			if (timeData.matches(".*[a-z]+.*")) {
				timeData = engMonth(timeData);
			}

			timeData = getFormatDate(timeData);
			DateTime date = null;
			try {
				if (timeData.isEmpty()) {
					date = DateTime.parse(MatchUtil.find(issue, "\\d{4}", ""));
				} else {
					date = DateTime
							.parse(MatchUtil.find(timeData, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}-\\d{1,2}|^\\d{4}$", ""));
				}
			} catch (Exception e) {
				try {
					date = DateTime.parse(MatchUtil.find(issue, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}", ""));
				} catch (Exception e2) {
					try {
						date = DateTime.parse(MatchUtil.find(issue, "\\d{4}", ""));
					} catch (Exception e3) {
						log.error("{}" + e + "issueis :{} " + issue);
					}
				}
			}
			timeData = date.toString("yyyy-MM-dd");
			return timeData;
		} catch (Exception e) {
			log.error("{}" + e + " issue is {}" + issue);
		}
		return "";
	}

	/**
	 * 英文月份转数字
	 */
	private static String engMonth(String engTime) {
		String[] strEnMonth = { "January", "February", "March", "April", "May", "June", "July", "Aguest", "September",
				"October", "November", "December", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
				"Nov", "Dec" };
		String[] strChMonth = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "1", "2", "3", "4", "5",
				"6", "7", "8", "9", "10", "11", "12" };
		for (int i = 0; i < strEnMonth.length; i++) {
			if (engTime.matches(".*" + strEnMonth[i] + ".*")) {
				engTime = engTime.replaceAll(strEnMonth[i], strChMonth[i]);
			}
		}
		return engTime;
	}

	/**
	 * 统一时间分隔符
	 */
	private static String getFormatDate(String date) {

		String data1 = date.replaceAll("[^\\d+]", " ").trim();

		if (data1.matches("^\\d{4}$")) {
			date = data1;
		} else if (data1.matches("\\d{1,2}\\s+\\d{1,2}\\s+\\d{4}|\\d{1,2}\\s+\\d{4}")) {
			date = data1.replaceAll("\\s+", "-");
			date = formatDate(date);
		} else {
			date = data1.replaceAll("\\s+", "-");
			date = MatchUtil.find(date, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}-\\d{1,2}", "");
		}

		return date;
	}

	/**
	 * 英文书写格式转中文格式
	 */
	private static String formatDate(String date) {
		return DateTime.parse(date, DateTimeFormat.forPattern("MM-dd-yyyy")).toString("yyyy-MM-dd");
	}
	
	/**
	 * MethodName：calDaysSpan
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午5:52:24
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param days
	 * @param dSapn
	 * @return
	 */
	public static double rondDouble(double d, int scale) {
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * MethodName：fillList
	 * @author: Administrator
	 * @Date: 2017年1月20日 下午4:24:42
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param celebrityMWList
	 * @param wMap
	 * @param doc
	 */
	public static void fillList(List<Document> celebrityMWList, Map<String, Double> wMap, Document doc) {
		for(Map.Entry<String, Double> en : wMap.entrySet()){
			celebrityMWList.add(new Document().append("entityId", doc.getString("entityId"))
					.append("entityUrl", doc.getString("entityUrl"))
					.append("name", doc.getString("name"))
					.append("celebrityName", en.getKey())
					.append("weight", en.getValue()));
		}
		
	}
}
