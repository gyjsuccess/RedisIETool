package com.d5.thread.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;

import net.sf.json.JSONObject;

/**
 * Created by 01 on 2016/8/10.
 */
public class InitData4SFThread implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String[] args;

    public InitData4SFThread(String[] args){
        this.args = args;
    }

    /**
     */
    @Override
    public void run() {
        //清理表数据
        //MongodbUtil.drop("ProdBasicInfo4QA");
        //MongodbUtil.drop("QuestionAndAnswers");
    	
        // 添加第一层的种子到队列中的程序
        /*log.debug("添加第一层的种子到队列中的程序");
        List<String> filter = Arrays.asList(args);
        
        Document query = new Document();
        List<Document> categoryList = MongodbUtil.find("category_dic_collect", query);
        
        for(Document cate : categoryList) {
        	if(!filter.contains(cate.getString("secondCategory"))){
        		continue;
        	}
            JSONObject info = new JSONObject();
            info.put(Enums.JsonColums.Url.toString(), cate.getString("url") + "b1saledate/");
            info.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.DOWNLOADER_IMPL_CLASS_NAME);
            info.put(Enums.JsonColums.InitData.toString(), JSONObject.fromObject("{\"siteKindId\":7,\"siteId\":26,\"categoryId\":\"" + cate.getString("cateId") + "\"}"));
            Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.DOWNLOADER_KEY_NAME, info.toString());
        }*/
    	
    	log.debug("添加第一层的种子到队列中的程序");
        for(int j=0; j<args.length; j++) {
            JSONObject info = new JSONObject();
            info.put(Enums.JsonColums.Url.toString(), args[j]);
            info.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.DOWNLOADER_IMPL_CLASS_NAME);
            info.put(Enums.JsonColums.InitData.toString(), JSONObject.fromObject("{}"));
            Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.DOWNLOADER_KEY_NAME, info.toString());
        }
    }
}
