package com.proxy.ip.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class RegexUtils {
	public static String findStringByRegex(String sourceString,String regexRule){
		Pattern pattern=Pattern.compile(regexRule);
		Matcher matcher = pattern.matcher(sourceString);
		while(matcher.find()){
			return matcher.group();			
		}
		return null;
	}
	public static List<String> patternStringList(String regex,String matchString){
		List<String> result = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(matchString);
		while(matcher.find()){
			result.add(matcher.group());
			//System.out.println(matcher.group());
		}
		return result;
	}
	public static String patternString(String regex,String matchString){
		String result = null;
		Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(matchString);
		while(matcher.find()){
			result=matcher.group();
			//System.out.println(matcher.group().replaceAll("&id=", "").replaceAll("&skuId", ""));
		}
		return result;
	}
	
	/**
	 * 匹配出字符串中的数字
	 * @param content 待匹配的字符
	 * @return 匹配完成的结果
	 */
	public static String numberMatch(String content) {
		String num = "";
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(content);
		if (m.find()) {num = m.group();}		
		return num;
	}
	
	/**
	 * 从字段中匹配出汉字
	 * @param chineseStr  包含汉字的String
	 * @return 匹配结果
	 */
	public static String chineseMatch(String chineseStr) {
		String chinese = "";
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher matcher = pattern.matcher(chineseStr);
		while (matcher.find()) {
				chinese += matcher.group();
		}
		return chinese;
	}
	
	/**
	 * 匹配出字符串中的时间（精确到秒）
	 * @param content 待匹配的字符
	 * @return 匹配完成的结果
	 */
	public static String timeMatch(String content) {
		String time = "";
		Pattern p = Pattern.compile("\\d{4}-\\d*-\\d* \\d*:\\d*:\\d*");
		Matcher m = p.matcher(content);
		if (m.find()) {time = m.group();}		
		return time;
	}
	
	/**
	 * 匹配出字符串中的时间（精确到天）
	 * @param content 待匹配的字符
	 * @return 匹配完成的结果
	 */
	public static String timeMatchOnlyDay(String content) {
		String time = "";
		Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		Matcher m = p.matcher(content);
		if (m.find()) {time = m.group();}		
		return time;
	}
	
	/**
	 * 从url地址中匹配出特定的urlId
	 * @param url 待匹配的url
	 * @return urlId
	 */
	public static String doUrlIdMatch(String url) {
		String urlId = "";
		Pattern pattern = Pattern.compile("(\\d+\\-)+\\d+");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {urlId = matcher.group();}
		return urlId;
	}
	
	/**
	 * 从字段中匹配出年龄，如果存在形如0-10的年龄范围的，直接返回，要是不存在范围，就匹配返回单个数字年龄
	 * @param age  包含年龄数据的String
	 * @return 匹配结果
	 */
	public static String ageMatch(String age)
	{
		String ageStr="";
		Pattern patternMixed = Pattern.compile("(\\d+\\-\\d+)+");//匹配年龄范围，例子：0-10
		Pattern patternSingle = Pattern.compile("\\d+");//匹配年龄范围，例子：20
		Matcher matcherMixed = patternMixed.matcher(age);
		if (matcherMixed.find()) {ageStr = matcherMixed.group();}
		if(StringUtils.isBlank(ageStr)){
			Matcher matcherSingle = patternSingle.matcher(age);
			if (matcherSingle.find()) {ageStr = matcherSingle.group();}
		}
		
		return ageStr;	
	}
}
