package com.d5.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;

/**
 * mongodb driver version: 3.2.2
 * 
 * @author Administrator 
 * start mongodb windows: 
 * cd D:\Program Files\mongoDB\Server\3.2\bin\
 * mongod.exe --port 27017 --dbpath "D:\\Program Files\\mongoDB\\Server\\3.2\\dbdata"
 * 
 * 将MongoDB服务器作为Windows服务运行
 * mongod.exe --bind_ip yourIPadress --logpath "C:\data\dbConf\mongodb.log" --logappend --dbpath "C:\data\db" --port yourPortNumber --serviceName "YourServiceName" --serviceDisplayName "YourServiceName" --install
	--bind_ip	绑定服务IP，若绑定127.0.0.1，则只能本机访问，不指定默认本地所有IP
	--logpath	定MongoDB日志文件，注意是指定文件不是目录
	--logappend	使用追加的方式写日志
	--dbpath	指定数据库路径
	--port	指定服务端口号，默认端口27017
	--serviceName	指定服务名称
	--serviceDisplayName	指定服务名称，有多个mongodb服务时执行。
	--install	指定作为一个Windows服务安装。
 */
public class MongodbUtil {
	private static Logger log = Logger.getLogger(MongodbUtil.class);
	private static MongoDatabase db;
	private static ConfigurationUtil config;
	private static boolean needAuthenticate;
	private static String authenticateType;
	private static String dbName;
	private static MongoClientOptions options;
	
	static {
		config = ConfigurationUtil.getInstance(
				CommonUtil.getHome() + System.getProperty("file.separator") + "conf"
						+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "conf_mongodb.ini");
		needAuthenticate = config.getBoolean("mongo.needAuthenticate", false);
		authenticateType = config.getString("mongo.authenticate.type", "CRCredential");
		dbName = config.getString("mongo.DBName", "test");
		MongoClientOptions.Builder buide = new MongoClientOptions.Builder();
        buide.connectionsPerHost(config.getInt("mongo.common.connectionsPerHost", 100));// 与目标数据库可以建立的最大链接数
        buide.connectTimeout(config.getInt("mongo.common.connectTimeout", 1000 * 60 * 20));// 与数据库建立链接的超时时间
        buide.maxWaitTime(config.getInt("mongo.common.maxWaitTime", 100 * 60 * 5));// 一个线程成功获取到一个可用数据库之前的最大等待时间
        buide.threadsAllowedToBlockForConnectionMultiplier(config.getInt("mongo.common.threadsAllowedToBlockForConnectionMultiplier", 100));
        buide.maxConnectionIdleTime(config.getInt("mongo.common.maxConnectionIdleTime", 0));
        buide.maxConnectionLifeTime(config.getInt("mongo.common.maxConnectionLifeTime", 0));
        buide.socketTimeout(config.getInt("mongo.common.socketTimeout", 0));
        buide.socketKeepAlive(config.getBoolean("mongo.common.socketKeepAlive", true));
        options = buide.build();
		if(needAuthenticate){
			getDBWithCred();
		}else{
			getDB();
		}
	}

	public static synchronized MongoDatabase getDB() {
		if (db == null) {
			try {
				// 连接到 mongodb 服务
				log.debug("hostIp :" + config.getString("mongo.hostIp", ""));
				ServerAddress serverAddress = new ServerAddress(
						config.getString("mongo.hostIp", "127.0.0.1"),
						config.getInt("mongo.hostPort", 27017));
	            List<ServerAddress> addrs = new ArrayList<ServerAddress>();  
	            addrs.add(serverAddress); 
				MongoClient mongoClient = new MongoClient(addrs, options);

				// 连接到数据库
				db = mongoClient.getDatabase(dbName);
				log.info("Connect to database successfully");

			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}
		return db;
	}
	
	public static synchronized MongoDatabase getDBWithCred() {
		if (db == null) {
			try {
				//连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址  
	            //ServerAddress()两个参数分别为 服务器地址 和 端口  
				ServerAddress serverAddress = new ServerAddress(
						config.getString("mongo.hostIp", "127.0.0.1"),
						config.getInt("mongo.hostPort", 27017));
	            List<ServerAddress> addrs = new ArrayList<ServerAddress>();  
	            addrs.add(serverAddress); 
	              
	            //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码  
	            MongoCredential credential = null;
				if("CRCredential".equals(authenticateType)){	
					credential = MongoCredential.createMongoCRCredential(
						config.getString("mongo.user", "root"), dbName,
						config.getString("mongo.password", "mongodata").toCharArray()); 
				}
				
				if("Sha1".equals(authenticateType)){	
					credential = MongoCredential.createScramSha1Credential(
						config.getString("mongo.user", "root"), dbName,
						config.getString("mongo.password", "mongodata").toCharArray()); 
				}
	            List<MongoCredential> credentials = new ArrayList<MongoCredential>();  
	            credentials.add(credential);  
	              
	            //通过连接认证获取MongoDB连接  
	            MongoClient mongoClient = new MongoClient(addrs,credentials, options);

				// 连接到数据库
				db = mongoClient.getDatabase(dbName);
				log.info("Connect to database successfully");

			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}
		return db;
	}
	
	/**
	 * 创建集合
	 * @param collectionName
	 * @return
	 */
	public static boolean createCollection(String collectionName){
		try{
			if(needAuthenticate){
				getDBWithCred().createCollection(collectionName);
			}else{
				getDB().createCollection(collectionName);
			}
		} catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 根据名称获取集合
	 * @param collectionName
	 * @return
	 */
	public static MongoCollection<Document> getCollection(String collectionName) {
		if(needAuthenticate){
			return getDBWithCred().getCollection(collectionName);
		}
		return getDB().getCollection(collectionName);
	}
	/**
	 * 插入单个文档
	 * @param collectionName
	 * @param document
	 * @return
	 */
	public static boolean insertOne(String collectionName, Document document){
		try{
			getCollection(collectionName).insertOne(document, new InsertOneOptions());
		}catch(Exception e){
			return false;
		}
		return true;
	}
	/**
	 * 插入多个文档
	 * @param collectionName
	 * @param documents
	 * @return
	 */
	public static boolean insertMany(String collectionName, List<Document> documents){
		try{
			getCollection(collectionName).insertMany(documents, new InsertManyOptions());
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 检索所有文档
	 * 1. 获取迭代器FindIterable<Document> 
     * 2. 获取游标MongoCursor<Document> 
     * 3. 通过游标遍历检索出的文档集合 
	 * @param collectionName
	 * @return
	 */
	public static List<Document> findAllDocuments(String collectionName){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find();
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 删除所有符合条件的文档
	 * @param collectionName
	 * @param bson 过滤条件
	 * @return
	 */
	public static boolean deleteMany(String collectionName, Bson bson){
		try{
			getCollection(collectionName).deleteMany(bson);
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public static boolean drop(String collectionName){
		try{
			getCollection(collectionName).drop();
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 删除符合条件的第一个文档 
	 * @param collectionName
	 * @param bson 过滤条件
	 * @return
	 */
	public static boolean deleteOne(String collectionName, Bson bson){
		try{
			getCollection(collectionName).deleteOne(bson);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 修改符合条件的第一个文档 
	 * @param collectionName
	 * @param dest 目的
	 * @param source 来源
	 * @return
	 */
	public static boolean updateOne(String collectionName, Bson dest, Bson source){
		try{
			getCollection(collectionName).updateOne(dest, source).equals("");
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 修改符合条件的第一个文档 
	 * @param collectionName
	 * @param dest 目的
	 * @param source 来源
	 * @return
	 */
	public static boolean updateOne(String collectionName, Bson dest, Bson source, boolean upsert){
		try{
			getCollection(collectionName).updateOne(dest, source, new UpdateOptions().upsert(upsert));
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 修改所有符合条件的文档
	 * @param collectionName
	 * @param dest 目的
	 * @param source 来源
	 * @return
	 */
	public static boolean updateMany(String collectionName, Bson dest, Bson source){
		try{
			getCollection(collectionName).updateMany(dest, source);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 修改所有符合条件的文档
	 * @param collectionName
	 * @param dest 目的
	 * @param source 来源
	 * @return
	 */
	public static boolean updateMany(String collectionName, Bson dest, Bson source, boolean upsert){
		try{
			getCollection(collectionName).updateMany(dest, source, new UpdateOptions().upsert(upsert));
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * 查询符合条件的数据
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，限制返回的数量和跳过的数量。
	 * @param collectionName
	 * @param bson
	 * @param projectBson
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson, Bson projectBson){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，限制返回的数量和跳过的数量。
	 * @param collectionName
	 * @param bson
	 * @param projectBson
	 * @param limit
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson, Bson projectBson, int limit){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson).limit(limit);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，限制返回的数量和跳过的数量。
	 * @param collectionName
	 * @param bson
	 * @param projectBson
	 * @param sortBson
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson, Bson projectBson, Bson sortBson){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson).sort(sortBson);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，限制返回的数量和跳过的数量。
	 * @param collectionName
	 * @param bson
	 * @param skipNum
	 * @param limitNum
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson, Integer skipNum, Integer limitNum){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).skip(skipNum).limit(limitNum);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，可以过滤返回的字段，可以限制返回的数据量
	 * @param collectionName
	 * @param bson
	 * @param projectBson
	 * @param skipNum
	 * @param limitNum
	 * @return
	 */
	public static List<Document> find(String collectionName, Bson bson, Bson projectBson, Integer skipNum, Integer limitNum){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson).skip(skipNum).limit(limitNum);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	public static ConfigurationUtil getConfig(){
		return config;
	}
	
	/**
	 * 查询符合条件的数据
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public static long count(String collectionName, Bson bson){
        return getCollection(collectionName).count(bson);
	}
	
	/**
	 * 查询符合条件的数据
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public static long count(String collectionName){
		return getCollection(collectionName).count();
	}
}
