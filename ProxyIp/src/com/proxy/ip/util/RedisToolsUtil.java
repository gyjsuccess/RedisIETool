package com.proxy.ip.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.ip.ProxyIpClean;

public class RedisToolsUtil {
	private static final JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
	private static Logger log = LoggerFactory.getLogger(ProxyIpClean.class);
	public static void main(String[] args) {
		List2List("ActiveProxy", "");
	}
	
	public static void List2List(String sKeyName, String tKeyName){
		List<String> sList = jedis.lpopList(sKeyName, 5);
		for(String s : sList){
			jedis.lpush(tKeyName, s, 5);
		}
	}
}
