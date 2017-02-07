package com.d5.redis.client.integration.server;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class QueryServerVersion extends JedisCommand {
	private RedisVersion version;
	
	public RedisVersion getVersionInfo() {
		return version;
	}

	public QueryServerVersion(int id) {
		super(id);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

	@Override
	protected void command() {
		this.version = getRedisVersion();
	}

}
