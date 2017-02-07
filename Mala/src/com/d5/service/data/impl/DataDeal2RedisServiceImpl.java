package com.d5.service.data.impl;

import com.d5.common.Constants;
import com.d5.service.data.IDataDealService;

/**
 * Created by 01 on 2016/7/28.
 */
public class DataDeal2RedisServiceImpl implements IDataDealService<String, String> {

    /**
     * @param info        数据
     * @param collectName 序列化的集合名称
     * @return
     */
    @Override
    public String deal(String info, String collectName) {
        Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L, collectName, info);
        return null;
    }
}
