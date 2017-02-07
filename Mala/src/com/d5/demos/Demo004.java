package com.d5.demos;

import java.util.Map;

import org.bson.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.crawler.Request;
import com.d5.util.CommonUtil;

public class Demo004 {
	public static void main(String[] args) {
		JSONObject jsonObj = JSON.parseObject("{    \"siteId\": 27,    \"warnningLevel\": 2,    \"warnningDate\": \"2016-10-09 01:00:43\",    \"entityUrl\": \"http://bbs.changhong.com/thread-88026-1-1.html\",    \"attention\": 3,    \"categoryId\": \"030013001005002\",    \"id\": \"b8ca3aa97986601506689133682278\",    \"planExcuteTime\": \"2016-10-11 14:44:00\",    \"brandId\": 1}");
		JSONObject jsonObj_ = (JSONObject) jsonObj.clone();
		
		JSONObject siteInfo = new JSONObject();
		siteInfo.put("siteId", "27");
		siteInfo.put("siteName", "麻辣社区");
		siteInfo.put("siteKindId", "3");
		siteInfo.put("siteKindName", "论坛贴吧");
		
		Document data = new Document();
		Map<String, String> fieldsMap = null;
		CommonUtil.fillDataIntoJsonObject(siteInfo, fieldsMap , data);
    	
		Request request = new Request();
		request.setUrl(jsonObj_.getString("entityUrl"));
		CommonUtil.addInitData2Request(request, siteInfo, jsonObj_);
		
		System.out.println(request.toString());
	}
}
