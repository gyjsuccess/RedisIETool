package com.d5.demos;

public class Demo012 {
	public static void main(String[] args) {
		int limitNum = 2000;
		int total = 6000;
		int skipNum = 0;
		while(skipNum <= total){
			System.out.println(1);
			System.out.println(skipNum);
			skipNum += limitNum;
			System.out.println(2);
			if(skipNum >= total){
				System.out.println(3);
				break;
			}
		}
	}
}
