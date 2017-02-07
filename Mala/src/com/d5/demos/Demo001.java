package com.d5.demos;

import java.util.List;

import com.d5.util.MatchUtil;

/**
 * Created by 01 on 2016/7/5.
 */
public class Demo001 {
    public static void main(String[] args){
    	/*SiteInfo siteInfo = new SiteInfo("27");
        System.out.println(JSON.toJSONString(siteInfo));
        System.out.println(JSONObject.parseObject("{\"id\":36}").toString());
        System.out.println(StringUtils.join("{\"", Constants.JsonColums.Url.toString(), "\":\"", "http:///", "\"}"));
        System.out.println(Calculator.calculate2Long("24 * 60 * 60 * 1000"));
        
        System.out.println(CommonUtil.generateShortUuid(DigestUtils.md5Hex("www.baidu.com")));*/
        
        /*String lastPos = "2016-10-08 18:00:22";
        String value = "2017-10-08 10:00:00";
        System.out.println(value.compareTo(lastPos));*/
    	/*String url = "http://www.mala.cn/thread-4523447-3-470.html";
    	String findAll = ".*thread-\\d+-|-.*";
    	List<String> urlParts = MatchUtil.findAll(url, findAll);
    	System.out.println(urlParts.toString());*/
    	System.out.println("2".compareTo("1"));
    }
}
