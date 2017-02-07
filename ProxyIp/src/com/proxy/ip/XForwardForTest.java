package com.proxy.ip;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.proxy.ip.util.JedisIPPortUtil;
import com.proxy.ip.util.RegexUtils;

/**
 * 
 */

public class XForwardForTest {
	private static Logger log = LoggerFactory.getLogger(XForwardForTest.class);
	private static RequestConfig rConfig = RequestConfig.custom().setConnectTimeout(3 * 1000).setSocketTimeout(3 * 1000)
			.build();
	private static HttpPost createIpPostRequest(String url) {
		HttpPost post = new HttpPost(url);
		addHeaders(post, url);
		try {
			post.setEntity(new UrlEncodedFormEntity(getPostData(Constants.PROXY_SITE_NAME)));
		} catch (UnsupportedEncodingException e) {
			log.error("create post failed");
		}

		return post;
	}
	
	private static HttpGet createIpGetRequest(String url) {
		HttpGet get = new HttpGet(url);
		addHeaders(get, url);
		return get;
	}

	private static List<? extends NameValuePair> getPostData(String proxySiteName4Dx) {
		return Constants.postParamsMap.get(proxySiteName4Dx);
	}

	private static void addHeaders(HttpUriRequest httpGet, String string) {
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
		for(Map.Entry<String, String> en : Constants.EXTRA_HEADERS_MAP.entrySet()){
			httpGet.setHeader(en.getKey(), en.getValue());
		}
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
	}

	public static void main(String args[]) throws IOException, Exception {
		JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		String url = "https://movie.douban.com/review/2189181/";
		class XForwardForThread implements Runnable{
			private JedisIPPortUtil jedis;
			private String url;
			public XForwardForThread(JedisIPPortUtil jedis, String url){
				this.jedis = jedis;
				this.url = url;
			}
			@Override
			public void run() {
				try {
					while(true){
						String xIpPort = jedis.rpop("IPPALL_LIST", 5);
						jedis.lpush("IPPALL_LIST", xIpPort, 5);
						Constants.EXTRA_HEADERS_MAP.put("X-Forwarded-For",
								xIpPort.split(":")[0]);
						
						CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(rConfig).build();
						HttpUriRequest httpRequest = createIpGetRequest(url);
						CloseableHttpResponse httpres = httpClient.execute(httpRequest);
						int code = httpres.getStatusLine().getStatusCode();
						log.info("code is: {}", code);
						if (200 == code) {
							HttpEntity en = httpres.getEntity();
							String content = EntityUtils.toString(en, "utf-8");
							httpClient.close();
							EntityUtils.consume(en);
							httpres.close();
							log.info("content.length() is: {}", content.length());
						}
					}
				} catch (Exception e) {
					log.error("{}", e);
				}
			}
		}
		
		for (int count = 0; count < 6; count ++){
			new Thread(new XForwardForThread(jedis, url)).start();
		}
	}
}
