package com.d5.factory;

import java.util.HashMap;
import java.util.Map;

import com.d5.common.Enums;
import com.d5.service.data.IDataDealService;

import net.sf.json.JSONObject;

/**
 * Created by 01 on 2016/7/27.
 */
public class DataDealImplClassFactory {
    private static Map<String, IDataDealService> implMap = new HashMap<>();

    /**
     *
     * @param info
     * @return
     */
    public static IDataDealService getImplClass(String info) {
        String implClassName = JSONObject.fromObject(info).getString(Enums.JsonColums.DataDealImplClassName.toString());
        if(!implMap.containsKey(implClassName)) {
            try {
                implMap.put(implClassName, (IDataDealService) Class.forName(implClassName).getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return implMap.get(implClassName);
    }
}
