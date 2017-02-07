package com.d5.data.transfer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.tool.RMdbConf;
import com.d5.util.MongodbIPPortUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;

import net.sf.json.JSONObject;

public class RemoteMongoDataSync {
	private static Logger log = LoggerFactory.getLogger(RemoteMongoDataSync.class);
	
	public static void testConnection(String collectionName, File file){
		String message = "失败！";
		try {
			if(RMdbConf.confInfoIsCorrect){
				MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init(
						RMdbConf.rIp, RMdbConf.rPort, RMdbConf.rDbname, RMdbConf.rNeedAuth,
						RMdbConf.rAuthType, RMdbConf.rAuthUser, RMdbConf.rAuthPasswd);
				List<Document> docList = mongo.findAllDocuments(collectionName);
				if(docList.size()>0){
					message = "成功！" + docList.size();
				}
			}
		} catch (Exception e) {
			message += e.getMessage();
			log.info("ip is :{} ----port is :{} ----{}", RMdbConf.rIp, RMdbConf.rPort, e);
		}
		log.info("Test connection {}", message);
	}
	
	/**
	 * 
	 * @param collectionName
	 * @param file
	 */
	public static void dump(String collectionName, File file){
		if(RMdbConf.confInfoIsCorrect){
			MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init(
					RMdbConf.rIp, RMdbConf.rPort, RMdbConf.rDbname, RMdbConf.rNeedAuth,
					RMdbConf.rAuthType, RMdbConf.rAuthUser, RMdbConf.rAuthPasswd);
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
				log.info("ip is :{} ----port is :{} ----{}", RMdbConf.rIp, RMdbConf.rPort, e);
			}
		}
	}
	
	/**
	 * 
	 * @param collectionName
	 * @param file
	 */
	public static void importData(String collectionName, File file){
		if(RMdbConf.confInfoIsCorrect){
			List<Document> docList = Lists.newArrayList();
			
			MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init(
					RMdbConf.rIp, RMdbConf.rPort, RMdbConf.rDbname, RMdbConf.rNeedAuth,
					RMdbConf.rAuthType, RMdbConf.rAuthUser, RMdbConf.rAuthPasswd);
			try {
				List<String> jsonStrList = FileUtils.readLines(file);
				for (String jsonStr : jsonStrList) {
					Document doc = new Document();
					doc.putAll(JSONObject.fromObject(jsonStr));
					docList.add(doc);
				}
				
				mongo.insertMany(collectionName, docList);
			} catch (IOException e) {
				log.info("ip is :{} ----port is :{} ----{}", RMdbConf.rIp, RMdbConf.rPort, e);
			}
		}
	}
	
	public static void syncRemoteData(String collectName, String tCollectName, Bson query, int limitNum){
		if(StringUtils.isBlank(collectName) || StringUtils.isBlank(tCollectName)){
			log.error("collect name is error.");
			return;
		}
		if(RMdbConf.confInfoIsCorrect){
			List<Document> rDocList = Lists.newArrayList();
			log.info("init mongodb connect start");
			MongodbIPPortUtil rMongo = MongodbIPPortUtil.getInstance().init(
					RMdbConf.rIp, RMdbConf.rPort, RMdbConf.rDbname, RMdbConf.rNeedAuth,
					RMdbConf.rAuthType, RMdbConf.rAuthUser, RMdbConf.rAuthPasswd);
			log.info("init mongodb connect end");
			//获取数据
			Long lTotal = rMongo.count(collectName, query);
			log.info("get 'lTotal' end");
			int total = lTotal.intValue();
			int skipNum = 0;
			while(skipNum <= total){
				rDocList.addAll(rMongo.find(collectName, query, skipNum, limitNum));
				skipNum += limitNum;
				if(skipNum >= total){
					break;
				}
			}
			
			log.info("sync data, total is :{}", rDocList.size());
			
			//清理临时表
			if(StringUtils.isNotBlank(Constants.SYNC_CLEAR_COLL_NAMES)){
				for(String cName : Constants.SYNC_CLEAR_COLL_NAMES.split("\\|")){
					MongodbUtil.deleteMany(cName, new Document());
				}
			}
			//写入数据
			MongodbUtil.insertMany(tCollectName, rDocList);
			log.info("'写入数据' end");
		}
	}
}
