package com.d5.service.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.service.data.IDataDealService;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;

/**
 * Created by 01 on 2016/7/28.
 */
public class DataDeal2MongodbServiceImpl implements IDataDealService<Document, String> {
	private Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * @param info        数据
     * @param collectName 序列化的集合名称
     * @return
     */
    @Override
    public String deal(Document info, String collectName) {
    	cleanData(info, "detailInfo");
        MongodbUtil.insertOne(collectName, info);
        return null;
    }
    
    private void cleanData(Document info, String cleanColName){
    	if(info.containsKey(cleanColName)){
    		String value = info.getString(cleanColName);
    		log.debug(cleanColName + " value is :" + value);
    		info.remove(cleanColName);
    		//value = value.replaceAll(" / ", "/");
    		//value = value.replaceAll(": ", ":");
    		/*List<String[]> keyValues = fillList(value);
    		for(String[] keyValue : keyValues){
    			String v = keyValue[1];
    			String k = keyValue[0];
    			if(Constants.PARAM_ATTR_RELA_MAP.containsKey(k)){
    				k = Constants.PARAM_ATTR_RELA_MAP.get(k);
    			} else {
    				log.info(k + " not has map reflected name.");
    			}
    			info.put(k, v);
    		}*/
    		fillList(info, value);
    	}
    }

	private List<String[]> fillList(String value) {
		List<String[]> valueList = new ArrayList<String[]>(5);
		String temp = SerializationUtils.clone(value);
		while(temp.length() > 0){
			String[] kV = new String[2];
			int pos1 = temp.lastIndexOf(":");
			String v = temp.substring(pos1 + 1);
			if(MatchUtil.contain(v, "^//(\\w+\\.)+\\w+.*")){
				pos1 = pos1 - 5;
			}
			kV[1] = temp.substring(pos1 + 1);
			temp = temp.substring(0, pos1);
			int pos2 = temp.lastIndexOf(" ");
			if(pos2 < 1){
				kV[0] = temp;
				temp = "";
				break;
			}else{
				kV[0] = temp.substring(pos2 + 1);
				temp = temp.substring(0, pos2);
			}
			valueList.add(kV);
		}
		return valueList;
	}
	
	public void fillList(Document info, String value){
		String pattern = "([导演编剧类型片长官方网站主又名制片国家/地区上映日期语言链接家庭成员更多中文名更多外文名职业出生地出生日期星座性别编号生卒日期季数小集数单集片长首播]|imdb|IMDb)+:";
		String[] aa = value.split(pattern);
        List<String> values = Arrays.asList(Arrays.copyOfRange(aa, 1, aa.length));
        List<String> keys = MatchUtil.findAll(value, pattern);
        int ix = 0;
        for(String key : keys){
			String k = key.replace(":", "");
			if(Constants.PARAM_ATTR_RELA_MAP.containsKey(k)){
				k = Constants.PARAM_ATTR_RELA_MAP.get(k);
			} else {
				log.info(k + " not has map reflected name.");
			}
			info.put(k, values.get(ix));
			ix ++;
        }
	}
}
