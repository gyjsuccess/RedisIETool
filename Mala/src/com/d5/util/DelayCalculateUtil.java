package com.d5.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

public class DelayCalculateUtil {
	public static final long ONE_DAY = 60 * 60 * 24 * 1000;
	public static final long ONE_HOUR = 60 * 60 * 1000;
	public static final long ONE_MINUTE = 60 * 1000;
	public static final long ONE_SECOND = 1 * 1000;
	
	private DelayCalculateUtil() {
	}
	
	/**
	 * 计算首次执行延迟时间,单位毫秒
	 * @param hour 0-23
	 * @param minute 0-59
	 * @param second 0-59
	 * @return
	 */
	public static long calcDelay(int hour, int minute, int second) {
		if(hour == -1 && minute == -1 && second == -1){ //立即执行
			return 0;
		}
		long delay = 0;
		if (!((hour == -1 || (0 <= hour && hour <= 23))
				&& (minute == -1 || (0 <= minute && minute <= 59))
				&& (second == -1 || (0 <= second && second <= 59)))) {
			throw new IllegalArgumentException();
		}
		
		if(hour != -1 && minute != -1 && second != -1){ //按天循环
			delay = calcDelayPerDay(fixed(hour, minute, second));
		}
		
		if(hour == -1 && minute != -1 && second != -1){ //按小时循环
			delay = calcDelayPerHour(fixed(hour, minute, second));
		}
		
		if(hour == -1 && minute == -1 && second != -1){ //按分钟循环
			delay = calcDelayPerMinute(fixed(hour, minute, second));
		}
		return delay;
	}
	
	/**
	 * 准点执行,每天循环,计算首次执行延迟时间
	 * @param targetDatetimeOfToday
	 * @return
	 */
	private static long calcDelayPerDay(DateTime targetDatetimeOfToday) {
		long delay = 0;
		DateTime now = new DateTime();

		// 时间点已过，只好延时到明天的这个时间点再执行
		if (now.isAfter(targetDatetimeOfToday)) {
			delay = targetDatetimeOfToday.plusDays(1).getMillis() - now.getMillis();

		// 时间点未到
		} else {
			delay = targetDatetimeOfToday.getMillis() - now.getMillis();
		}

		return delay;
	}
	
	/**
	 * 准点执行,每小时循环,计算首次执行延迟时间
	 * @param targetDatetimeOfToday
	 * @return
	 */
	private static long calcDelayPerHour(DateTime targetDatetimeOfToday) {
		long delay = 0;
		DateTime now = new DateTime();

		// 时间点已过，只好延时到明天的这个时间点再执行
		if (now.isAfter(targetDatetimeOfToday)) {
			delay = targetDatetimeOfToday.plusHours(1).getMillis() - now.getMillis();

		// 时间点未到
		} else {
			delay = targetDatetimeOfToday.getMillis() - now.getMillis();
		}

		return delay;
	}
	
	/**
	 * 准点执行,每分钟循环,计算首次执行延迟时间
	 * @param targetDatetimeOfToday
	 * @return
	 */
	private static long calcDelayPerMinute(DateTime targetDatetimeOfToday) {
		long delay = 0;
		DateTime now = new DateTime();

		// 时间点已过，只好延时到明天的这个时间点再执行
		if (now.isAfter(targetDatetimeOfToday)) {
			delay = targetDatetimeOfToday.plusMinutes(1).getMillis() - now.getMillis();

		// 时间点未到
		} else {
			delay = targetDatetimeOfToday.getMillis() - now.getMillis();
		}

		return delay;
	}

	/**
	 * 返回这样一个DateTime对象： 1.日期为今天 2.时分秒为参数指定的值
	 * 
	 * @param hour 0-23
	 * @param minute 0-59
	 * @param second 0-59
	 * @return
	 */
	private static DateTime fixed(int hour, int minute, int second) {
		DateTime dateTime = new DateTime();
		
		return dateTime.withHourOfDay(hour==-1?dateTime.getHourOfDay():hour)
				.withMinuteOfHour(minute==-1?dateTime.getMinuteOfHour():minute)
				.withSecondOfMinute(second==-1?dateTime.getSecondOfMinute():second);
	}
	
	/**
     * 计算从当前时间currentDate开始，满足条件dayOfWeek, hourOfDay, 
     * minuteOfHour, secondOfMinite的最近时间
     * 
     * //获取当前时间
        Calendar currentDate = Calendar.getInstance();
        long currentDateLong = currentDate.getTime().getTime();
        System.out.println("Current Date = " + currentDate.getTime().toString());
        //计算满足条件的最近一次执行时间
        Calendar earliestDate = test
                .getEarliestDate(currentDate, 3, 16, 38, 10);
        long earliestDateLong = earliestDate.getTime().getTime();
        System.out.println("Earliest Date = "
                + earliestDate.getTime().toString());
        //计算从当前时间到最近一次执行时间的时间间隔
        long delay = earliestDateLong - currentDateLong;
     * 
     * @param currentDate 当前时间
     * @param dayOfWeek 日
     * @param hourOfDay 时
     * @param minuteOfHour 分
     * @param secondOfMinite 秒
     * @return
     */
	public Calendar getEarliestDate(Calendar currentDate, int dayOfWeek,
            int hourOfDay, int minuteOfHour, int secondOfMinite) {
        //计算当前时间的WEEK_OF_YEAR,DAY_OF_WEEK, HOUR_OF_DAY, MINUTE,SECOND等各个字段值
        int currentWeekOfYear = currentDate.get(Calendar.WEEK_OF_YEAR);
        int currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
        int currentHour = currentDate.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentDate.get(Calendar.MINUTE);
        int currentSecond = currentDate.get(Calendar.SECOND);

        //如果输入条件中的dayOfWeek小于当前日期的dayOfWeek,则WEEK_OF_YEAR需要推迟一周
        boolean weekLater = false;
        if (dayOfWeek < currentDayOfWeek) {
            weekLater = true;
        } else if (dayOfWeek == currentDayOfWeek) {
            //当输入条件与当前日期的dayOfWeek相等时，如果输入条件中的
            //hourOfDay小于当前日期的
            //currentHour，则WEEK_OF_YEAR需要推迟一周    
            if (hourOfDay < currentHour) {
                weekLater = true;
            } else if (hourOfDay == currentHour) {
                 //当输入条件与当前日期的dayOfWeek, hourOfDay相等时，
                 //如果输入条件中的minuteOfHour小于当前日期的
                //currentMinute，则WEEK_OF_YEAR需要推迟一周
                if (minuteOfHour < currentMinute) {
                    weekLater = true;
                } else if (minuteOfHour == currentSecond) {
                     //当输入条件与当前日期的dayOfWeek, hourOfDay， 
                     //minuteOfHour相等时，如果输入条件中的
                    //secondOfMinite小于当前日期的currentSecond，
                    //则WEEK_OF_YEAR需要推迟一周
                    if (secondOfMinite < currentSecond) {
                        weekLater = true;
                    }
                }
            }
        }
        if (weekLater) {
            //设置当前日期中的WEEK_OF_YEAR为当前周推迟一周
            currentDate.set(Calendar.WEEK_OF_YEAR, currentWeekOfYear + 1);
        }
        // 设置当前日期中的DAY_OF_WEEK,HOUR_OF_DAY,MINUTE,SECOND为输入条件中的值。
        currentDate.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        currentDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        currentDate.set(Calendar.MINUTE, minuteOfHour);
        currentDate.set(Calendar.SECOND, secondOfMinite);
        return currentDate;
    }
	
	/** 
	 * 获取指定时间对应的毫秒数 
	 * @param time "HH:mm:ss" 
	 * @return 
	 */  
	private static long getTimeMillis(String time) {  
	    try {  
	        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");  
	        DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");  
	        Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);  
	        return curDate.getTime();  
	    } catch (ParseException e) {  
	        e.printStackTrace();  
	    }  
	    return 0;  
	}
	
	/**
	 * 根据时间字符串获取初始化延迟时间的毫秒数
	 * 按天周期执行
	 * @param timeString 时分秒，格式HH:mm:ss
	 * @return
	 */
	public static long getInitDelay(String timeString, long period){
		long initDelay  = getTimeMillis(timeString) - System.currentTimeMillis();
		return initDelay > 0 ? initDelay : period + initDelay;
	}
}
