/******************************************************************
 *    ProjectName: RedisIETool
 *    FileName: d0001.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月23日 下午5:15:28
 *    Revision:
 *    2017年1月23日 下午5:15:28
 *        - first revision
 *****************************************************************/
package com.d5.demos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * PackageName: com.d5.demos
 * @ClassName d0001
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月23日 下午5:15:28
 */
public class d0001 {
	public static void main(String[] args) {
		String fKeyStr = ",";
		String regex = "(\\w+,)*\\w+";
		System.out.println(subString(fKeyStr, regex));
		fKeyStr = "234,4234,423423,,,";
		System.out.println(subString(fKeyStr, regex));
		fKeyStr = ",234,,4234,423423,,";
		System.out.println(match(fKeyStr,"\\w,,\\w"));
		System.out.println(subString(fKeyStr, regex));
	}
	
	public static boolean match(String source, String regex){
		boolean foundMatch = false;
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher regexMatcher = pattern.matcher(source);
			foundMatch = regexMatcher.matches();
		} catch (PatternSyntaxException e) {
		}
		return foundMatch;
	}
	
	public static boolean find(String source, String regex){
		boolean foundMatch = false;
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher regexMatcher = pattern.matcher(source);
			foundMatch = regexMatcher.find();
		} catch (PatternSyntaxException e) {
		}
		return foundMatch;
	}
	
	public static String subString(String source, String regex){
		String resultString = null;
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher regexMatcher = pattern.matcher(source);
			if (regexMatcher.find()) {
				resultString = regexMatcher.group();
			} 
		} catch (PatternSyntaxException e) {
		}
		return resultString;
	}
	
	public static String subString(String source, String regex, int gId){
		String resultString = null;
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher regexMatcher = pattern.matcher(source);
			if (regexMatcher.find()) {
				resultString = regexMatcher.group(gId);
			} 
		} catch (PatternSyntaxException e) {
		}
		return resultString;
	}
}
