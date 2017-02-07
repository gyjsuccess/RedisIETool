package com.d5.util;

import javaTEA.tea;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * PackageName: com.d5.util
 * @ClassName DecryptUtil
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月19日 下午4:17:20
 */
public class DecryptUtil {
	private Logger log = LoggerFactory.getLogger(DecryptUtil.class);
	private String key;
	
	/**
	 * 
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 */
	private DecryptUtil(){
	}
	
	/**
	 * 
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param key
	 */
	public DecryptUtil(String key){
		this.key = key;
	}

    /**
     * 解密方法
     *
     * @param encryptedStr
     * @return
     */
    public String decrypt(String encryptedStr) {
        try {
            return tea.hex_de(encryptedStr, key.getBytes(), 16);
        } catch (UnsupportedEncodingException e) {
        	log.error("{}", e);
        }
        return "";
    }
    
    /**
     * 获取加密密码
     *
     * 
     *
     */
    public String getEncryptedKey(String password) {
        return tea.hex_en(password.getBytes(), key.getBytes(), 16);
    }

    /**
     * 获取加密密码
     *
     * 
     *
     */
    public static String getEncryptedKey(String password, String key) {
        return tea.hex_en(password.getBytes(), key.getBytes(), 16);
    } 
}
