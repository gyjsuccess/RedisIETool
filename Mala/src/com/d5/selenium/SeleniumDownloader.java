package com.d5.selenium;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.crawler.Page;
import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.service.crawler.ICrawler;
import com.d5.service.crawler.Task;

/**
 * 使用Selenium调用浏览器进行渲染。目前仅支持chrome。<br>
 * 需要下载Selenium driver支持。<br>
 *
 * @author code4crafter@gmail.com <br>
 *         Date: 13-7-26 <br>
 *         Time: 下午1:37 <br>
 */
public class SeleniumDownloader implements ICrawler, Closeable {

	private volatile WebDriverPool webDriverPool;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private int sleepTime = 0;

	private int poolSize = 1;

	/**
	 * Constructor without any filed. Construct PhantomJS browser
	 * 
	 * @author bob.li.0718@gmail.com
	 */
	public SeleniumDownloader() {
	}

	/**
	 * set sleep time to wait until load success
	 *
	 * @param sleepTime
	 *            sleepTime
	 * @return this
	 */
	public SeleniumDownloader setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}

	@Override
	public Page download(Request request, Task task) {
		checkInit();
		WebDriver webDriver;
		try {
			webDriver = webDriverPool.get();
		} catch (InterruptedException e) {
			logger.warn("interrupted", e);
			return null;
		}
		logger.info("downloading page {}", request.getUrl());
		webDriver.get(request.getUrl());
		logger.debug("等待下载开始...");
		try {
			WebDriverWait wait = new WebDriverWait(webDriver, sleepTime);
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver webDriver) {
					//return webDriver.findElements(By.tagName("html")).size() > 0;
					return((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"); 
				}
			});
			//Thread.currentThread().sleep(sleepTime);
		} catch (Exception e) {
			logger.error("下载页面时出错，错误信息如下：{}", e);
		}
		logger.debug("等待下载结束.");
		WebDriver.Options manage = webDriver.manage();
		Site site = task.getSite();
		if (site != null && site.getCookies() != null) {
			for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
				Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
				manage.addCookie(cookie);
			}
		}

		logger.debug("获取内容开始...");
		webDriver.getWindowHandle();
		WebElement webElement = webDriver.findElement(By.xpath("/html"));
		String content = webElement.getAttribute("outerHTML");
		webDriverPool.returnToPool(webDriver);
		logger.debug("content length is :{}", content.length());
		Page page = new Page();
		page.setRawText(content);
		page.setRequest(request);
		page.setStatusCode(200);
		logger.debug("获取内容结束.");
		return page;
	}

	private void checkInit() {
		if (webDriverPool == null) {
			synchronized (this) {
				webDriverPool = new WebDriverPool(poolSize);
			}
		}
	}

	@Override
	public void setThread(int thread) {
		this.poolSize = thread;
	}

	@Override
	public void close() throws IOException {
		webDriverPool.closeAll();
	}
}
