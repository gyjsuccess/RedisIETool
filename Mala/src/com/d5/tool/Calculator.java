package com.d5.tool;

import java.util.ArrayList;
import java.util.List;

/**
 * 复杂计算器
 * 
 * @author 崔素强
 */
public class Calculator {
	public static String calculate(String text){
		// 规则，只能出现数字和加减乘除符号，最前和最后都是数字，即字符串能有效计算的
		//String text = "30*1+4*2*10-10+40/20";

		// 计算内容分割
		List<String> numList = new ArrayList<String>();
		int splitIndex = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '+' || c == '-' || c == '*' || c == '/') {
				numList.add(text.substring(splitIndex, i).trim());
				numList.add(c + "");
				splitIndex = i + 1;
			}
		}
		// 因为使用符号做判断，增加前一位和符号，所以最后一位数字不会在循环里处理
		numList.add(text.substring(splitIndex, text.length()).trim());

		/*System.out.println("====分割后====");
		for (int i = 0; i < numList.size(); i++) {
			System.out.println(i + " -> " + numList.get(i));
		}*/

		// 先做乘除计算
		List<String> list = new ArrayList<String>();
		Integer temp = null; // 用于做乘除计算临时变量
		for (int i = 1; i < numList.size(); i += 2) { // 这里只循环运算符号
			if ("+".equals(numList.get(i)) || "-".equals(numList.get(i))) {
				if (null != temp) { // 存在临时变量，说明前面进行过乘除计算
					list.add(temp.toString());
					temp = null;
				} else {
					list.add(numList.get(i - 1));
				}
				list.add(numList.get(i)); // 把符号加进去
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理

					list.add(numList.get(i + 1));

				}
			} else if ("*".equals(numList.get(i))) {
				if (null == temp) {
					temp = Integer.parseInt(numList.get(i - 1))
							* Integer.parseInt(numList.get(i + 1));
				} else {
					temp = temp * Integer.parseInt(numList.get(i + 1));
				}
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			} else if ("/".equals(numList.get(i))) {
				if (null == temp) {
					temp = Integer.parseInt(numList.get(i - 1))
							/ Integer.parseInt(numList.get(i + 1));
				} else {
					temp = temp / Integer.parseInt(numList.get(i + 1));
				}
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			}
		}
		/*System.out.println("====乘除后====");
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i + " -> " + list.get(i));
		}*/

		// 再做加减计算
		Integer sum = Integer.parseInt(list.get(0)); // 第一位不会在循环里处理
		for (int i = 1; i < list.size(); i += 2) { // 这里只循环运算符号
			if ("+".equals(list.get(i))) {
				sum += Integer.parseInt(list.get(i + 1));
			} else if ("-".equals(list.get(i))) {
				sum -= Integer.parseInt(list.get(i + 1));
			}
		}

		//System.out.println("====最终值====");
		// 打印结果
		//System.out.println(sum);
		return String.valueOf(sum);
	}
	
	public static long calculate2Long(String text){
		// 规则，只能出现数字和加减乘除符号，最前和最后都是数字，即字符串能有效计算的
		//String text = "30*1+4*2*10-10+40/20";

		// 计算内容分割
		List<String> numList = new ArrayList<String>();
		int splitIndex = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '+' || c == '-' || c == '*' || c == '/') {
				numList.add(text.substring(splitIndex, i).trim());
				numList.add(c + "");
				splitIndex = i + 1;
			}
		}
		// 因为使用符号做判断，增加前一位和符号，所以最后一位数字不会在循环里处理
		numList.add(text.substring(splitIndex, text.length()).trim());

		/*System.out.println("====分割后====");
		for (int i = 0; i < numList.size(); i++) {
			System.out.println(i + " -> " + numList.get(i));
		}*/

		// 先做乘除计算
		List<String> list = new ArrayList<String>();
		Long temp = null; // 用于做乘除计算临时变量
		for (int i = 1; i < numList.size(); i += 2) { // 这里只循环运算符号
			if ("+".equals(numList.get(i)) || "-".equals(numList.get(i))) {
				if (null != temp) { // 存在临时变量，说明前面进行过乘除计算
					list.add(temp.toString());
					temp = null;
				} else {
					list.add(numList.get(i - 1));
				}
				list.add(numList.get(i)); // 把符号加进去
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理

					list.add(numList.get(i + 1));

				}
			} else if ("*".equals(numList.get(i))) {
				if (null == temp) {
					temp = Long.parseLong(numList.get(i - 1))
							* Long.parseLong(numList.get(i + 1));
				} else {
					temp = temp * Long.parseLong(numList.get(i + 1));
				}
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			} else if ("/".equals(numList.get(i))) {
				if (null == temp) {
					temp = Long.parseLong(numList.get(i - 1))
							/ Long.parseLong(numList.get(i + 1));
				} else {
					temp = temp / Long.parseLong(numList.get(i + 1));
				}
				if (i == numList.size() - 2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			}
		}
		/*System.out.println("====乘除后====");
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i + " -> " + list.get(i));
		}*/

		// 再做加减计算
		Long sum = Long.parseLong(list.get(0)); // 第一位不会在循环里处理
		for (int i = 1; i < list.size(); i += 2) { // 这里只循环运算符号
			if ("+".equals(list.get(i))) {
				sum += Long.parseLong(list.get(i + 1));
			} else if ("-".equals(list.get(i))) {
				sum -= Long.parseLong(list.get(i + 1));
			}
		}

		//System.out.println("====最终值====");
		// 打印结果
		//System.out.println(sum);
		return sum;
	}
}