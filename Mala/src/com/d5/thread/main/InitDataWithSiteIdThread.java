package com.d5.thread.main;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;

import net.sf.json.JSONObject;

/**
 * Created by 01 on 2016/8/10.
 */
public class InitDataWithSiteIdThread implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String[] args;
    private String siteId;

    public InitDataWithSiteIdThread(String[] args, String siteId){
        this.args = args;
        this.siteId = siteId;
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
    @Override
    public void run() {
        //清理表数据
        /*MongodbUtil.drop("ProdBasicInfo4QA");
        MongodbUtil.drop("QuestionAndAnswers");*/

        // 添加第一层的种子到队列中的程序
        log.debug("添加第一层的种子到队列中的程序");
        for(int j=0; j<args.length; j++) {
            JSONObject info = new JSONObject();
            info.put(Enums.JsonColums.Url.toString(), args[j]);
            info.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.DOWNLOADER_IMPL_CLASS_NAME);
            info.put(Enums.JsonColums.InitData.toString(), JSONObject.fromObject(StringUtils.join("{\"siteId\":\"", siteId, "\"}")));
            Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.DOWNLOADER_KEY_NAME, info.toString());
        }
    }
}
