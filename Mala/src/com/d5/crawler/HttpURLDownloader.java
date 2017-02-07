package com.d5.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.service.crawler.Task;
import com.d5.util.MatchUtil;

import net.sf.json.JSONObject;

/**
 * this downloader is used to download pages which need to render the javascript
 *
 * @author dolphineor@gmail.com
 * @version 0.5.3
 */
@ThreadSafe
public class HttpURLDownloader extends AbstractCrawler {

    private Logger logger = LoggerFactory.getLogger(HttpURLDownloader.class);

    private int retryNum = 3;
    private int sleepTime = 0 * 1000;
    private int retrySleepTime = 10 * 1000;

    public HttpURLDownloader() {
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
                page.setNeedCycleRetry(true);
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
    	String url = request.getUrl();
        try {
            logger.debug("sleepTime start");
            new Thread().sleep(sleepTime);
            logger.debug("sleepTime end");
            
            //获取网络连接
            HttpURLConnection connection = getConnection(url);
            //打开连接
    		connection.connect();
    		InputStream urlStream =null;
    		//读取响应码
    		int responseCode = connection.getResponseCode();
    		if(responseCode != 200){
    			logger.debug("responseCode={}", responseCode);
    			return "HTTP request failed";
    		}
            BufferedReader br = null;
            StringBuffer stringBuffer = null;
    		try{
    			//获取网页编码格式
    			String contentEncoding = connection.getContentEncoding();
    			if(StringUtils.isNotBlank(contentEncoding)){
    				String contentCode = contentEncoding.toLowerCase();
    				if(contentCode.indexOf("gzip") != -1){
    					urlStream = new GZIPInputStream(connection.getInputStream());
    				}else{
    					urlStream = connection.getInputStream();
    				}
    			}else{
    				urlStream = connection.getInputStream();
    			}
    		
    			String encode=connection.getHeaderField("Content-Type");
    			if(!StringUtils.isBlank(encode)){
					try{
						br = new BufferedReader(new InputStreamReader(urlStream, encode.contains("charset")?encode.substring(encode.indexOf("=")+1):"utf-8"));//取文档中的字符编码,如果没有则默认为UTF-8
					} catch (UnsupportedEncodingException uee) {//网页返回的字符编码名称书写错误如：java.io.UnsupportedEncodingException: UFT-8
						br = new BufferedReader(new InputStreamReader(urlStream, "utf-8"));//取文档中的字符编码,如果没有则默认为UTF-8
					}
				}else{
					br = new BufferedReader(new InputStreamReader(urlStream, "utf-8"));//取文档中的字符编码,如果没有则默认为UTF-8
				}
    			
    			char[] buf = new char[2048];
    			int n = 0;
    			stringBuffer = new StringBuffer();
    			try {
    				while ((n = br.read(buf, 0, 2048)) != -1) {
    					stringBuffer.append(new String(buf, 0, n));
    				}
    			} catch (SocketTimeoutException e){
    				logger.error("下载器读取远端内容发生错误！url是：{}", url);
    				logger.error("原因是：{}", e);
    				stringBuffer.append("HTTP request failed");
    			}
    			
    			String content = stringBuffer.toString();
    			
    			String regex = null;
    			
    			String fm = null;
    			if(url.matches("http://weibo\\.com/\\?category=\\d+")){
    				//列表页的regex
    				regex = "<script.*?>FM\\.view\\((\\{.*?class=\\\\\"WB_innerwrap\\\\\".*?class=\\\\\"m_wrap\\\\\".*?\\})\\)</script>";
    				fm = MatchUtil.find(content, regex, "", 1);
    			}
    			if(url.matches("http://weibo.com/\\d+/\\w+\\?ref=feedsdk.*")){
    				//微博页的regex
        			regex = "<script.*?>FM\\.view\\((\\{.*?class=\\\\\"WB_feed.*?\\})\\)</script>";
        			fm = MatchUtil.find(content, regex, "", 1);
    			}
    			//logger.debug("fm === {}", fm);
    			//广电舆情--获取微博标题--jiefeng.wen--start
    			String headRegex = null;
    			String headHtml = null;
    			if(StringUtils.isNoneBlank(fm)){
    				headRegex = "<head>(.*[\r\n])*.*</head>";
        			headHtml = MatchUtil.find(content, headRegex, ""); 
        			String rsHtml = "<html>" + headHtml + "<body>" + JSONObject.fromObject(fm).getString("html") + "</body></html>";
    				return rsHtml;
    			}
    			//广电舆情--获取微博标题--jiefeng.wen--end
    			return content;
    		}finally{
    			if(null != br){
    				br.close();
    			}
    			if(null != urlStream){
    				urlStream.close();
    			}
    		}
        } catch (IOException e) {
        	logger.error("url是：{}, {}", url, e);
        } catch (InterruptedException e) {
        	logger.error("url是：{}, {}", url, e);
		} catch (Exception e) {
			logger.error("url是：{}, {}", url, e);
		}

        return "HTTP request failed";
    }

    public int getRetryNum() {
        return retryNum;
    }

    public HttpURLDownloader setRetryNum(int retryNum) {
        this.retryNum = retryNum;
        return this;
    }

	@Override
	public void close() throws IOException {
		
	}
	
	public HttpURLDownloader setSleepTime(int sleepTime){
		this.sleepTime = sleepTime;
		return this;
	}
	
	public HttpURLDownloader setRetrySleepTime(int retrySleepTime){
		this.retrySleepTime = retrySleepTime;
		return this;
	}
	
	public HttpURLConnection getConnection(String url) throws Exception{
		URL path = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) path
				.openConnection();
		connection.setConnectTimeout(6000);
		connection.setReadTimeout(12000);
		connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");
		connection.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		//需要的，加条件后，取消注释
		connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
		connection.setRequestProperty("Cache-Control", "max-age=0");
		connection.setRequestProperty("Connection", "keep-alive");
		if (url.startsWith("http://weibo.com")) { //访问会员信息，必须要有登录信息
			String cookie = "SUB=_2AkMvDs14f8NhqwJRmP4WzG_ra4t3ywzEieLBAH7sJRMxHRl-yT83qksStRAX4xWr5WYAlatFlAv61x2JTytN5Q..; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9WWVX0hAu0Y1iIjHj_J-7G_T; SINAGLOBAL=4914179113852.886.1481789052406; UOR=ts.21cn.com,widget.weibo.com,www.baidu.com; login_sid_t=1f41a2bce61d159aeb9728f423b7d15f; YF-Ugrow-G0=1eba44dbebf62c27ae66e16d40e02964; YF-V5-G0=dc2e98bae9c8f3ecec40249231d366d6; _s_tentry=-; Apache=7038194591203.153.1483597187870; ULV=1483597187881:4:2:2:7038194591203.153.1483597187870:1483582717061; YF-Page-G0=59104684d5296c124160a1b451efa4ac; WBStorage=194a5e7d191964cc|undefined";
			connection.setRequestProperty("Cookie", cookie);
		}
		return connection;
	}
}
