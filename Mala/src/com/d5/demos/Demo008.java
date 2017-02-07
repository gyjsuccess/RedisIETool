package com.d5.demos;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Months;

import com.d5.util.MatchUtil;

public class Demo008 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str = " 2013-09-06(中国大陆) / 2012-12-28(台湾) ";
		/*System.out.println(MatchUtil.find(str, "(\\d{4}\\-\\d{1,2}\\-\\d{1,2})", "", 1));
		str = " 2012-12-28(中国大陆) ";
		System.out.println(MatchUtil.find(str, "(\\d{4}\\-\\d{1,2}\\-\\d{1,2})", "", 1));
		str = " 1991-03-29 ";
		System.out.println(MatchUtil.find(str, "(\\d{4}\\-\\d{1,2}\\-\\d{1,2})", "", 1));
		*/
		
		String issue = MatchUtil.find(str, "(\\d{4}\\-\\d{1,2}\\-\\d{1,2})", "", 1);
		System.out.println(issue);
		int diff = Math.abs(Months.monthsBetween(DateTime.now(), DateTime.parse(issue)).getMonths());
		System.out.println(diff);
		System.out.println(5/2);
	}

}
