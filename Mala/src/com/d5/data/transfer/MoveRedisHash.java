package com.d5.data.transfer;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.util.JedisIPPortUtil;
import com.d5.util.MongodbIPPortUtil;

public class MoveRedisHash {
	private static Logger log = LoggerFactory.getLogger(MoveRedisHash.class);
	public static void main(String[] args) {
		//one2Mutiple();
		//one2one();
		//dataIntoHash();
		//modifyHashValue();
		removeMutipleRows();
	}
	
	public static void one2Mutiple(){
		String sKeyName = "IS_REPEAT_HASH_4_MDEIA";
		JedisIPPortUtil sJedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379, null);
		JedisIPPortUtil tJedis = sJedis; //JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		Map<String, String> map = sJedis.hgetAll(sKeyName, 5);
		
		
		String tKeyName = null;
		for(Map.Entry<String, String> en : map.entrySet()){
			String field = en.getKey();
			if(field.matches("^https.*subject/\\d+/$")){
				tKeyName = "HASH_4_DBAN_ENTITY_REPEAT";
			}
			if(field.matches("^https.*celebrity/\\d+/$")){
				tKeyName = "HASH_4_DBAN_CELEBRITY_REPEAT";
			}
			if(field.matches("^https.*review/\\d+/$")){
				tKeyName = "HASH_4_DBAN_REVIEW_REPEAT";
			}
			log.info("tKeyName is : {},field is : {},en.getValue() is : {}", tKeyName, field, en.getValue());
			if(tKeyName != null){
				//if(!tJedis.hexists(tKeyName, field, 5)){
					tJedis.hset(tKeyName, field, "0", 5);
				//}
				tKeyName = null;
			}
		}
	}
	
	/**
	 * 一对一，同名
	 * @param tKeyName
	 */
	public static void one2one(String tKeyName){
		JedisIPPortUtil tJedis = JedisIPPortUtil.getInstance().init("127.0.0.1", 6380, null);
		JedisIPPortUtil sJedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379, null);
		Map<String, String> mapAll = sJedis.hgetAll(tKeyName, 5);
		for(Map.Entry<String, String> en : mapAll.entrySet()){
			tJedis.hset(tKeyName, en.getKey(), en.getValue(), 8);
		}
	}
	
	public static void dataIntoHash(){
		MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init("10.9.201.190", 27017, "douban", null, null, null, null);
		//JedisIPPortUtil jedisLocal = JedisIPPortUtil.getInstance().init("127.0.0.1", 6380);
		JedisIPPortUtil jedisServer = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379, null);
		String collName = "douban_media_basic_info";
		List<Document> datas = mongo.find(collName, new Document(),
				new Document().append("entityUrl", 1).append("entityId", 1).append("_id", 0));
		String keyName = "HASH_4_DOUBAN_ENTITY_ID";
		for(Document dt : datas){
			String field = dt.getString("entityUrl");
			String value = dt.getString("entityId");
			log.info("tKeyName is : {},field is : {},value is : {}", keyName, field, value);
			//jedisLocal.hset(keyName, field, value, 8);
			if(!jedisServer.hexists(keyName, field, 5)){
				jedisServer.hset(keyName, field, value, 5);
			}
			//System.out.println(dt);
		}
	}
	
	public static void modifyHashValue(){
		String keyName = "HASH_4_DBAN_ENTITY_REPEAT";
		JedisIPPortUtil jedisServer = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379, null);
		Map<String, String> map = jedisServer.hgetAll(keyName, 5);
		for(Map.Entry<String, String> en : map.entrySet()){
			jedisServer.hset(keyName, en.getKey(), "1", 5);
		}
	}
	
	public static void removeMutipleRows(){
		MongodbIPPortUtil mongo = MongodbIPPortUtil.getInstance().init("10.9.201.190", 27017, "douban20161122", null, null, null, null);
		String collName = "douban_media_basic_info";
		List<Document> datas = mongo.find(collName, new Document(),
				new Document().append("entityUrl", 1));
		for(Document dt : datas){
			String entityUrl = dt.getString("entityUrl");
			List<Document> urlObjList = mongo.find(collName, new Document().append("entityUrl", entityUrl));
			if(urlObjList.size() > 1){
				for(Document urlObj : urlObjList){
					if("0".equals(urlObj.getString("isNew"))){
						mongo.deleteOne(collName, new Document().append("_id", urlObj.get("_id")));
					}
				}
			}
		}
	}
}
