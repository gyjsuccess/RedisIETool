package com.d5.tool;

import org.apache.commons.lang3.StringUtils;

import com.d5.common.Constants;

public class AllCrawFlagDeal {
	public static String getAllCrawFlag(String uuid){
		return Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
        		Constants.ALL_CRAW_KEY_NAME, uuid);
	}
	
	public static void writeAllCrawFlag(String uuid, String value){
		Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
        		Constants.ALL_CRAW_KEY_NAME, uuid, value);
	}
	
	public static boolean isEqual(String uuid, String value){
		String allCrawFlag = AllCrawFlagDeal.getAllCrawFlag(uuid);
		if(StringUtils.isBlank(allCrawFlag)){
			return false;
		}
		return StringUtils.equals(value, allCrawFlag);
	}
}
