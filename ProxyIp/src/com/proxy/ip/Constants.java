package com.proxy.ip;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

import com.google.common.collect.Maps;

public class Constants {
	public static final String REDIS_LIST_4_SUCC = "voteonce4succ";
	public static volatile String REDIS_LIST_4_ALL = "";
	public static String PROXY_SITE_NAME = "";
	public static Map<String, String> EXTRA_HEADERS_MAP = Maps.newHashMap();
	public static Map<String, List<NameValuePair>> postParamsMap = Maps.newHashMap();
}
