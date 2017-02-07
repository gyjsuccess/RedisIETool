package com.d5.crawler;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeCrawler {
	private static WebDriver webDriver;
	
	public static synchronized String download(String url){
		String html = "";
		if(webDriver == null){
			webDriver = new ChromeDriver();
		}
		webDriver.get(url);
		int inx = 0;
		while(StringUtils.isBlank(html)){
			inx++;
			try {
				new Thread().sleep(3 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			html = webDriver.getPageSource();
			if(inx >= 5){
				html = "";
				break;
			}
		}
		return html;
	}
	
	public static void close(){
		webDriver.close();
		webDriver.quit();
	}
}
