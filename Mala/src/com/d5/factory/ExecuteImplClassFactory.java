package com.d5.factory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.service.business.IExecuteService;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 01 on 2016/7/5.
 */
public class ExecuteImplClassFactory {
    private static Map<String, IExecuteService> implMap = new HashMap<>();

    /**
     *
     * @param info
     * @return
     */
    public static IExecuteService getImplClass(String info) {
        String implClassName = JSONObject.fromObject(info).getString(Enums.JsonColums.ExecuteImplClassName.toString());
        if(!implMap.containsKey(implClassName)) {
            try {
                implMap.put(implClassName, (IExecuteService) Class.forName(implClassName).getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return implMap.get(implClassName);
    }
}
