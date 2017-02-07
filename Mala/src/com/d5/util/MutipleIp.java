package com.d5.util;

import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Created by chuckpu on 2016/9/18.
 */
public class MutipleIp {
    private static final Logger log = LoggerFactory.getLogger(MutipleIp.class);
    private static LinkedList<Map<String, Integer>> ipStatusMapList = null;
    private static boolean isUsing = true;
    
    private static void genIpStatusMapList(){
    	synchronized ("1") {
    		if(ipStatusMapList == null){
        		ipStatusMapList = new LinkedList<Map<String, Integer>>();
        		String ipbind = "";
                for(int i=21;i<31;i++){
                    ipbind = "10.129.9."+i;
                    Map<String, Integer> map = Maps.newHashMap();
                    map.put(ipbind, 0);
                    ipStatusMapList.add(map);//未使用
                }
        	}
		}
    }
    
    public static void modifyStatus(boolean stauts){
    	isUsing = stauts;
    }
    
    public static String getNewBindIp(){
    	genIpStatusMapList();
    	String bindIp = "";
    	Map<String, Integer> map = null;
    	synchronized (bindIp) {
    		if(isUsing){
        		map = ipStatusMapList.getFirst();
        	} else {
        		map = ipStatusMapList.removeFirst();
        		ipStatusMapList.addLast(map);
        		map = ipStatusMapList.getFirst();
        		isUsing = true;
        	}
		}
    	bindIp = map.entrySet().iterator().next().getKey();
    	return bindIp;
    }
}
