package com.d5.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

public class MatchUtil {

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
	 * 提取主机url，匹配的形式：http(s)://(xxx.)xxx.xxx
	 * @param url 地址栏中访问的URL
	 * @param defaultStr 默认的字符串
	 * @return
	 */
	public static String getHostUrl(String url, String defaultStr){
		String resultString = null;
		if(defaultStr != null){
			return defaultStr;
		}
		
		resultString = find(url, "(http|https)://\\w+(\\.\\w+)+", defaultStr);
		
		return resultString;
	}

	/**
	 * 提取主机域名，匹配的形式：(xxx.)xxx.xxx
	 * @param url 地址栏中访问的URL
	 * @param defaultStr 默认的字符串
	 * @return
	 */
	public static String getHost(String url, String defaultStr){
		String resultString = null;
		if(defaultStr != null){
			return defaultStr;
		}
		
		resultString = find(url, "\\w+(\\.\\w+)+", defaultStr);
		
		return resultString;
	}
	
	
	/**
	 * 根据正则表达式，获取url地址中URI部分。即，主机端口之后，参数分隔符之前的内容
	 * 如：http://weibo.com/xiaopapi?from=feed&loc=nickname&is_all=1 的URI为/xiaopapi
	 * @param url 地址栏中访问的URL
	 * @return
	 */
	public static String getHostURI(String url){
		String resultString = null;
		resultString = find(url, "(/\\w+)+\\?", "");
		
		resultString = resultString.replaceAll("\\?", "");
		return resultString;
	}
	
	/**
	 * 根据正则表达式，获取网页源代码中，从javascript中的代码获取页面ID数据
	 * @param url 网页源代码
	 * @return
	 */
	public static String getSinaPageID(String htmlScript){
		String resultString = null;
		resultString = find(htmlScript, "\\$CONFIG\\['page_id'\\]='\\d+';", "");
		
		resultString = resultString.replaceAll("[^\\d]+", "");
		return resultString;
	}
	
	/**
	 * 从网页源代码中获取微博的id
	 * @param htmlScript 网页源代码
	 * @return
	 */
	public static String getId(String htmlScript){
		String str = find(htmlScript, "mid=\\\\\"\\d+\\\\\"", "");
		return str.replaceAll("[^\\d]+", "");
	}
	
	/**
	 * 获取315online特殊url的id
	 * @param str
	 * @return
	 */
	public static String get315OnlineUrlId(String str){
		String resultString= find(str, "&id=\\d+", "");
		return resultString.replace("&id=", "");
	}
	
	/**
	 * 判断字符串是否符合正则表达,匹配则返回true,其他返回false
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @return
	 */
	public static boolean contain(String string, String patternStr){
		try {
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			if (regexMatcher.matches()) {
				return true;
			}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 判断字符串是否符合正则表达,匹配则返回true,其他返回false
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @return
	 */
	public static boolean has(String string, String patternStr){
		try {
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			if (regexMatcher.find()) {
				return true;
			}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 在字符串中查找满足正则表达式的内容,找到则返回所有的,否则返回传入默认字符串.
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @param defaultStr 默认字符串
	 * @return
	 */
	public static String find(String string, String patternStr, String defaultStr){
		if (StringUtils.isBlank(string)) {
			return defaultStr;
		}
		try {
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			if (regexMatcher.find()) {
				return regexMatcher.group();
			}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return defaultStr;
	}
	
	/**
	 * 在字符串中查找满足正则表达式的内容,找到则返回所有的,否则返回传入默认字符串.
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @param defaultStr 默认字符串
	 * @return
	 */
	public static String find(String string, String patternStr, String defaultStr, int groupNum){
		if (StringUtils.isBlank(string)) {
			return defaultStr;
		}
		try {
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			if (regexMatcher.find()) {
				return regexMatcher.group(groupNum);
			}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return defaultStr;
	}
	
	/**
	 * 在字符串中查找满足正则表达式的内容,找到则返回所有的,否则返回传入默认字符串.
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @param defaultStr 默认字符串
	 * @return
	 */
	public static String find(String string, Pattern regex, String defaultStr, int groupNum){
		if (StringUtils.isBlank(string)) {
			return defaultStr;
		}
		try {
			Matcher regexMatcher = regex.matcher(string);
			if (regexMatcher.find()) {
				return regexMatcher.group(groupNum);
			}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return defaultStr;
	}

	/**
	 * 在字符串中查找满足正则表达式的内容,找到则返回所有的,否则返回传入默认字符串.
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @param defaultStr 默认字符串
	 * @param contactStr 连接字符串
	 * @return
	 */
	public static String findAll(String string, String patternStr, String defaultStr, String contactStr){
		if (StringUtils.isBlank(string)) {
			return defaultStr;
		}
		if(contactStr == null){
			contactStr = " ";//默认是空格连接
		}
		try {
			StringBuffer resultStr = new StringBuffer();
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			//if (regexMatcher.find()) {
			int index = 0;
			while (regexMatcher.find()) {
				resultStr.append(index==0?regexMatcher.group():contactStr.concat(regexMatcher.group()));
				index ++;
			}
			return resultStr.toString();
			//}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return defaultStr;
	}
	
	/**
	 * 在字符串中查找满足正则表达式的内容,找到则返回所有的,否则返回传入默认字符串.
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @param defaultStr 默认字符串
	 * @param contactStr 连接字符串
	 * @return
	 */
	public static int findAllCount(String string, String patternStr){
		if (StringUtils.isBlank(string)) {
			return 0;
		}
		int index = 0;
		try {
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			//if (regexMatcher.find()) {
			while (regexMatcher.find()) {
				index ++;
			}
			//}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return index;
	}

	/**
	 * 在字符串中查找正则表达式匹配的子串，并返回List
	 * @param string 被检查字符串
	 * @param patternStr 正则表达式
	 * @return
	 */
	public static List<String> findAll(String string, String patternStr){
		if (StringUtils.isBlank(string)) {
			return new ArrayList<String>();
		}
		try {
			List<String> resultList = new ArrayList<String>();
			Pattern regex = Pattern.compile(patternStr);
			Matcher regexMatcher = regex.matcher(string);
			//if (regexMatcher.find()) {
			while (regexMatcher.find()) {
				resultList.add(regexMatcher.group());
			}
			return resultList;
			//}
		} catch (PatternSyntaxException ex) {
			ex.printStackTrace();
		}
		return new ArrayList<String>();
	}
}

