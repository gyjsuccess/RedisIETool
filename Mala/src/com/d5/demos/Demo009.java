package com.d5.demos;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.d5.util.JedisIPPortUtil;

public class Demo009 {

	public static void main(String[] args) {
		JedisIPPortUtil jds = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379, null);
		Map<String, String> map = jds.hgetAll("HASH_4_DBAN_ENTITY_REPEAT", 8);
		File f = new File("D:\\HASH_4_DBAN_ENTITY_REPEAT.txt");
		int idx = 0;
		for(Map.Entry<String, String> en : map.entrySet()){
			try {
				FileUtils.writeStringToFile(f, (idx == 0 ? "" : "\n") + en.getKey(), "utf-8", true);
				idx ++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
