package com.d5.util;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;

public class RedisDataUtil {
	private static Logger log = LoggerFactory.getLogger(RedisDataUtil.class);
	public static String getXIP() {
		String xIp = JedisUtil.rpop(Constants.IP_PORT_4_XFORWARD, Constants.REDIS_INDEX_4_H);
		JedisUtil.lpush(Constants.IP_PORT_4_XFORWARD, xIp, Constants.REDIS_INDEX_4_H);
		log.debug("xIp is :{}", xIp);
		return MatchUtil.find(xIp, Constants.REGEX_4_IP_PORT, "", 1);
	}
	public static HttpHost getProxyIpPort() {
		String proxyIpPort = JedisUtil.rpop(Constants.IP_PORT_4_PROXY, Constants.REDIS_INDEX_4_H);
		JedisUtil.lpush(Constants.IP_PORT_4_PROXY, proxyIpPort, Constants.REDIS_INDEX_4_H);
		log.debug("proxyIpPort is :{}", proxyIpPort);
		String[] arr = proxyIpPort.split(":");
		HttpHost proxy = new HttpHost(arr[0], Integer.parseInt(arr[1]));
		return proxy;
	}
}
