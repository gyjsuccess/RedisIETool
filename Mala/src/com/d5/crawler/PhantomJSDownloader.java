package com.d5.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.service.crawler.Task;

/**
 * this downloader is used to download pages which need to render the javascript
 *
 * @author dolphineor@gmail.com
 * @version 0.5.3
 */
@ThreadSafe
public class PhantomJSDownloader extends AbstractCrawler {

    private Logger logger = LoggerFactory.getLogger(PhantomJSDownloader.class);
    private String phantomJSPath;
    private String phantomJSBinPath;

    private int retryNum = 3;
    private int sleepTime = 0 * 1000;
    private int retrySleepTime = 10 * 1000;
    //private int threadNum;
	private String phantomJSParams;

    public PhantomJSDownloader() {
		this.phantomJSPath = Constants.PHANTOMJS_JS_PATH;
		this.phantomJSBinPath = Constants.PHANTOMJS_BIN_PATH;
		this.phantomJSParams = Constants.PHANTOMJS_PARAMS;
    }

    @Override
    public Page download(Request request, Task task) {
        if (logger.isInfoEnabled()) {
            logger.info("downloading page: " + request.getUrl());
        }
        String content = getPage(request);
        if (content.contains("HTTP request failed")) {
            for (int i = 1; i <= getRetryNum(); i++) {
            	try {
            		logger.debug("retrySleepTime start");
            		new Thread().sleep(retrySleepTime);
					logger.debug("retrySleepTime end");
				} catch (InterruptedException e) {
					logger.error("{}", e);
				}
                content = getPage(request);
                if (!content.contains("HTTP request failed")) {
                    break;
                }
            }
            if (content.contains("HTTP request failed")) {
                //when failed
                Page page = new Page();
                page.setRequest(request);
                return page;
            }
        }

        Page page = new Page();
        page.setRawText(content);
        page.setRequest(request);
        page.setStatusCode(200);
        return page;
    }

    @Override
    public void setThread(int threadNum) {
        //this.threadNum = threadNum;
    }

    protected String getPage(Request request) {
        try {
            String url = request.getUrl();
            Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(phantomJSBinPath
							+ (StringUtils.isBlank(phantomJSParams) ? "" : (" " + phantomJSParams))
							+ " " + phantomJSPath + " " + url);
            logger.debug("sleepTime start");
            new Thread().sleep(sleepTime);
            logger.debug("sleepTime end");
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}

        return null;
    }

    public int getRetryNum() {
        return retryNum;
    }

    public PhantomJSDownloader setRetryNum(int retryNum) {
        this.retryNum = retryNum;
        return this;
    }

	@Override
	public void close() throws IOException {
		
	}
	
	public PhantomJSDownloader setSleepTime(int sleepTime){
		this.sleepTime = sleepTime;
		return this;
	}
	
	public PhantomJSDownloader setRetrySleepTime(int retrySleepTime){
		this.retrySleepTime = retrySleepTime;
		return this;
	}
}
