package com.d5.quartz.job;

import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.data.transfer.RemoteMongoDataSync;

/**
 * Created by 01 on 2016/8/10.
 */
public class SyncRMDataJob implements Job {
	private final int limitNum = 2000;
	private final String collectName = Constants.SYNC_REMOTE_COLL_NAME;
	private final String tCollectName = Constants.SYNC_LOCAL_COLL_NAME;
	private final Document query = new Document();
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public SyncRMDataJob () {
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("SyncRMDataJob execution start...");
		RemoteMongoDataSync.syncRemoteData(collectName, tCollectName, query, limitNum);
		log.info("SyncRMDataJob execution end.");
	}
}
