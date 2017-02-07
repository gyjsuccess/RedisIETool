/******************************************************************
 *    ProjectName: Mala
 *    FileName: CalculateWeightThread.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月20日 下午3:49:25
 *    Revision:
 *    2017年1月20日 下午3:49:25
 *        - first revision
 *****************************************************************/
package com.d5.data.transfer.thread;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.data.transfer.DataDeal;
import com.d5.service.data.IDataRedisService;
import com.d5.tool.WeightCalculate;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;

/**
 * PackageName: com.d5.data.transfer.thread
 * @ClassName CalculateWeightThread
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月20日 下午3:49:25
 */
public class CalculateWeightThread extends Thread {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String redisKeyName;
    private IDataRedisService dataRedisService;
    private Integer sleepMilliSeconds;
    private String collectionName;
    public CalculateWeightThread(String redisKeyName, IDataRedisService dataRedisService,
    		Integer sleepMilliSeconds, String collectionName){
        this.redisKeyName = redisKeyName;
        this.dataRedisService = dataRedisService;
        this.sleepMilliSeconds = sleepMilliSeconds;
        this.collectionName = collectionName;
        if(this.sleepMilliSeconds != null && this.sleepMilliSeconds > 0){
        }
    }
    @Override
    public void run(){
    	int sleepCount = 0;
        while(true){
            String info = dataRedisService.getInfoFromList(Constants.REDIS_INDEX_4_L, redisKeyName);
            log.debug("{}线程{}执行中", "CalculateWeightThread", this.getName());
            if(info == null){
                try {
                    if(sleepCount < 60 * 10/*30*/){ //10分钟内
                        sleep(1000);//休眠1秒
                        sleepCount ++;
                    }
                    
                    if(sleepCount >= 60 * 10 && sleepCount < 60 * 30){ //半个小时内
                        sleep(2000);//休眠2秒
                        sleepCount ++;
                    }
					
					if(sleepCount >= 60 * 30 && sleepCount < 60 * 60){ //半个小时~1小时
						sleep(5000);//休眠5秒
					}

					if(sleepCount >= 60 * 60){ //1小时以后
						sleep(15000);//休眠15秒
					}

					if(sleepCount < 4000){//4000以内计数+1，4000以后就不再+1了，因为休眠时间已经不再改变了。
						sleepCount ++;
					}
                } catch (InterruptedException e) {
                }
            } else {
                sleepCount = 0; //清除之前的计数
                try{
                	//获取redis数据
                	Document doc = Document.parse(info);
                	String celebrities = "";
                	String movieType = doc.getString("tv_model");
                	long heatValue = doc.getLong("heatValue");
                	Map<String, Double> wMap = null;
                	List<Document> celebrityMWList = Lists.newArrayList();
                	if(doc.containsKey("director")){
                		celebrities = doc.getString("director");
        				if(!celebrities.matches("\\s*(不详|未知)?\\s*")){
        					wMap = WeightCalculate.calculateWeight(heatValue, celebrities, movieType);
        					DataDeal.fillList(celebrityMWList, wMap, doc);
        				}
        			}
        			if(doc.containsKey("writer")){
        				celebrities = doc.getString("writer");
        				if(!celebrities.matches("\\s*(不详|未知)?\\s*")){
        					wMap = WeightCalculate.calculateWeight(heatValue, celebrities, movieType);
        					DataDeal.fillList(celebrityMWList, wMap, doc);
        				}
        			}
        			if(doc.containsKey("cast")){
        				celebrities = doc.getString("cast");
        				if(!celebrities.matches("\\s*(不详|未知)?\\s*")){
        					wMap = WeightCalculate.calculateWeight(heatValue, celebrities, movieType);
        					DataDeal.fillList(celebrityMWList, wMap, doc);
        				}
        			}
        			
        			//写入数据
        			for(Document _doc : celebrityMWList){
        				Document _doc_ = SerializationUtils.clone(_doc);
        				_doc_.remove("weight");
        				MongodbUtil.updateOne(collectionName, _doc_, new Document().append("$set", _doc), true);
        				log.debug("写入数据");
        			}
        			log.debug("写入数据完成");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
