package com.d5.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式中分组功能高级用法
 * 通过将部分正则表达式用括号括住来实现分组捕获的用法大部分人都很熟悉，
 * 如/.+(\d+).+/捕获字符串中的所有数字部分，
 * 然后通过组号就可以抽取出各分组匹配的字符文本或者通过反向引用的方式对分组中的文本进行替换。
 * 但是除了不同的分组用法以外，正则表达式引擎还提供了一些高级的分组功能，
 * 下面介绍部分比较常用的特殊分组：
 */
public class RegexHighUseExample {

	public static void main(String[] args) {
		negtiveLookbehandGroup();
		
	}
	//负后向查找分组（Negative lookbehind）
    /**
     * 使用语法：(?<!regex)这里的?和<!都是语法的组成部分；这种分组功能和正负向查找分组一样，
     * 唯一的不同就是当负后向查找分组里面的正则表达式匹配失败的时候才继续后面的匹配过程语言支持说明：java支持，javascript不支持
     */
	public static void negtiveLookbehandGroup() {
		Pattern pattern = Pattern.compile("(\\d+)\\s+(?<!s)(\\w+)");
		String source = "543543   ttreets"; // 如果正则表达式为(\\d+)\\s+(?<!\\s)(\\w+)则匹配失败
		Matcher matcher = pattern.matcher(source);
		if (matcher.matches()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				System.out.println("group " + i + ":" + matcher.group(i));
			}
		}
	}
  	
  	//正后向查找分组（Positive lookbehind）
  	/**
  	 * 使用语法：(?<=regex)这里的?和<=都是语法的组成部分；
  	 * 可以理解成在正后向查找分组前面的正则表达式匹配成功后，
  	 * 正则表达式引擎从最后的位置往字符串左边进行回溯然后和(?<=regex)进行匹配，
  	 * 如果匹配失败则整个匹配过程失败；如果匹配成功，
  	 * 则将指针移动到正后向查找分组开始进行匹配的位置继续进行后面正则表达式的匹配过程。
  	 * 注意这里的正前向查找分组为非捕获型分组即不会占用分组号。例如下面的样例代码中\\s+匹配完数字后面的所有空格后，
  	 * 指针指向s字符，然后进行回溯（?<=\\s）和最后一个空格字符匹配成功,但这个时候指针指向s字符前面的空格，
  	 * 然后正则表达式将指针重新移动到s字符处再进行(\\w+)分组的匹配过程。语言支持说明：java支持，javascript不支持
  	 */
  	public static void positiveLookbehandGroup(){
        Pattern pattern = Pattern.compile("(\\d+)\\s+(?<=\\s)(\\w+)");
        String source = "543543   ttreets";  //"543543   streets" 匹配失败
        Matcher matcher = pattern.matcher(source);
        if(matcher.matches()){
            for(int i=0;i<=matcher.groupCount();i++){
                System.out.println("group "+i+":"+matcher.group(i));
            }
        }
    }
  	
  	//负前向查找分组（Negative lookahead）
  	/**
  	 * 使用语法：(?!regex)这里的?和!都是语法的组成部分；
  	 * 这种分组功能和正前向查找分组一样，
  	 * 唯一的不同就是当前向查找分组里面的正则表达式匹配失败的时候才继续后面的匹配过程语言支持说明：java支持，javascript不支持
  	 */
  	public static void negtiveLookaheadGroup(){
        Pattern pattern = Pattern.compile("(\\d+)\\s+(?!s)(\\w+)");
        String source = "543543   ttreets";  //如"543543   streets" 匹配失败
        Matcher matcher = pattern.matcher(source);
        if(matcher.matches()){
            for(int i=0;i<=matcher.groupCount();i++){
                System.out.println("group "+i+":"+matcher.group(i));
            }
        }
    }
  	
  	//正前向查找分组（Positive lookahead）
  	/**
  	 * 使用语法：(?=regex)这里的?和=都是语法的组成部分；
  	 * 可以理解成在正前向分组里面的表达式匹配成功后，
  	 * 正则表达式引擎回溯到正前向分组开始匹配的字符处再进行后面正则表达式的匹配，
  	 * 如果后面的正则表达式也匹配成功，整个匹配过程才算成功。
  	 * 注意这里的正前向查找分组为非捕获型分组即不会占用分组号
  	 * 例如下面的样例代码中\\s+匹配完数字后面的所有空格后（?=s）和s字符匹配成功,
  	 * 但这个时候指针指向t字符，当进行(\\w+)这个分组匹配时正则表达式引擎会将指针回溯到s处才开始(\\w+)分组的匹配过程。
  	 * 语言支持说明：java支持，javascript不支持
  	 */
  	public static void positvieLookaheadGroup(){
        Pattern pattern = Pattern.compile("(\\d+)\\s+(?=s)(\\w+)");
        String source = "543543   streets";        //"543543   ttreets" 匹配失败
        Matcher matcher = pattern.matcher(source);
        if(matcher.matches()){
            for(int i=0;i<=matcher.groupCount();i++){
                System.out.println("group "+i+":"+matcher.group(i));
            }
        }
    }
  	
  	//原子分组（Atomic group）
  	/**
  	 * 使用语法：(?>regex)这里的?和>都是语法的组成部分；
  	 * 原子分组是贪婪的匹配，当文本和这个分组匹配的成功后，
  	 * 正则表达式引擎在匹配后面的表达式时不会发生回溯行为及尽可能多的匹配，
  	 * 注意这个分组的贪婪行为和++这种贪婪匹配略有不同，
  	 * ++只能对正则表达式字符进行多次贪婪匹配如(bc|b)是没办法利用++进行贪婪匹配的而(\w++)可以，
  	 * 如下面代码中的正则表达式如果换成：(\\d+)\\s+(\\w++)(\\w)匹配则会失败。
  	 * 语言支持说明：java支持，javascript不支持
  	 */
  	public static void atomicGroup(){
        Pattern pattern = Pattern.compile("(\\d+)\\s+(?>bc|b)(\\w)");
        String source = "543543   bcc";  //而“543543   bc” 却匹配失败因为bc已经被原子分组匹配了，当(\\w)进行匹配的时候前面的分组由于是贪婪型匹配所以不会突出以匹配的字符
        Matcher matcher = pattern.matcher(source);
        if(matcher.matches()){
            for(int i=0;i<=matcher.groupCount();i++){
                System.out.println("group "+i+":"+matcher.group(i));
            }
        }
    }
  	
  	//非捕获分组（ non-capturing group）
  	/**
  	 * 使用语法：(?:regex)这里的?和:都是语法的组成部分；
  	 * 这种分组正则表达式引擎不会捕获它所匹配的内容即不会为非捕获型分组分配组号；
  	 * 样例说明：Set(?:Value)?表达式匹配SetValue或者Set，
  	 * 但是不能通过group(1)的方式获取Value文本串，Set(Value)?则可以获取的到
  	 * 语言支持说明：java和javascript都支持
  	 */
  	public static void noCaptureGroup(){
        Pattern pattern = Pattern.compile("(?:(\\d+))?\\s?([a-zA-Z]+)?.+");
        String source = "2133 fdsdee4333";
        Matcher matcher = pattern.matcher(source);
        if(matcher.matches()){
            for(int i=0;i<=matcher.groupCount();i++){
                System.out.println("group "+i+":"+matcher.group(i));
            }
        }
    }
}
