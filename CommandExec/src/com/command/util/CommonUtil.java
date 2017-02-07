package com.command.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);
		
	/**
	 * 62个可打印字符
	 */
	private static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", 
        "W", "X", "Y", "Z" };

	/**
	 * 短8位UUID思想其实借鉴微博短域名的生成方式，但是其重复概率过高，而且每次生成4个，需要随即选取一个。
	 * 本算法利用62个可打印字符，通过随机生成32位UUID，由于UUID都为十六进制，所以将UUID分成8组，
	 * 每4个为一组，然后通过模62操作，结果作为索引取出字符
	 * @return
	 */
	public static String generateShortUuid() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();

	}
	
	/**
	 * 短8位UUID思想其实借鉴微博短域名的生成方式，但是其重复概率过高，而且每次生成4个，需要随即选取一个。
	 * 本算法利用62个可打印字符，通过随机生成32位UUID，由于UUID都为十六进制，所以将UUID分成8组，
	 * 每4个为一组，然后通过模62操作，结果作为索引取出字符
	 * @return
	 */
	public static String generateShortUuid(String uuid) {
		if(uuid.trim().length() != 32){
			return null;
		}
		StringBuffer shortBuffer = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();
	}
	
	public static Map<Integer, Integer> parseString2Map(String mapStr) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String[] mapArr = mapStr.split(",");
		for(String ma : mapArr){
			String[] kV = ma.split(":");
			map.put(Integer.parseInt(kV[0]), Integer.parseInt(kV[1]));
		}
		return map;
	}
}
