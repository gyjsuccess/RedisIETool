package com.d5.demos;

import com.d5.common.Enums;

public class Demo015 {
	public static void main(String[] args) {
		Enums.SeedsTypeEnum type = Enums.SeedsTypeEnum.ADD;
		System.out.println(Enums.SeedsTypeEnum.ADD.equals(type));
		type = Enums.SeedsTypeEnum.ADD;
		System.out.println(Enums.SeedsTypeEnum.ADD==type);
	}
}
