package com.d5.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.Request;

public class CommonUtil {
	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);
	
	public static String getHome() {
    	String home = System.getProperty("CRAWLER_CONF_HOME");
        home = home == null?System.getProperty("user.dir"):home;
		return home;
	}
	
	/**
	 * 
	 * @param fSt
	 * @return
	 */
	public static String getProgramName(){
		String programName = System.getProperty("PROGRAM_NAME");
		if(programName != null && programName.trim().length() > 0){
			return System.getProperty("file.separator") + programName;
		}

		return "";
	}
	
	/**
	 * 时间字符串转换
	 * @param dateStr 时间字符串
	 * @return
	 */
	public static String convertDate(String dateStr){
		if(dateStr.length() < 1){
			return dateStr;
		}
		dateStr = dateStr.trim().replaceAll("/", "-");
		String num = "0";
		Calendar calendar = Calendar.getInstance();
		Date date = null;
		String pattern = "";
		String timeStr = "";
		SimpleDateFormat sdf = null;
		if(hasChinesWord(dateStr)){
			if(isChinaDateString(dateStr)){
				try {
					dateStr = fixChinaDateStr(dateStr);
				} catch (Exception e) {
					log.error("dateStr=" + dateStr);
					e.printStackTrace();
				}
			} else {
				dateStr = dateStr.replaceAll("[\\s,\\u00A0,\\t,&nbsp;]+", "");
				
				if(dateStr.indexOf("天前") != -1){
					pattern = "yyyy-MM-dd";
					num = dateStr.substring(0,dateStr.length()-2);
					calendar.add(Calendar.DAY_OF_YEAR, 0 - Integer.parseInt(num));
					date = calendar.getTime();
				}else if(dateStr.indexOf("小时前") != -1){
					pattern = "yyyy-MM-dd HH";
					num = dateStr.substring(0,dateStr.length()-3);
					if(num.contains("半")){
						calendar.add(Calendar.MINUTE,-30);
					}else{
						calendar.add(Calendar.HOUR_OF_DAY, 0 - Integer.parseInt(num));
					}
					date = calendar.getTime();
				}else if(dateStr.indexOf("分钟前") != -1){
					num = dateStr.substring(0,dateStr.length()-3);
					calendar.add(Calendar.MINUTE,0 - Integer.parseInt(num));
					date = calendar.getTime();
				}else if(dateStr.indexOf("秒前") != -1){
					num = dateStr.substring(0,dateStr.length()-2);
					calendar.add(Calendar.SECOND,0 - Integer.parseInt(num));
					date = calendar.getTime();
				}else if(dateStr.indexOf("前天") != -1){
					pattern = "yyyy-MM-dd";
					calendar.add(Calendar.DAY_OF_YEAR,-2);
					date = calendar.getTime();
					timeStr = dateStr.substring(2);
					timeStr = timeStr.length() > 0?(" "+timeStr):timeStr;
				}else if(dateStr.indexOf("昨天") != -1){
					pattern = "yyyy-MM-dd";
					calendar.add(Calendar.DAY_OF_YEAR,-1);
					date = calendar.getTime();
					timeStr = dateStr.substring(2);
					timeStr = timeStr.length() > 0?(" "+timeStr):timeStr;
				}else if(dateStr.indexOf("今天") != -1){
					pattern = "yyyy-MM-dd";
					date = calendar.getTime();
					timeStr = dateStr.substring(2);
					timeStr = timeStr.length() > 0?(" "+timeStr):timeStr;
				}else if(dateStr.indexOf("刚刚") != -1){
					pattern = "yyyy-MM-dd HH:mm:ss";
					calendar.add(Calendar.SECOND, -5);
					date = calendar.getTime();
					timeStr = dateStr.substring(2);
					timeStr = timeStr.length() > 0?(" "+timeStr):timeStr;
				}
				sdf = new SimpleDateFormat(pattern);
				dateStr = sdf.format(date) + timeStr;
			}
		}else{
			try {
				if(!isDateString(dateStr)){
					String temp = dateStr;
					dateStr = fixDateStr(dateStr);
					if(temp.equals(dateStr)) {
						pattern = "yyyy-MM-dd HH:mm:ss";
						dateStr = fixChinaDateStr(temp);
					}
				}
			} catch (Exception e) {
				log.error("dateStr=" + dateStr);
				e.printStackTrace();
			}
		}
		String nowDate = dateStr;
		return fixTimePart(nowDate);
	}
	/**
	 * 检测字符串是否是时间格式的字符串
	 * @param str 时间字符串
	 */
	public static boolean isDateString(String str){
		str = str.replaceAll("/", "-");
		StringBuffer patterStr = new StringBuffer();
		//yyyy-MM-dd HH:mm:ss
		patterStr.append("[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) ([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])");
		//yyyy-MM-dd HH:mm
		patterStr.append("|[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) ([0-1][0-9]|2[0-3]):([0-5][0-9])");
		//yyyy-MM-dd HH
		patterStr.append("|[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) ([0-1][0-9]|2[0-3])");
		//yyyy-MM-dd
		patterStr.append("|[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])");
		Pattern datePattern = Pattern.compile(patterStr.toString());
		Matcher dateMatcher = datePattern.matcher(str);
		return dateMatcher.find();
	}
	
	/**
	 * 判断字符串是否是中文时间串
	 * @param str
	 * @return
	 */
	public static boolean isChinaDateString(String str) {
		StringBuffer patterStr = new StringBuffer();
		//MM月dd日 HH:mm
		patterStr.append("\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{2}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{2}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{4}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{4}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}:\\d{1,2}");
		patterStr.append("|\\d{2}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}时\\d{1,2}分\\d{1,2}秒");
		patterStr.append("|\\d{2}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}时\\d{1,2}分");
		patterStr.append("|\\d{4}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}时\\d{1,2}分\\d{1,2}秒");
		patterStr.append("|\\d{4}年\\d{1,2}月\\d{1,2}日 ?\\d{1,2}时\\d{1,2}分");
		patterStr.append("|\\d{4}年\\d{1,2}月\\d{1,2}日");
		patterStr.append("|\\d{2}年\\d{1,2}月\\d{1,2}日");
		Pattern datePattern = Pattern.compile(patterStr.toString());
		Matcher dateMatcher = datePattern.matcher(str);
		return dateMatcher.matches();
	}
	
	/**
	 * 检测字符串中是否含有汉字
	 * @param words
	 * @return
	 */
	private static boolean hasChinesWord(String words) {
		//words = words.replaceAll("\t|\r|\n|\\s*|\\S*", "");
		StringBuffer patterStr = new StringBuffer();
		//yyyy-MM-dd HH:mm:ss
		patterStr.append("[\u4e00-\u9fa5]");
		Pattern datePattern = Pattern.compile(patterStr.toString());
		Matcher dateMatcher = datePattern.matcher(words);
		return dateMatcher.find();
	}
	
	/**
	 * 根据当前年字符串，和当前月的大小，得出正确的当前年
	 * @param str
	 * @return
	 */
	private static String getCurYear(String str, String mon){
		String r = str;
		int monI = Integer.valueOf(mon);
		int year = Integer.valueOf(str);
		int m = Integer.valueOf(DateTime.now().toString("MM"));
		if(monI >= 10 && m < 10){
			year = year - 1;
		}
		
		if(monI>DateTime.now().getMonthOfYear()){
			year = year - 1;
		}
		r = String.valueOf(year);
		return r;
	}
	
	/**
	 * 修复时间字符串。只能补全字符串，匹配以下格式：
	 * MM月dd日 HH:mm
	 * 修复后字符串格式为：yyyy-MM-dd HH:mm
	 * @param str 时间字符串
	 */
	private static String fixChinaDateStr(String str){
		int dayLen = getDayLength(str);
		str = str.replaceAll("日|秒", "").replaceAll("年|月", "-").replaceAll("时|分", ":");
		int spaceIndex = str.indexOf(" ");
		String[] dateTime = null;
		String[] date = null;
		String[] time = null;
		String cuurentYear = DateTime.now().toString("yyyy");
		boolean haveTime = true;
		if(spaceIndex > -1){
			dateTime = str.split(" ");
			date = dateTime[0].split("-");
			if(date.length == 2){
				date = (getCurYear(cuurentYear, dateTime[0].split("-")[0])
					+ "-" + dateTime[0]).split("-");
			}
			time = dateTime[1].split(":");
		}else{
			int pos = str.lastIndexOf("-") + dayLen;
			if((pos + 1) == str.length()) {
				date = str.split("-");
				haveTime = false;
			} else {
				date = str.substring(0, (pos + 1)).split("-");
				time = str.substring((pos + 1)).split(":");
			}
			
		}
		
		for(int i=0; i<date.length; i++){
			String temp = date[i];
			if(date.length>2 && i == 0){
				date[i] = temp.length() == 4 ? temp : (cuurentYear.substring(0, 2) + temp);
				continue;
			}
			date[i] = temp.length() > 1 ? temp : ("0" + temp);
		}
		if(haveTime){
			for(int j=0; j<time.length; j++){
				String temp = time[j];
				time[j] = temp.length() > 1 ? temp : ("0" + temp);
			}
			if(time.length < 3){
				String[] temp = Arrays.copyOf(time, 3);
				for(int m=time.length;m < 3; m++){
					temp[m] = "00";
				}
				
				time = temp;
			}
		}
		
		if(haveTime){
			return converArray2String(date, "-") + " " + converArray2String(time, ":");
		}else{
			return converArray2String(date, "-");
		}
	}
	
	private static int getDayLength(String str){
		return (str.lastIndexOf("日") -1) - str.lastIndexOf("月");
	}
	
	/**
	 * 修复时间字符串。只能补全字符串，匹配以下格式：
	 * yyyy-MM-dd HH:mm:ss
	 * yyyy-MM-dd HH:mm
	 * yyyy-MM-dd HH
	 * yyyy-MM-dd
	 * @param str 时间字符串
	 */
	private static String fixDateStr(String str){
		str = str.replaceAll("/", "-");
		int spaceIndex = str.indexOf(" ");
		String[] dateTime = null;
		String[] date = null;
		String[] time = null;
		if(spaceIndex > -1){
			dateTime = str.split(" ");
			date = dateTime[0].split("-");
			time = dateTime[1].split(":");
		}else{
			date = str.split("-");
		}
		
		for(int i=1; i<date.length; i++){
			String temp = date[i];
			date[i] = temp.length()>1?temp:("0"+temp);
		}
		if(spaceIndex > -1){
			for(int j=0; j<time.length; j++){
				String temp = time[j];
				time[j] = temp.length()>1?temp:("0"+temp);
			}
		}
		
		if(spaceIndex > -1){
			return converArray2String(date, "-") + " " + converArray2String(time, ":");
		}else{
			return converArray2String(date, "-");
		}
	}
	
	/**
	 * 修复时间字符串。只能补全字符串，匹配以下格式：
	 * yyyy-MM-dd HH:mm:ss
	 * yyyy-MM-dd HH:mm:00
	 * yyyy-MM-dd HH:00:00
	 * yyyy-MM-dd 00:00:00
	 * @param str 时间字符串
	 */
	private static String fixTimePart(String str){
		str = str.replaceAll("/", "-");
		int spaceIndex = str.indexOf(" ");
		String[] dateTime = null;
		String date = null;
		String[] time = new String[3];
		String[] time_ = null;
		if(spaceIndex > -1){
			dateTime = str.split(" ");
			date = dateTime[0];
			time_ = dateTime[1].split(":");
		} else {
			date = str;
		}
		
		for(int j=1; j<4; j++){
			if(time_==null || j>time_.length){
				time[j-1] = "00";
			} else {
				String temp = time_[j-1];
				time[j-1] = temp.length()>1?temp:("0"+temp);
			}
		}
		
		return date + " " + converArray2String(time, ":");
	}
	
	/**
	 * 将数组用连接符号转换成字符串
	 * @param arr 数组
	 * @param joinStr 连接符号
	 * @return
	 */
	private static String converArray2String(String[] arr, String joinStr){
		StringBuffer buffer = new StringBuffer();
		int offset = arr.length - 1;
		for( int i = 0; i < arr.length; i++ )
		{
			buffer.append(arr[i]).append(offset==i?"":joinStr);
		}
		
		return buffer.toString();
	}
	
	/***
	 * Get location of element in a array
	 * @param arr : a array
	 * @param value : element of array
	 * @return
	 */
	public static int indexOfArr(String[] arr,String value){
		if(arr == null || arr.length <  1){
			return -1;
		}
		for(int i=0;i<arr.length;i++){
			if(arr[i].equals(value)){
				return i;
			}else{//做了容错,不是完全匹配
				if(value.startsWith(arr[i])){
					return i;
				}
			}
		}
		return -1;
	}
	
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
	
	public static void addInitData2Request(Request request, JSONObject siteInfo, JSONObject jsonObj){
		Object obj = request.getExtra(Request.DATAS);
		String dataStr = obj==null?"":String.valueOf(obj);
		if(StringUtils.isBlank(dataStr)){//种子
			JSONObject info = JSONObject.parseObject("{}");
			JSONObject initData = siteInfo == null? JSONObject.parseObject("{}") : (JSONObject) siteInfo.clone();
			if(jsonObj != null){
				initData.putAll(jsonObj);
			}
	        info.put(Enums.JsonColums.InitData.toString(), initData.toString());
	        request.putExtra(Request.DATAS, info.toString());
		}
	}
	
	public static void main(String args[]){
		testTime();
	}
	
	public static void testTime(){
		System.out.println("刚刚，转换后的时间为：" + convertDate("刚刚"));//刚刚
		System.out.println("今天 09:09，转换后的时间为：" + convertDate("今天 09:09"));
		System.out.println("今天 09:09:03，转换后的时间为：" + convertDate("今天 09:09:03"));
		System.out.println("半小时前，转换后的时间为：" + convertDate("半小时前"));
		System.out.println("5 小时前，转换后的时间为：" + convertDate("5 小时前"));
		System.out.println("5秒前，转换后的时间为：" + convertDate("5秒前"));
		System.out.println("5 秒前，转换后的时间为：" + convertDate("5 秒前"));
		System.out.println("2小时前，转换后的时间为：" + convertDate("2小时前"));
		System.out.println("5 分钟前，转换后的时间为：" + convertDate("5 分钟前"));
		System.out.println("52分钟前，转换后的时间为：" + convertDate("52分钟前"));
		System.out.println("5 天前，转换后的时间为：" + convertDate("5 天前"));
		System.out.println("前天，转换后的时间为：" + convertDate("前天"));
		System.out.println("前天23:11，转换后的时间为：" + convertDate("前天23:11"));
		System.out.println("前天23:11:32，转换后的时间为：" + convertDate("前天23:11:32"));
		System.out.println("前天 03:11，转换后的时间为：" + convertDate("前天  03:11"));
		System.out.println("昨天，转换后的时间为：" + convertDate("昨天"));
		System.out.println("昨天21:11，转换后的时间为：" + convertDate("昨天21:11"));
		System.out.println("昨天 11:11，转换后的时间为：" + convertDate("昨天 11:11"));
		System.out.println("昨天 11:11:23，转换后的时间为：" + convertDate("昨天 11:11:23"));
		System.out.println("2015-9-2，转换后的时间为：" + convertDate("2015-9-2"));
		System.out.println("2015-9-12 12:33，转换后的时间为：" + convertDate("2015-9-12 12:33"));
		System.out.println("2015-09-02，转换后的时间为：" + convertDate("2015-9-2"));
		System.out.println("2014/12/26 1:37:56，转换后的时间为：" + convertDate("2014/12/26 1:37:56"));
		System.out.println("2014-12-26 1:37:56，转换后的时间为：" + convertDate("2014-12-26 1:37:56"));
		System.out.println("08月12日 06:41，转换后的时间为：" + convertDate("08月12日 06:41"));
		System.out.println("04年08月12日 06时41分22秒，转换后的时间为：" + convertDate("04年08月12日 06时41分22秒"));
		System.out.println("2504年08月12日 06时41分22秒，转换后的时间为：" + convertDate("2504年08月12日 06时41分22秒"));
		System.out.println("08月12日 06:41:22，转换后的时间为：" + convertDate("08月12日 06:41:22"));
		System.out.println("04年08月12日 06:41:22，转换后的时间为：" + convertDate("04年08月12日 06:41:22"));
		System.out.println("3004年08月12日 06:41:22，转换后的时间为：" + convertDate("3004年08月12日 06:41:22"));
		System.out.println("2016年06月12日11:08，转换后的时间为：" + convertDate("2016年06月12日11:08"));
		System.out.println("2014-12-26 01:37:56，转换后的时间为：" + convertDate("2014-12-26 01:37:56"));
		System.out.println("2015年7月16日，转换后的时间为：" + convertDate("2015年7月16日"));
		System.out.println("3999年1月1日，转换后的时间为：" + convertDate("3999年1月1日"));
		System.out.println("8-12，转换后的时间为：" + convertDate("8-12"));
	}

	public static void fillDataIntoJsonObject(JSONObject siteInfo, Map<String, String> fieldsmap, Document data) {
		// TODO Auto-generated method stub
		
	}

	/**
     * Returns {@code true} if the arguments are equal to each other
     * and {@code false} otherwise.
     * Consequently, if both arguments are {@code null}, {@code true}
     * is returned and if exactly one argument is {@code null}, {@code
     * false} is returned.  Otherwise, equality is determined by using
     * the {@link Object#equals equals} method of the first
     * argument.
     *
     * @param a an object
     * @param b an object to be compared with {@code a} for equality
     * @return {@code true} if the arguments are equal to each other
     * and {@code false} otherwise
     * @see Object#equals(Object)
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    
    
    public static Map<String, String> initFilterScriptMap(){
        Map<String, String> filterScriptMap = new HashMap<String, String>();
        
		String scriptsPath = CommonUtil.getHome()
				+ System.getProperty("file.separator") + "conf"
				+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "scripts";
		log.info("scriptsPath:" + scriptsPath);
        String filter = StringUtils.join(".", Constants.PARSER_CONF_FILE_TYPE);
        Collection<File> scriptFiles = FileUtils.listFiles(new File(scriptsPath), new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        }, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        });
        for(File f : scriptFiles){
            if(!f.getAbsolutePath().endsWith(filter)){
                continue;
            }
            try {
				String fContent = FileUtils.readFileToString(f);
				readPropertitiesFile(filterScriptMap, fContent);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("错误，读取配置文件错误。文件名称为：" + f.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("错误，读取配置文件错误。文件名称为：" + f.getAbsolutePath());
			}
        }
        return filterScriptMap;
    }
    
	public static Map<String, String> initAttrRelaMap() {
		Map<String, String> relaMap = new HashMap<String, String>();
		List<Document> relaList = MongodbUtil.findAllDocuments(Constants.ATTR_RELA_COLLECT);
		for(Document rela : relaList){
			relaMap.put(rela.getString("chineseName"), rela.getString("englishName"));
		}
		return relaMap;
	}

	private static void readPropertitiesFile(Map<String, String> filterScriptMap, String fContent) throws Exception{
    	if("json".equals(Constants.PARSER_CONF_FILE_TYPE)){
            JSONObject fObj = JSONObject.parseObject(fContent);
            filterScriptMap.put(fObj.getString("pattern"), fContent);
    	}
    	
    	if("xml".equals(Constants.PARSER_CONF_FILE_TYPE)){
    		filterScriptMap.put(Dom4jUtil.getRootElement(fContent).elementTextTrim("pattern"), fContent);
    	}
    }
}
