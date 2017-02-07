package com.d5.demos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.d5.util.MongodbIPPortUtil;
import com.google.common.collect.Lists;

import net.sf.json.JSONObject;

public class Demo007 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String collectionName = "c_163_basic_info";
		String filePath = "d:\\c_163_basic_info.txt";
		filePath = "d:\\c_163_basic_info.txt";
		File file = new File(filePath);
		RemoteMongoDataDump.dump(collectionName, file);
//		RemoteMongoDataDump.importData(collectionName, file);
	}
}

class RemoteMongoDataDump {
	static void dump(String collectionName, File file){
		MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init("127.0.0.1", 27017, "db_4_163", null, null, null, null);
		List<Document> docList = mongo.findAllDocuments(collectionName);
		try {
			List<String> jsonStringList = Lists.newArrayList();
			for (Document doc : docList) {
				JSONObject json = new JSONObject();
				json.putAll(doc);
				json.remove("_id");
				jsonStringList.add(json.toString());
			}
			FileUtils.writeLines(file, jsonStringList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void importData(String collectionName, File file){
		MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init("127.0.0.1", 27017, "EPGInfo", null, null, null, null);
		List<Document> docList = Lists.newArrayList();

		try {
			List<String> jsonStrList = FileUtils.readLines(file);
			for (String jsonStr : jsonStrList) {
				Document doc = new Document();
				doc.putAll(JSONObject.fromObject(jsonStr));
				docList.add(doc);
			}
			
			mongo.insertMany(collectionName, docList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
