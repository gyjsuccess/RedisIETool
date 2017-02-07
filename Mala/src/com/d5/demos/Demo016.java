/******************************************************************
 *    ProjectName: Mala
 *    FileName: Demo016.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月20日 下午4:57:10
 *    Revision:
 *    2017年1月20日 下午4:57:10
 *        - first revision
 *****************************************************************/
package com.d5.demos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.util.MongodbIPPortUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;

/**
 * PackageName: com.d5.demos
 * 
 * @ClassName Demo016
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月20日 下午4:57:10
 */
public class Demo016 {
	private static Logger log = LoggerFactory.getLogger(Demo016.class);
	public static void main(String[] args) {
		MongodbIPPortUtil mUtil = MongodbIPPortUtil.getInstance()
				.init("106.14.62.40", 9666, "guangdian", true, "CRCredential", "gduser_dev10", "Passw0rd&234$");
		List<Document> sDocListAll = Lists.newArrayList();
		List<Character> garbledCharList = Lists.newArrayList();
		String collectionName = "c_gd_news_basicinfo";
		int limitNum = 2000; //每次取出的数据量
		Document query = new Document();
		try{
			Long lTotal = mUtil.count(collectionName, query);
			int total = lTotal.intValue();
			int skipNum = 0;
			while(skipNum <= total ){
				sDocListAll.addAll(mUtil.find(collectionName, query, skipNum, limitNum));
				for(Document doc : sDocListAll){
					getAllGarbledCode(doc.getString("content"), garbledCharList);
					if(garbledCharList.size() > 0){
						log.info("{}===={}", doc.getString("entityUrl"), garbledCharList);
						garbledCharList.clear();
					}
				}
				skipNum += limitNum;
				sDocListAll.clear();
			}
		}catch(Exception e){
			e.printStackTrace();
		} finally {
		}
	}

	public static boolean isGarbledCode(String strName) {
		if (null == strName || 0 == strName.trim().length()) {
			return true;
		}

		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {

				if (!isChinese(c)) {
					count = count + 1;
				}
			}
		}
		//float result = count / chLength;
		//if (result > 0.4) {
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void getAllGarbledCode(String strName, List<Character> garbledCharList) {
		if (null == strName || 0 == strName.trim().length()) {
			return;
		}

		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {

				if (!isChinese(c)) {
					garbledCharList.add(c);
				}
			}
		}
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
}
