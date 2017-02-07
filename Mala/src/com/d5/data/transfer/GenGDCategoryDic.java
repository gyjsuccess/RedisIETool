package com.d5.data.transfer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;

import com.d5.util.MongodbIPPortUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GenGDCategoryDic {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONObject jData;
		try {
			jData = JSONObject.fromObject(
					FileUtils.readFileToString(new File("F:\\文档\\广电舆情需求\\category_dic.json"), "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String[] typeNames = {"网页", "微博", "微信"};
		/*
		 * 类型（0-网页、1-微博、2-微信）
			类别（体育，经济…）
			查询地域类型对应的热点（local为地区热点，intra国内，interl为全球热点，根据新闻所在网站是本地网站还是全国性网站来判断）
		 */
		MongodbIPPortUtil mg = MongodbIPPortUtil.getInstance().init("10.9.201.190", 27017, "GDNews", null, null, null, null);
		String collectionName = "site_info_dic_collect";
		String collectionNameCate = "category_dic_collect";
		List<Document> docList = mg.find(collectionName, Filters.in("siteId", 39, 40, 41, 42, 43, 24, 17));
		List<Document> dataList = Lists.newArrayList();
		for (Document doc : docList){
			Document data = new Document();
			data.put("siteKindId", String.valueOf(doc.get("siteKindId"))); //网站分类id
			data.put("siteKindName", doc.getString("siteKindName")); //网站分类名称
			data.put("siteId", String.valueOf(doc.get("siteId"))); //网站id
			data.put("siteName", doc.getString("siteName")); //网站名称
			data.put("siteTypeId", String.valueOf(doc.get("siteTypeId"))); //网站类型Id
			data.put("siteTypeName", doc.getString("siteTypeName")); //网站类型名称
			if(!jData.containsKey(String.valueOf(doc.get("siteId")))){
				continue;
			}
			JSONArray jArr = jData.getJSONArray(String.valueOf(doc.get("siteId")));
			
			for(int index = 0; index<jArr.size(); index++){
				JSONObject jd = jArr.getJSONObject(index);
				data.put("typeId", jd.getString("typeId")); //类型Id
				data.put("typeName", typeNames[Integer.parseInt(data.getString("typeId"))]); //类型名称
				data.put("categoryId", StringUtils.leftPad(data.getString("siteKindId"), 2, "0") +
						StringUtils.leftPad(data.getString("siteId"), 4, "0") + 
						StringUtils.leftPad(data.getString("typeId"), 3, "0") +
						StringUtils.leftPad(String.valueOf(index + 1), 3, "0")); //类别Id
				data.put("categoryName", jd.getString("categoryName")); //类别名称
				data.put("url", jd.getString("url")); //分类的url地址
				dataList.add(SerializationUtils.clone(data));
			}
		}
		mg.insertMany(collectionNameCate, dataList);
	}

}
