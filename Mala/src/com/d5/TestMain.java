package com.d5;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.selenium.SeleniumDownloader;
import com.d5.service.crawler.Task;
import com.d5.util.MatchUtil;

public class TestMain {
	static { //设置环境变量
        //设置日志级别
        System.setProperty(
                "LOG_LEVEL",
                System.getenv("LOG_LEVEL") == null ? "DEBUG" : System
                        .getenv("LOG_LEVEL"));

        // 爬虫程序主目录，日志文件中使用。
        System.setProperty(
                "CRAWLER_HOME",
                System.getenv("CRAWLER_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_HOME"));

        // 爬虫程序配置文件主目录，配置文件路径获取时使用。
        System.setProperty(
                "CRAWLER_CONF_HOME",
                System.getenv("CRAWLER_CONF_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_CONF_HOME"));
        
        // 爬虫程序浏览器主目录。
        System.setProperty(
                "CRAWLER_BROWER_HOME",
                System.getenv("CRAWLER_BROWER_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_BROWER_HOME"));

        /**
         * 项目程序英文别名。如，团购->group_purchase。配置文件路径获取时使用。 默认是""。
         */
        if (System.getenv("PROGRAM_NAME") == null) {
            System.setProperty("PROGRAM_NAME", "");
            System.setProperty("LOG_SUB_DIR", "");
        } else {
			/*
			 * 日志文件路径配置时使用。 根据PROGRAM_NAME来设置具体的值。
			 */
            System.setProperty(
                    "LOG_SUB_DIR",
                    System.getProperty("file.separator")
                            + System.getenv("PROGRAM_NAME"));
            System.setProperty("PROGRAM_NAME", System.getenv("PROGRAM_NAME"));
        }

    }

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
    	
    	SeleniumDownloader sDownloader = new SeleniumDownloader(
    			/*"d:\\geckodriver32.exe",
    			"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"*/).setSleepTime(6 * 1000);
    	String url = "http://sports.163.com/16/1126/08/C6PMEDMV0005877U.html";
		String html = sDownloader.download(new Request(url), new Task(){
			@Override
			public String getUUID() {
				return null;
			}

			@Override
			public Site getSite() {
				return null;
			}
    		
    	}).getHtml();
		log.info("html is:{}", html);
		Document doc  = Jsoup.parse(html);
		log.info(doc.select("title").text());
		try {
			sDownloader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	/*try {
			Element doc = Dom4jUtil.getRootElement(FileUtils.readFileToString(new File("C:\\Users\\Administrator\\Desktop\\1.xml")));
			
			String formatter = doc.element("formatter").elementTextTrim("template");
			List<String> calExpressions = MatchUtil.findAll(formatter, "\\{&\\w+=:\\?([\\*\\+-/]\\d+)+\\}");
			for(String calExpression : calExpressions){
				System.out.println(MatchUtil.find(calExpression, "&\\w+=", ""));
				System.out.println(MatchUtil.find(calExpression, "\\?([\\*\\+-/]\\d+)+", ""));
				System.out.println(MatchUtil.find(calExpression, "([\\*\\+-/]\\d+)+", ""));
			}
    		//System.out.println(CommonUtil.generateShortUuid(DigestUtils.md5Hex("movie.douban.com")));
			//System.out.println(MatchUtil.find("http://xxx.ddd.cc/fwefe?fwef=fww&pagenum=15", "&pagenum=(\\d+)", ""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
