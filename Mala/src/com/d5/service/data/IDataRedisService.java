package com.d5.service.data;

import java.util.Map;

/**
 * Created by 01 on 2016/7/5.
 */
public interface IDataRedisService {
    /**
     *
     * @param index
     * @param keyName
     * @param info
     */
    public void addInfo2List(int index, String keyName, String info);

    /**
     *
     * @param index
     * @param keyName
     * @return
     */
    public String getInfoFromList(int index, String keyName);

    /**
     *
     * @param index
     * @param keyName
     * @param fieldName
     * @param info
     */
    public void addInfo2Map(int index, String keyName, String fieldName, String info);

    /**
     *
     * @param index
     * @param keyName
     * @param fieldName
     * @return
     */
    public String getInfoFromMap(int index, String keyName, String fieldName);

    /**
     *
     * @param index
     * @param keyName
     * @param fieldName
     * @return
     */
    public boolean existInHash(int index, String keyName, String fieldName);

	/**
	 * MethodName：addInfo2Map
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午4:09:56
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param index
	 * @param keyName
	 * @param hash
	 */
	void addInfo2Map(int index, String keyName, Map<String, String> hash);

	/**
	 * MethodName：getAllInfoFromMap
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午5:09:26
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param index
	 * @param keyName
	 * @return
	 */
	Map<String, String> getAllInfoFromMap(int index, String keyName);
}
