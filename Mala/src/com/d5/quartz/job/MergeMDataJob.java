package com.d5.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.data.transfer.MergeMongodbDatas;

/**
 * Created by 01 on 2016/8/10.
 */
public class MergeMDataJob implements Job {
	private final String collectionNameSource = Constants.DEAL_M_SORCE_COLL_NAME;
	private final String collectionNameTarget = Constants.DEAL_M_TARGET_COLL_NAME;
	private final String collectionNameResult = Constants.DEAL_M_RESULT_COLL_NAME;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public MergeMDataJob () {
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("MergeMDataJob execution start...");
		MergeMongodbDatas.mergeData(collectionNameSource, collectionNameTarget,
				collectionNameResult, Constants.KEY_DB_MERGE, Constants.REDIS_INDEX_4_H);
		log.info("MergeMDataJob execution end.");
	}
}
