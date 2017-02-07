package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class DeleteKey extends JedisCommand {
	private int db;
	private String key;
	
	public DeleteKey(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	public void command() {
		jedis.select(db);
		jedis.del(key);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
