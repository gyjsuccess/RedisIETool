package com.d5.quartz.job;

import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.crawler.Spider;

/**
 * Created by 01 on 2016/8/10.
 */
public class InitData4Spider1J implements Job {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Spider spider;
    public InitData4Spider1J () {
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
    	if(spider.getIsAllCraw()){
    		spider.addRequest(spider.getSite().getStartRequests());
    		log.debug("spider.addRequest 执行了");
    		
    		spider.setExecuteTime(DateTime.now().toString(Constants.DATE_FORMATTER));
    	}
    	log.debug("InitData4Spider1TJ 执行了");
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		spider = (Spider) context.getJobDetail().getJobDataMap().get(context.getJobDetail().getKey().toString());
		run();
	}
}
