package com.proxy.ip;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.proxy.ip.util.JedisIPPortUtil;

/**
 * 2016.10.17
 */

public class VoteTestAll {
	private static final JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
	private static Logger log = LoggerFactory.getLogger(VoteTestAll.class);
	private static List<String> ipList = Lists.newArrayListWithCapacity(0);
	//private static volatile BlockingQueue<String> xIpList = new ArrayBlockingQueue(50000);
	private static String url = "http://58.68.240.132/sctop10_vote/index1.php";
	private static final String REDIS_KEY_NAME = "IPPALL_LIST";
	
	public static void main(String[] args) {
		class VoteThread implements Runnable {
			private String url;
			private String ip;
			private String port;
			public VoteThread(String url, String ip, String port){
				this.url = url;
				this.ip = ip;
				this.port = port;
			}
			@Override
			public void run() {
				while(true){
					vote(url, ip, port, null);
					try {
						Thread.sleep(100 * 2);
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}
			}
		}
		
		/*try {
			xIpList.addAll(FileUtils.readLines(new File("D:\\ip.txt"), "utf-8"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}*/
		
		int threadSize = 500;
		if(args.length > 0 && StringUtils.isNumeric(args[0])){
			threadSize = Integer.parseInt(args[0]);
		}
		
		try {
			ExecutorService eService = Executors.newFixedThreadPool(threadSize);
			while(threadSize > 0){
				eService.submit(new VoteThread(url, null, null));
				threadSize --;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	public static void vote(String url, String ip, String port, String xIp) {	
		try {
			HttpUriRequest httpRequest = createRequest(url, "P");
			String _ip = (String) SerializationUtils.clone(ip);
			String _port = (String) SerializationUtils.clone(port);
			
			if(ip == null && port == null){
		        String _xIp = null;
		        try {
		        	while(true){
		        		_xIp = jedis.rpop(REDIS_KEY_NAME, 5);
		        		if(_xIp == null){
		        			Thread.sleep(100 * 2);
		        		}else{
		        			break;
		        		}
					}
					jedis.lpush(REDIS_KEY_NAME, _xIp, 5);
					String[] arr = _xIp.split(":");
					log.info("xIp is :{}", _xIp);
					_ip = arr[0];
					_port = arr[1];
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			addHeaders(httpRequest, xIp == null ? _ip : xIp);
			
			RequestConfig requestConfig = createRequestConfig(_ip, _port);
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
						
			CloseableHttpResponse httpres =  null;
			
			try {
				httpres =  httpClient.execute(httpRequest);
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}
			
			int statusCode = httpres.getStatusLine().getStatusCode();
			log.info("StatusCode:{}", statusCode);
			if(statusCode == 200){
                HttpEntity entity = httpres.getEntity();
                try {
                	String response = EntityUtils.toString(entity,"utf-8");
                	log.info(StringUtils.join(ip, ":", port, "----response is : {}"), response);
                	EntityUtils.consume(entity);
				} catch (Exception e) {
					log.error(e.getMessage());
				}finally{
					entity = null;
					httpres.close();
				}
            }
			httpClient.close();
			
		} catch (Exception e) {
			log.error("Exception is :{}", e);
		}
	}
	
	private static HttpUriRequest createRequest(String url, String method) {
		HttpUriRequest httpReq = null;
		if("P".equals(method)){
			httpReq = createPostRequest(url);
		}
		if("G".equals(method)){
			httpReq = new HttpGet(url);
		}
		return httpReq;
	}
	
	private static HttpPost createPostRequest(String url) {
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
	
	private static void addHeaders(HttpUriRequest httpGet) {
		httpGet.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
	}
	
	
	private static void addHeaders(HttpUriRequest httpGet, String ip) {
		httpGet.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Connection", "keep-alive");
        if(ip != null){
        	log.info("X-Forwarded-For is :{}", ip);
        	httpGet.setHeader("X-Forwarded-For", ip);
        }
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpGet.setHeader("Origin", "http://sc.people.com.cn");
        //httpGet.setHeader("Referer", "http://sc.people.com.cn/GB/345545/378771/378772/index.html");
        httpGet.setHeader("Referer", "http://sc.people.com.cn/GB/345545/378771/378914/index.html");
        //httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
        //httpGet.setHeader("User-Agent", "Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13");
        //httpGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13C75 Safari/601.1");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50");
	}
	
	private static String getOneIp() {
		synchronized (ipList) {
			/*if(ipList.isEmpty()){
				try {
					ipList = FileUtils.readLines(new File("data/ip.txt"), "utf-8");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(indx.get() == ipList.size()){
				indx = new AtomicInteger(0);
			}
			return ipList.get(indx.getAndIncrement());*/
			String ip;
			ip = jedis.rpop("ip4static", 5);
			jedis.lpush("", ip, 5);
			return ip;
		}
	}

	private static RequestConfig createRequestConfig(String ip, String port){
		if(ip == null && port == null){
			return RequestConfig.custom()
					.setConnectTimeout(new Integer(1000 * 3 ))	
					.setSocketTimeout(new Integer(1000 * 3 )).build();
		}
		HttpHost proxy = new HttpHost(ip, Integer.parseInt(port));	
		return RequestConfig.custom().setProxy(proxy) 
										.setConnectTimeout(new Integer(1000 * 3 ))	
										.setSocketTimeout(new Integer(1000 * 3 )).build();	
	}
}
