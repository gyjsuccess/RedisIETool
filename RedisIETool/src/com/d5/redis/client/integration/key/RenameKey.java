package com.d5.redis.client.integration.key;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.JedisCommand;

public class RenameKey extends JedisCommand {
	private int db;
	private String oldKey;
	private String newKey;
	private boolean overwritten;
	private Long result;
	
	public Long getResult() {
		return result;
	}

	public RenameKey(int id, int db, String oldKey, String newKey, boolean overwritten) {
		super(id);
		this.db = db;
		this.oldKey = oldKey;
		this.newKey = newKey;
		this.overwritten = overwritten;
				
	}

	@Override
	public void command() {
		jedis.select(db);
		if(overwritten)
			jedis.rename(oldKey, newKey);
		else
			result = jedis.renamenx(oldKey, newKey);
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
