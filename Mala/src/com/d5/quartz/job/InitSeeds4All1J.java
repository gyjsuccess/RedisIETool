package com.d5.quartz.job;

import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.Spider;
import com.d5.tool.InitSeedsUtil;
import com.d5.util.MongodbUtil;
import com.mongodb.client.model.Filters;

/**
 * Created by 01 on 2016/8/10.
 */
public class InitSeeds4All1J implements Job {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Spider spider;
    public InitSeeds4All1J () {
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("InitSeeds4All1J 执行开始");
		spider = (Spider) context.getJobDetail().getJobDataMap().get(context.getJobDetail().getKey().toString());
		InitSeedsUtil.run(spider, null, null, Enums.SeedsTypeEnum.ALL);
		
		//清理douban_media_digital_info表中一个月以前的数据。
		MongodbUtil.deleteMany(Constants.COLL_NAME_4_DIGITAL,
				Filters.lt("getDateTime", DateTime.now().minusMonths(1).toString("yyyy-MM-dd 00:00:00")));
		log.info("InitSeeds4All1J 执行结束");
	}
}
