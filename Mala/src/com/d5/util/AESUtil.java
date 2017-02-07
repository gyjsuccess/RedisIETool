package com.d5.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * AES加密算法工具类
 * 
 * @author jie.xiao
 *
 */
public class AESUtil {
	private static Key key;
	private static String KEY_STR = "changhong";// 密钥
	private static String CHARSETNAME = "UTF-8";// 编码
	private static String ALGORITHM = "AES";// 加密类型

	static {
		try {
			 //防止linux下 随机生成key 
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );  
            secureRandom.setSeed(KEY_STR.getBytes());  
			KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
			generator.init(128,secureRandom);
			key = generator.generateKey();
			generator = null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对str进行DES加密
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncryptString(String str) {
		BASE64Encoder base64encoder = new BASE64Encoder();
		try {
			byte[] bytes = str.getBytes(CHARSETNAME);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] doFinal = cipher.doFinal(bytes);
			return base64encoder.encode(doFinal);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对str进行DES解密
	 * 
	 * @param str
	 * @return
	 */
	public static String getDecryptString(String str) {
		BASE64Decoder base64decoder = new BASE64Decoder();
		try {
			byte[] bytes = base64decoder.decodeBuffer(str);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] doFinal = cipher.doFinal(bytes);
			return new String(doFinal, CHARSETNAME);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		List<String> list = Arrays.asList(new String[]{"root", "mongodata"});
		for(String str : list){
			String strEnc = AESUtil.getEncryptString(str);// 加密字符串,返回String的密文
			System.out.println(strEnc);

			String strDes = AESUtil.getDecryptString(strEnc);// 把String 类型的密文解密
			System.out.println(strDes);
		}
	}
}