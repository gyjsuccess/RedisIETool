package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class TTLs extends JedisCommand {
	private int db;
	private String key;
	private long second;
	
	public long getSecond() {
		return second;
	}

	public TTLs(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	protected void command() {
		jedis.select(db);
		second = jedis.ttl(key);
		
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
