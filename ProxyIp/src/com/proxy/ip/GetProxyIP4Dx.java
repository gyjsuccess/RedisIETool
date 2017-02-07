package com.proxy.ip;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.ip.util.JedisIPPortUtil;
import com.proxy.ip.util.RegexUtils;

/**
 * 娴嬭瘯浣跨敤浠ｇ悊ip鑳藉惁鐖彇鍒版暟鎹� 2016.10.17
 */

public class GetProxyIP4Dx {
	private static Logger log = LoggerFactory.getLogger(GetProxyIP4Dx.class);

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
	
	public static void getPorxyIp(String url, JedisIPPortUtil jedis){
		while (true) {
			try {
				int count = 0;
				RequestConfig rConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000)
						.build();
				CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(rConfig).build();

				HttpUriRequest httpRequest = createIpPostRequest(url);
				CloseableHttpResponse httpres = httpClient.execute(httpRequest);
				int code = httpres.getStatusLine().getStatusCode();
				//System.out.println("code is :" + code);
				if (200 == code) {
					HttpEntity en = httpres.getEntity();
					String content = EntityUtils.toString(en, "gbk");
					httpClient.close();
					//log.info(content);
					String regexRule="((\\d{1,3}\\.){3}\\d{1,3}:\\d{2,5})";
					List<String> ipString= RegexUtils.patternStringList(regexRule,content);
					for (String ipPort:ipString) {

						//String ipPort = StringUtils.join(obj.getString("Ip"), ":", obj.getString("Port"));
						count++;

						try {
							if (!jedis.hexists("voteipmap", ipPort, 5)) // 判断是否重复
							{
								jedis.lpush("newvoteip", ipPort, 5); // 存入redis
								jedis.hset("voteipmap", ipPort, ipPort, 5);
							}
						} catch (Exception e) {
							Thread.sleep(1000 * 2);
						}

					}
				}
				log.info("add " + count + " new ip into newvoteip");
				Thread.sleep(1000 * 60 * 2);
			} catch (Exception e) {
				log.error("{}", e.getMessage());
				try {
					Thread.sleep(1000 * 60 * 2);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static void getPorxyIp(String url, JedisIPPortUtil jedis, String charSet){
		while (true) {
			try {
				int count = 0;
				RequestConfig rConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000)
						.build();
				CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(rConfig).build();

				HttpUriRequest httpRequest = createIpPostRequest(url);
				CloseableHttpResponse httpres = httpClient.execute(httpRequest);
				int code = httpres.getStatusLine().getStatusCode();
				//System.out.println("code is :" + code);
				if (200 == code) {
					HttpEntity en = httpres.getEntity();
					String content = EntityUtils.toString(en, charSet);
					httpClient.close();
					//log.info(content);
					String regexRule="((\\d{1,3}\\.){3}\\d{1,3}:\\d{2,5})";
					List<String> ipString= RegexUtils.patternStringList(regexRule,content);
					for (String ipPort:ipString) {

						//String ipPort = StringUtils.join(obj.getString("Ip"), ":", obj.getString("Port"));
						count++;

						try {
							if (!jedis.hexists("voteipmap", ipPort, 5)) // 判断是否重复
							{
								jedis.lpush("newvoteip", ipPort, 5); // 存入redis
								jedis.hset("voteipmap", ipPort, ipPort, 5);
							}
						} catch (Exception e) {
							Thread.sleep(1000 * 2);
						}

					}
				}
				log.info("add " + count + " new ip into newvoteip");
				Thread.sleep(1000 * 60 * 2);
			} catch (Exception e) {
				log.error("{}", e.getMessage());
				try {
					Thread.sleep(1000 * 60 * 2);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void main(String args[]) throws IOException, Exception {
		JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		String url = "http://www.daxiangdaili.com/pick/";
		Constants.PROXY_SITE_NAME = "DX";
		
		Constants.EXTRA_HEADERS_MAP.put("Cookie",
				"_daili_sesfdasion=amtKdFZDRHdmVVZ5N0V2bE1wNmxtY3RHRGlOeEdZUWpDU0tuYWtTMDlRdXhFZWV0TkhoZWF0Slp1Ym1mSDJnZWhqVWh2WU9LUmZKNGJ2UnZTMWJERUx4LzUwVjV4WjVkS3lvV3ZPZmV1em1MYm1CZFgxbWdLRVlNcUFPV1orajN4dkNNUS9DaEM1ZktnelkwZG1yY2RaNzA1SStxL3FDM01kdzRsV0VuRkxkclpzVGZIdnYzaXo1RXRaTFdrelhCLS1XREFQL0gwMnBhMDdNd21BZXR3dE1RPT0%3D--d983513fbc210bf4432c793e590881c59005253a; CNZZDATA4793028=cnzz_eid%3D1096036066-1479430577-%26ntime%3D1479430577");
		Constants.EXTRA_HEADERS_MAP.put("Host", "www.daxiangdaili.com");
		Constants.EXTRA_HEADERS_MAP.put("Origin", "http://www.daxiangdaili.com");
		Constants.EXTRA_HEADERS_MAP.put("Referer", "http://www.daxiangdaili.com/web?tid=556912682969969");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("tid", "556912682969969")); // 订单号
		nvps.add(new BasicNameValuePair("num", "500")); // 提取数量
		nvps.add(new BasicNameValuePair("foreign", "none")); // 运营商
//		nvps.add(new BasicNameValuePair("operator", "移动")); // 匿名性
//		nvps.add(new BasicNameValuePair("operator", "电信")); // 指定IP段
//		nvps.add(new BasicNameValuePair("operator", "联通")); // 指定端口
		nvps.add(new BasicNameValuePair("ports", null)); // 排除端口
		nvps.add(new BasicNameValuePair("exclude_ports", null)); // 指定地区
		nvps.add(new BasicNameValuePair("area", null)); // 过滤条件
		nvps.add(new BasicNameValuePair("category", null)); // 过滤条件
		nvps.add(new BasicNameValuePair("protocol", null)); // 过滤条件
		nvps.add(new BasicNameValuePair("download", null)); // 过滤条件
		nvps.add(new BasicNameValuePair("filter", "on")); // 过滤条件
		
		Constants.postParamsMap.put(Constants.PROXY_SITE_NAME, nvps);
		getPorxyIp(url, jedis);
	}
}
