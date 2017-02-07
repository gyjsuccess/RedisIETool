package com.d5.thread.main;

import java.util.List;

import org.bson.Document;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.factory.DataDealImplClassFactory;

import net.sf.json.JSONObject;

public class DataSerialThread implements Runnable {
	private JSONObject info;
	private String collectName;
	private List<Document> container;
	
	public DataSerialThread(JSONObject info, String collectName, List<Document> container){
		this.info = info;
		this.collectName = collectName;
		this.container = container;
	}
	@Override
	public void run() {
		JSONObject param = new JSONObject();
		param.put(Enums.JsonColums.DataDealImplClassName.toString(), Constants.DATA_DEAL_4_MONGODB_IMPL_CLASS_NAME);
        for(Document doc : container){
            //重复
            /*if(doc.containsKey("url")){
                if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, Constants.IS_REPEAT_KEY_NAME, doc.getString("url"))){
                    continue;
                }
            }*/

            //TODO getDate的开关放入配置文件
            //doc.put("getDate", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            DataDealImplClassFactory.getImplClass(param.toString()).deal(doc, collectName);
        }
	}

}
