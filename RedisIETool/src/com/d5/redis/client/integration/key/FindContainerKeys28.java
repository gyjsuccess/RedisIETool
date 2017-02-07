package com.d5.redis.client.integration.key;

import static redis.clients.jedis.ScanParams.SCAN_POINTER_START;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.d5.redis.client.domain.NodeType;
import com.d5.redis.client.domain.RedisVersion;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class FindContainerKeys28 extends FindContainerKeys {

	public FindContainerKeys28(int id, int db, String container, String keyPattern) {
		super(id, db, container, keyPattern);
	}

	public FindContainerKeys28(int id, int db, String container, String keyPattern, List<NodeType> valueTypes, boolean forward) {
		super(id, db, container, keyPattern, valueTypes, forward);
	}
	
	protected Set<String> getResult() {
		Set<String> nodekeys = new HashSet<String>();
		assert(container != null);
		
		ScanParams params = new ScanParams();
		params.match(container + keyPattern);
		ScanResult<String> result;
		String cursor = SCAN_POINTER_START;
		do {
			result = jedis.scan(cursor, params);
			nodekeys.addAll(result.getResult());
			cursor = result.getStringCursor();
		}while(!result.getStringCursor().equals(SCAN_POINTER_START));
		
		return nodekeys;
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_2_8;
	}

}
