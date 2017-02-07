package com.d5.selenium;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.util.CommonUtil;

/**
 * @author code4crafter@gmail.com <br>
 *         Date: 13-7-26 <br>
 *         Time: 下午1:41 <br>
 */
class WebDriverPool {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final static int DEFAULT_CAPACITY = 5;

	private final int capacity;

	private final static int STAT_RUNNING = 1;

	private final static int STAT_CLODED = 2;

	private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

	/*
	 * new fields for configuring phantomJS
	 */
	private WebDriver mDriver = null;
	private boolean mAutoQuitDriver = false;

	private static final String CONFIG_FILE = System.getProperty("CRAWLER_CONF_HOME") + System.getProperty("file.separator") + "conf"
			+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "config_selenium.ini";
	private static final String DRIVER_FIREFOX = "firefox";
	private static final String DRIVER_CHROME = "chrome";
	private static final String DRIVER_PHANTOMJS = "phantomjs";

	protected static Properties sConfig;
	protected static DesiredCapabilities sCaps;

	/**
	 * Configure the GhostDriver, and initialize a WebDriver instance. This part
	 * of code comes from GhostDriver.
	 * https://github.com/detro/ghostdriver/tree/master/test/java/src/test/java/ghostdriver
	 * 
	 * @author bob.li.0718@gmail.com
	 * @throws IOException
	 */
	public void configure() throws IOException {
		// Read config file
		sConfig = new Properties();
		sConfig.load(new FileReader(CONFIG_FILE));

		// Prepare capabilities
		sCaps = new DesiredCapabilities();
		sCaps.setJavascriptEnabled(true);
		sCaps.setCapability("takesScreenshot", false);

		String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);

		// Fetch PhantomJS-specific configuration parameters
		if (driver.equals(DRIVER_PHANTOMJS)) {
			mAutoQuitDriver = true;
			
			logger.debug("Fetch PhantomJS-specific configuration parameters");
			// "phantomjs_exec_path"
			if (sConfig.getProperty("phantomjs_exec_path") != null) {
				sCaps.setCapability(
						PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
						sConfig.getProperty("phantomjs_exec_path"));
			} else {
				throw new IOException(
						String.format(
								"Property '%s' not set!",
								PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY));
			}
			// "phantomjs_driver_path"
			if (sConfig.getProperty("phantomjs_driver_path") != null) {
				System.out.println("Test will use an external GhostDriver");
				sCaps.setCapability(
						PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY,
						sConfig.getProperty("phantomjs_driver_path"));
			} else {
				System.out
						.println("Test will use PhantomJS internal GhostDriver");
			}
			
			
			
			// Disable "web-security", enable all possible "ssl-protocols" and
			// "ignore-ssl-errors" for PhantomJSDriver
			/*sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
					new String[] { "--web-security=false", "--ssl-protocol=any", "--ignore-ssl-errors=true" });*/
			
			ArrayList<String> cliArgsCap = new ArrayList<String>();
			cliArgsCap.add("--web-security=false");
			cliArgsCap.add("--ssl-protocol=any");
			cliArgsCap.add("--ignore-ssl-errors=true");
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
					cliArgsCap);

			// Control LogLevel for GhostDriver, via CLI arguments
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS,
					new String[] { "--logLevel=" + (sConfig.getProperty("phantomjs_driver_loglevel") != null
							? sConfig.getProperty("phantomjs_driver_loglevel") : "INFO") });
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
					new String[] { "--webdriver-loglevel=" +  (sConfig.getProperty("phantomjs_webdriver_loglevel") != null
					? sConfig.getProperty("phantomjs_webdriver_loglevel") : "NONE") });//NONE,ERROR
		} else {
			for(String pName : sConfig.stringPropertyNames()){
				if(pName.startsWith("webdriver")){
					System.getProperties().setProperty(pName, sConfig.getProperty(pName));
				}
			}
		}

		// Start appropriate Driver
		//TODO
//		if (isUrl(driver)) {
//			logger.debug("Start appropriate Driver");
//			//DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
//			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36");
//			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
//			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
//			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "resourceTimeout", 60 * 100);
//			
//			//sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//			
//			sCaps.setBrowserName("Chrome");
//			mDriver = new RemoteWebDriver(new URL(driver), sCaps);
//		} else if (driver.equals(DRIVER_FIREFOX)) {
		
		if (driver.equals(DRIVER_FIREFOX)) {
			mDriver = new FirefoxDriver(sCaps);
		} else if (driver.equals(DRIVER_CHROME)) {
			mDriver = new ChromeDriver(sCaps);
		} else if (driver.equals(DRIVER_PHANTOMJS)) {
			logger.debug("generate PhantomJSDriver object.");
			//DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36");
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "resourceTimeout", 60 * 100);
			//sCaps.setBrowserName("Chrome");
			/*LoggingPreferences lPre = new LoggingPreferences();
			lPre.enable(LogType.BROWSER, Level.FINE);
			sCaps.setCapability(CapabilityType.LOGGING_PREFS, lPre);*/
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			mDriver = new PhantomJSDriver(sCaps);
		}
	}

	/**
	 * check whether input is a valid URL
	 * 
	 * @author bob.li.0718@gmail.com
	 * @param urlString urlString
	 * @return true means yes, otherwise no.
	 */
	private boolean isUrl(String urlString) {
		try {
			new URL(urlString);
			return true;
		} catch (MalformedURLException mue) {
			return false;
		}
	}

	/**
	 * store webDrivers created
	 */
	private List<WebDriver> webDriverList = Collections
			.synchronizedList(new ArrayList<WebDriver>());

	/**
	 * store webDrivers available
	 */
	private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();

	public WebDriverPool(int capacity) {
		this.capacity = capacity;
	}

	public WebDriverPool() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public WebDriver get() throws InterruptedException {
		checkRunning();
		WebDriver poll = innerQueue.poll();
		if (poll != null) {
			return poll;
		}
		if (webDriverList.size() < capacity) {
			synchronized (webDriverList) {
				if (webDriverList.size() < capacity) {

					// add new WebDriver instance into pool
					try {
						configure();
						innerQueue.add(mDriver);
						webDriverList.add(mDriver);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return innerQueue.take();
	}

	public void returnToPool(WebDriver webDriver) {
		checkRunning();
		innerQueue.add(webDriver);
	}

	protected void checkRunning() {
		if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
			throw new IllegalStateException("Already closed!");
		}
	}

	public void closeAll() {
		boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
		if (!b) {
			throw new IllegalStateException("Already closed!");
		}
		for (WebDriver webDriver : webDriverList) {
			logger.info("Quit webDriver {}", webDriver);
			webDriver.quit();
			webDriver = null;
		}
	}

}
