package com.d5.thread;

import com.d5.common.Constants;
import com.d5.factory.ExecuteImplClassFactory;
import com.d5.service.data.IDataRedisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列基于redis
 * Created by 01 on 2016/7/5.
 */
public class CanExitExecuteThread extends Thread {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String redisKeyName;
    private IDataRedisService dataRedisService;
    private Integer sleepMilliSeconds;
    private boolean sleepAble = false;
    public CanExitExecuteThread(String redisKeyName, IDataRedisService dataRedisService,
    		Integer sleepMilliSeconds){
        this.redisKeyName = redisKeyName;
        this.dataRedisService = dataRedisService;
        this.sleepMilliSeconds = sleepMilliSeconds;
        if(this.sleepMilliSeconds != null && this.sleepMilliSeconds > 0){
        	this.sleepAble = true;
        }
    }
    public void run(){
        int sleepCount = 0;
        while(true){
            String info = dataRedisService.getInfoFromList(Constants.REDIS_INDEX_4_L, redisKeyName);
            log.debug(redisKeyName + "线程执行中");
            if(info == null){
                try {
                    if(sleepCount < 60 * 30/*30*/){ //半个小时内
                        sleep(1000);//休眠1秒
                        sleepCount ++;
                    }
                    else{
                        break;
                    }
                } catch (InterruptedException e) {
                }
            } else {
                sleepCount = 0; //清除之前的计数
                try{
                    ExecuteImplClassFactory.getImplClass(info).execute(info);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            
            //线程休眠
            if(this.sleepAble){
            	try {
					sleep(this.sleepMilliSeconds);
				} catch (InterruptedException e) {
					log.error("线程休眠被中断。。");
					e.printStackTrace();
				}
            }
        }
    }
}
