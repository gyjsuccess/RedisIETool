package com.d5.quartz.job;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.Spider;
import com.d5.tool.InitSeedsUtil;

/**
 * Created by 01 on 2016/8/10.
 */
public class InitSeeds4Add1J implements Job {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String collectionName = Constants.DEAL_INITSEEDS_ADD_COLL_NAME;
    private final String urlTemplate = "https://movie.douban.com/subject_search?search_text={name}&cat=1002";
    private Spider spider;
    public InitSeeds4Add1J () {
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("InitSeeds4Add1J 执行开始");
		if(StringUtils.isBlank(collectionName)){
			log.error("collect name is error.");
		} else {
			spider = (Spider) context.getJobDetail().getJobDataMap().get(context.getJobDetail().getKey().toString());
			InitSeedsUtil.run(spider, urlTemplate, collectionName, Enums.SeedsTypeEnum.ADD);
		}
		log.info("InitSeeds4Add1J 执行结束");
	}
}
