package com.d5.tool;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.d5.util.MatchUtil;

public class CleanIssue {
private static Logger log = LoggerFactory.getLogger(CleanIssue.class);
   /**
    * 日期清理格式规范化
    * */
	public static String cleanDateTime(String issue){
		try{
				String timeData = issue;
				if(timeData.matches(".*[a-z].*")){
					timeData = engMonth(timeData);
				}
						
				timeData = getFormatDate(timeData);
				DateTime date = null;
				try {
					if(timeData.isEmpty()){
						date = DateTime.parse(MatchUtil.find(issue, "\\d{4}", ""));
					}else{
						date = DateTime.parse(MatchUtil.find(timeData, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}-\\d{1,2}|^\\d{4}$", ""));
					}
				} catch (Exception e) {
					try {
						date = DateTime.parse(MatchUtil.find(issue, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}", ""));
					} catch (Exception e2) {
						try {
							date = DateTime.parse(MatchUtil.find(issue, "\\d{4}", ""));
						} catch (Exception e3) {
							log.error("{}"+e+"issueis :{} "+issue);
						}
					}
				}
				timeData = date.toString("yyyy-MM-dd");
				return timeData;
		}catch(Exception e){
			log.error("{}"+e+" issue is {}"+issue);
		}
		return "";
	}
	/**
	 * 英文月份转数字
	 * */
	private static String engMonth(String engTime){
		String[] strEnMonth = {"January", "February", "March", "April"
							, "May", "June", "July", "Aguest"
							, "September", "October", "November", "December"
							, "Jan",  "Feb", "Mar", "Apr"
							, "May", "Jun", "Jul", "Aug"
							, "Sep", "Oct", "Nov", "Dec"};
		String[] strChMonth = {"1", "2", "3", "4"
							, "5", "6", "7", "8"
							, "9", "10", "11", "12"
							, "1", "2", "3", "4"
							, "5", "6", "7", "8"
							, "9", "10", "11", "12"};
		for(int i = 0 ; i < strEnMonth.length ; i ++){
			if(engTime.matches(".*"+strEnMonth[i]+".*")){
				engTime = engTime.replaceAll(strEnMonth[i], strChMonth[i]);
			}
		}
		return engTime;
	}
	/**
	 * 统一时间分隔符
	 * */
	private static String getFormatDate(String date){
		
		String data1 = date.replaceAll("[^\\d+]", " ").trim();
		
		if(data1.matches("^\\d{4}$")){
			date = data1;
		}else if(data1.matches("\\d{1,2}\\s+\\d{1,2}\\s+\\d{4}|\\d{1,2}\\s+\\d{4}")){
			date = data1.replaceAll("\\s+", "-");
			date = formatDate(date);
		}else{
			date = data1.replaceAll("\\s+", "-");
			date = MatchUtil.find(date, "\\d{4}-\\d{1,2}-\\d{1,2}|\\d{4}-\\d{1,2}", "");
		}
		
		return date;
	}
	/**
	 * 英文书写格式转中文格式
	 * */
	private static String formatDate (String date){
		String fromFormat = "MM-dd-yyyy";
		String toFormat = "yyyy-MM-dd";
		DateFormat d1 = new SimpleDateFormat(toFormat); 
		DateFormat d2 = new SimpleDateFormat(fromFormat); 
		try {
			date = d1.format(d2.parse(date));
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
}
