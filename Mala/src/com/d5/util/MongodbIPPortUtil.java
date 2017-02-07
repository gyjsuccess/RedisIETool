package com.d5.util;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;

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
public class MongodbIPPortUtil {
	private Logger log = LoggerFactory.getLogger(MongodbIPPortUtil.class);
	private MongoDatabase db;
	private boolean needAuthenticate;
	private String authenticateType;
	private String dbName;
	private String hostIp;
	private int hostPort;
	private String user;
	private String password;

	private MongodbIPPortUtil(){}
	
	public static MongodbIPPortUtil getInstance(){
		return new MongodbIPPortUtil();
	}
	
	public MongodbIPPortUtil init(String ip, Integer hostPort, String dbName, 
			Boolean needAuthenticate, String authenticateType, String credUser, String credPassword){
		this.needAuthenticate = needAuthenticate == null ? false : needAuthenticate;
		this.authenticateType = authenticateType == null ? "CRCredential" : authenticateType;
		this.dbName = dbName == null ? "test" : dbName;
		this.hostIp = ip;
		this.hostPort = hostPort;
		this.user = credUser == null ? "root" : credUser;
		this.password = credPassword == null ? "mongodbdata" : credPassword;
		
		if(needAuthenticate){
			getDBWithCred();
		} else {
			getDB();
		}
		
		return this;
	}
	
	private MongoDatabase getDB() {
		if (db == null) {
			try {
				// 连接到 mongodb 服务
				log.debug("hostIp :{}", hostIp);
				MongoClient mongoClient = new MongoClient(hostIp, hostPort);

				// 连接到数据库
				db = mongoClient.getDatabase(dbName);
				log.info("Connect to database successfully");

			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}
		return db;
	}
	
	private MongoDatabase getDBWithCred() {
		if (db == null) {
			try {
				//连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址  
	            //ServerAddress()两个参数分别为 服务器地址 和 端口  
				ServerAddress serverAddress = new ServerAddress(
						hostIp, hostPort);
	            List<ServerAddress> addrs = new ArrayList<ServerAddress>();  
	            addrs.add(serverAddress);  
	              
	            //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码  
	            MongoCredential credential = null;
				if("CRCredential".equals(authenticateType)){	
					credential = MongoCredential.createMongoCRCredential(
						user, dbName, password.toCharArray()); 
				}
				
				if("Sha1".equals(authenticateType)){	
					credential = MongoCredential.createScramSha1Credential(
							user, dbName, password.toCharArray()); 
				}
	            List<MongoCredential> credentials = new ArrayList<MongoCredential>();  
	            credentials.add(credential);  
	              
	            //通过连接认证获取MongoDB连接  
	            MongoClient mongoClient = new MongoClient(addrs,credentials);

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
	public boolean createCollection(String collectionName){
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
	public MongoCollection<Document> getCollection(String collectionName) {
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
	public boolean insertOne(String collectionName, Document document){
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
	public boolean insertMany(String collectionName, List<Document> documents){
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
	public List<Document> findAllDocuments(String collectionName){
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
	public boolean deleteMany(String collectionName, Bson bson){
		try{
			getCollection(collectionName).deleteMany(bson);
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public boolean drop(String collectionName){
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
	public boolean deleteOne(String collectionName, Bson bson){
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
	public boolean updateOne(String collectionName, Bson dest, Bson source){
		try{
			getCollection(collectionName).updateOne(dest, source);
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
	public boolean updateMany(String collectionName, Bson dest, Bson source){
		try{
			getCollection(collectionName).updateMany(dest, source);
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
	public List<Document> find(String collectionName, Bson bson){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，可以过滤返回的字段
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public List<Document> find(String collectionName, Bson bson, Bson projectBson){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson);
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
	 * @return
	 */
	public List<Document> find(String collectionName, Bson bson, Bson projectBson, int limit){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).projection(projectBson).limit(limit);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据，可以过滤返回的字段，可以排序。
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public List<Document> find(String collectionName, Bson bson, Bson projectBson, Bson sortBson){
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
	 * @return
	 */
	public List<Document> find(String collectionName, Bson bson, Integer skipNum, Integer limitNum){
		List<Document> documents = new ArrayList<Document>();
		FindIterable<Document> findIterable = getCollection(collectionName).find(bson).skip(skipNum).limit(limitNum);
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while (mongoCursor.hasNext()) {
			documents.add(mongoCursor.next());
		}
        return documents;
	}
	
	/**
	 * 查询符合条件的数据
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public long count(String collectionName, Bson bson){
        return getCollection(collectionName).count(bson);
	}
	
	/**
	 * 查询符合条件的数据
	 * @param collectionName
	 * @param bson
	 * @return
	 */
	public long count(String collectionName){
		return getCollection(collectionName).count();
	}
}
