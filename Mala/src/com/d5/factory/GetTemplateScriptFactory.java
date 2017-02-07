package com.d5.factory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.util.MatchUtil;

import net.sf.json.JSONObject;

/**
 * Created by 01 on 2016/7/28.
 */
public class GetTemplateScriptFactory {
	private static Logger log = LoggerFactory.getLogger(GetTemplateScriptFactory.class);
    /**
     *
     * @param info
     * @return
     */
    public static String getTemplateScript(String info){
        String url = JSONObject.fromObject(info).getString(Enums.JsonColums.Url.toString());
        for(Map.Entry<String, String> entry : Constants.FILTER_SCRIPT_MAP.entrySet()){
            String pattern = entry.getKey();
            try {
            	if(MatchUtil.contain(url, pattern)){
                    return entry.getValue();
                }
			} catch (Exception e) {
				log.error("pattern is :{}.  --- {}", pattern, e);
			}
        }
        return null;
    }
}
