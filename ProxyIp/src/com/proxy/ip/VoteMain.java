package com.proxy.ip;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.proxy.ip.util.IPTestUtil;
import com.proxy.ip.util.JedisIPPortUtil;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;

public class VoteMain {
	public static void main(String[] args) {
		final JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
	}
}

class VoteThread implements Runnable {
	private String url;
	private String ip;
	private String port;
	private JedisIPPortUtil jedis;
	private Logger log = LoggerFactory.getLogger(VoteThread.class);
	
	public VoteThread(String url, String ip, String port, JedisIPPortUtil jedis) {
		super();
		this.url = url;
		this.ip = ip;
		this.port = port;
		this.jedis = jedis;
	}

	@Override
	public void run() {
		try {
			HttpUriRequest httpRequest = createRequest(url, "P");
			addHeaders(httpRequest, ip);
			
			RequestConfig requestConfig = createRequestConfig(ip, port);
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
						
			CloseableHttpResponse httpres =  httpClient.execute(httpRequest);
			
			int statusCode = httpres.getStatusLine().getStatusCode();
			log.info("StatusCode:{}", statusCode);
			if(statusCode == 200){
                HttpEntity entity = httpres.getEntity();
                String response = EntityUtils.toString(entity,"utf-8");
                
                if(!"-1".equals(response) && !"0".equals(response) && !response.contains("<html")){
        			jedis.lpush(Constants.REDIS_LIST_4_SUCC, StringUtils.join(ip, ":", port), 5);
        		}else{
        			jedis.lpush(Constants.REDIS_LIST_4_ALL, StringUtils.join(ip, ":", port), 5);
        		}
        		log.info(StringUtils.join(ip, ":", port, "----response is : {}"), response);
        		
        		EntityUtils.consume(entity);
            }else{
            	jedis.lpush(Constants.REDIS_LIST_4_ALL, StringUtils.join(ip, ":", port), 5);
            }
			
			httpClient.close();
			
		} catch (Exception e) {
			jedis.lpush(Constants.REDIS_LIST_4_ALL, StringUtils.join(ip, ":", port), 5);
			log.error(StringUtils.join(ip, ":", port, "----error:{}"), e.getMessage());
		}
	}
	
	private String getHourStr() {
		return DateTime.now().toString("yyyyMMddHH");
	}
	
	private HttpUriRequest createRequest(String url, String method) {
		HttpUriRequest httpReq = null;
		if("P".equals(method)){
			httpReq = createPostRequest(url);
		}
		if("G".equals(method)){
			httpReq = new HttpGet(url);
		}
		return httpReq;
	}
	
	private HttpPost createPostRequest(String url) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		HttpPost post = new HttpPost(url);
		nvps.add(new BasicNameValuePair("id", "21"));
		nvps.add(new BasicNameValuePair("submit", "1"));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			log.error("{}", e);
		}
		return post;
	}
	
	private void addHeaders(HttpUriRequest httpGet, String ip) {
		httpGet.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("X-Forwarded-For", ip);
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpGet.setHeader("Origin", "http://sc.people.com.cn");
        httpGet.setHeader("Referer", "http://sc.people.com.cn/GB/345545/378771/378772/index.html");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
	}

	private RequestConfig createRequestConfig(String ip, String port){
		HttpHost proxy = new HttpHost(ip, Integer.parseInt(port));	
		return RequestConfig.custom().setProxy(proxy) 
										.setConnectTimeout(new Integer(1000 * 12 ))	
										.setSocketTimeout(new Integer(1000 * 12 )).build();	
	}
}