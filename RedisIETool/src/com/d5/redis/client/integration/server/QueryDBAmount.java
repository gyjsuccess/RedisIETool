package com.d5.redis.client.integration.server;

import java.util.List;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class QueryDBAmount extends JedisCommand {
	private int dbAmount;

	public int getDbAmount() {
		return dbAmount;
	}

	public QueryDBAmount(int id) {
		super(id);
	}

	@Override
	public void command() {
		List<String> dbs = jedis.configGet("databases");
		if(dbs.size() > 0)
			dbAmount = Integer.parseInt(dbs.get(1));
		else
			dbAmount = 15;
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_2_0;
	}

}
