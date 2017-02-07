package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class GetEncoding extends JedisCommand {
	private int db;
	private String key;
	private String encoding;
	
	public String getEncoding() {
		return encoding;
	}

	public GetEncoding(int id, int db, String key) {
		super(id);
		this.db = db;
		this.key = key;
	}

	@Override
	protected void command() {
		jedis.select(db);
		encoding = jedis.objectEncoding(key);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_2_2;
	}

}
