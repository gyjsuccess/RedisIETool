package com.d5.thread.main;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.SerializationUtils;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.factory.DataDealImplClassFactory;
import com.d5.util.HttpConstant;

import net.sf.json.JSONObject;

public class UrlSerialThread implements Runnable {
	private JSONObject info;
	private String collectName;
	private List<Map<String, String>> container;
	
	public UrlSerialThread(JSONObject info, String collectName, List<Map<String, String>> container){
		this.info = info;
		this.collectName = collectName;
		this.container = container;
	}

	@Override
	public void run() {
		JSONObject param = new JSONObject();
		param.put(Enums.JsonColums.DataDealImplClassName.toString(), Constants.DATA_DEAL_4_REDIS_IMPL_CLASS_NAME);
        outer : for(Map<String, String> map : container){
            JSONObject newInfoObj = (JSONObject) SerializationUtils.clone(info);
            newInfoObj.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.DOWNLOADER_IMPL_CLASS_NAME);
            JSONObject initData = JSONObject.fromObject(newInfoObj.getString(Enums.JsonColums.InitData.toString()));
            for(Entry<String, String> en : map.entrySet()){
            	if("url".equals(en.getKey())){
            		newInfoObj.put(Enums.JsonColums.Url.toString(), en.getValue());//url，为统一固定的字段
            		//Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H, Constants.IS_REPEAT_KEY_NAME, en.getValue(), "1");
            		continue;
            	}
            	//重复
            	if("repeatstore".equals(en.getKey())){
            		String storeName = en.getValue();
            		String url = map.get(Enums.JsonColums.Url.toString());
            		//synchronized (storeName) {
            			if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, storeName, url)){
                			continue outer;
                		}else{
                			Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H, storeName, url, "1");
                        }
					//}
            		continue;
            	}
            	if(HttpConstant.Header.REFERER.equals(en.getKey())){
            		newInfoObj.put(HttpConstant.Header.REFERER, en.getValue());//referer，为统一固定的字段
            		continue;
            	}
            	initData.put(en.getKey(), en.getValue());
            }
            newInfoObj.put(Enums.JsonColums.InitData.toString(), initData.toString());

            DataDealImplClassFactory.getImplClass(param.toString()).deal(newInfoObj.toString(), collectName);
        }
	}

}
