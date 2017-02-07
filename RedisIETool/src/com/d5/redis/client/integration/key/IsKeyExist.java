package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class IsKeyExist extends JedisCommand {
	private int db;
	private String key;
	private boolean exist;
	
	public boolean isExist() {
		return exist;
	}

	public IsKeyExist(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	protected void command() {
		jedis.select(db);
		exist = jedis.exists(key);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
