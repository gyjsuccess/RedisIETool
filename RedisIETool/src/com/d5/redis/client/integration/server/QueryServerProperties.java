package com.d5.redis.client.integration.server;

import java.util.HashMap;
import java.util.Map;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class QueryServerProperties extends JedisCommand {
	private Map<String, String[]> serverInfo = new HashMap<String, String[]>();
	
	public QueryServerProperties(int id) {
		super(id);
	}

	public Map<String, String[]> getServerInfo() {
		return serverInfo;
	}

	@Override
	protected void command() {
		String info = jedis.info();
		String[] tabs = info.split("#");
		
		for(String tab: tabs){
			if(tab.length() > 0){
				String[] keys = tab.split("\r\n");
				String[] values = new String[keys.length-1];
				for(int i = 1; i < keys.length; i ++) {
					values[i-1] = keys[i];
				}
				serverInfo.put(keys[0], values);
			}
		}
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
