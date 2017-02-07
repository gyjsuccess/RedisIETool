package com.d5.service.data;

/**
 * Created by 01 on 2016/7/27.
 */
public interface IDataDealService<T, R> {
    /**
     *
     * @param info 数据
     * @param collectName 序列化的集合名称
     * @return
     */
    public R deal(T info, String collectName);
}
