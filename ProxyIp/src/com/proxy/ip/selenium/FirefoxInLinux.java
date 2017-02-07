package com.proxy.ip.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FirefoxInLinux {

	public static void main(String[] args) {
		String driverPath = "/home/crawler/user_files/geckodriver";
		String binPath = "/usr/bin/firefox";
		
		driverPath = "d:\\geckodriver.exe";
		binPath = "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe";
		System.getProperties().setProperty("webdriver.gecko.driver", driverPath);
		System.getProperties().setProperty("webdriver.firefox.bin", binPath);
		
		driverPath = "/home/crawler/user_files/ghostdriver/main.js";
		binPath = "/home/crawler/user_files/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
		
		System.getProperties().setProperty(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY, driverPath);
		System.getProperties().setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, binPath);
		WebDriver webDriver = new PhantomJSDriver();
		String url = args[0];
		
		if(StringUtils.isBlank(url)){
			return;
		}
		
		webDriver.get(url);
		int sleepTime = 100 * 100;
		try {
			WebDriverWait wait = new WebDriverWait(webDriver, sleepTime);
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver webDriver) {
					//return webDriver.findElements(By.tagName("html")).size() > 0;
					return((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"); 
				}
			});
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		WebElement webElement = webDriver.findElement(By.xpath("/html"));
		String content = webElement.getAttribute("outerHTML");
		System.out.println(content);
		webDriver.close();
	}

}
