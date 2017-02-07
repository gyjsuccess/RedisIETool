package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class DumpKey extends JedisCommand {
	private int db;
	private String key;
	private byte[] value;
	
	public DumpKey(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	protected void command() {
		jedis.select(db);
		value = jedis.dump(key);
		
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_2_6;
	}

}
