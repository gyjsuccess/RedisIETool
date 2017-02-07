package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class GetIdletime extends JedisCommand {
	private int db;
	private String key;
	private Long idleTime;
	
	public Long getIdleTime() {
		return idleTime;
	}

	public GetIdletime(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	protected void command() {
		jedis.select(db);
		idleTime = jedis.objectIdletime(key);
		
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_2_2;
	}

}
