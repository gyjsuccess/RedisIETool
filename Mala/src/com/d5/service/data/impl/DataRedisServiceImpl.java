package com.d5.service.data.impl;

import java.util.Map;

import com.d5.service.data.IDataRedisService;
import com.d5.util.JedisUtil;

/**
 * Created by 01 on 2016/7/5.
 */
public class DataRedisServiceImpl implements IDataRedisService {
    @Override
    public void addInfo2List(int index, String keyName, String info) {
        JedisUtil.lpush(keyName, info, index);
    }

    @Override
    public String getInfoFromList(int index, String keyName) {
        return JedisUtil.rpop(keyName, index);
    }

    @Override
    public void addInfo2Map(int index, String keyName, String fieldName, String info) {
        JedisUtil.hset(keyName, fieldName, info, index);
    }
    
    @Override
    public void addInfo2Map(int index, String keyName, Map<String, String> hash) {
        JedisUtil.hmset(keyName, hash, index);
    }

    @Override
    public String getInfoFromMap(int index, String keyName, String fieldName) {
        return JedisUtil.hget(keyName, fieldName, index);
    }
    @Override
    public Map<String, String> getAllInfoFromMap(int index, String keyName) {
    	return JedisUtil.hgetAll(keyName, index);
    }

    @Override
    public boolean existInHash(int index, String keyName, String fieldName) {
        return JedisUtil.hexists(keyName, fieldName, index);
    }
}
