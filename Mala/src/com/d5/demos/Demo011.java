package com.d5.demos;

import java.io.File;

public class Demo011 {

	public static void main(String[] args) {
		String path = new File(Demo011.class.getResource("/").getPath()).getParentFile().getPath() + System.getProperty("file.separator") + "resource" + System.getProperty("file.separator") + "crawl.js ";
		System.out.println(Demo011.class.getResource("/").getPath());
		System.out.println(path);
	}
}
