package com.d5.demos;

import org.bson.Document;

public class Demo006 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Document d1 = new Document().append("name", "a1")
				.append("url", "http://11.12.sf/")
				.append("age", 30)
				.append("address", "sefsefsefsefsefes");
		System.out.println(d1);
		Document d2 = new Document().append("name", "a1")
				.append("url", "http://11.12.sf/3223f/")
				.append("age", 20)
				.append("address", "sefsefsefsefsefes")
				.append("tel", "15212364587")
				.append("fax", "01256891234");
		
		d1.putAll(d2);
		System.out.println(d1);
		
		/**
		 * 1.获取源数据
		 * 2.提取需要处理的字段
		 * 3.将提取到的数据与抓取的数据进行字符串拼接，调用算法的程序，得到最终的字符串
		 * 4.将最终字符串和抓取的到数据进行合并
		 * 5.合并抓取的数据和源数据
		 * 6.将数据写入新的mongodb集合中去
		 */
	}
}
