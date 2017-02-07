package com.d5.redis.client.integration.string;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class ReadString extends JedisCommand {
	private int db;
	private String key;
	private String value;
	
	public String getValue() {
		return value;
	}

	public ReadString(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	public void command() {
		jedis.select(db);
		value = jedis.get(key);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
