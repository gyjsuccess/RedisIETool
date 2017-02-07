package com.d5.demos;

import org.apache.commons.lang.StringUtils;

public class Demo013 {
	public static void main(String[] args) {
		String str = "又名: The Light";
		for(String s : StringUtils.split(str, ":", 2)){
			System.out.println(s);
		}
	}
}
