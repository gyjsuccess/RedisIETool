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

public class GetProxyIP {
	private static Logger log = LoggerFactory.getLogger(GetProxyIP.class);

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

	public static void main(String args[]) throws IOException, Exception {
		JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		String url = "http://www.ip3366.net/action/";
		Constants.PROXY_SITE_NAME = "YDL";
		
		Constants.EXTRA_HEADERS_MAP.put("Cookie",
				"ASPSESSIONIDASQSSDST=CAIANKFBBHCNAJKPFKCFEAMJ; CNZZDATA1256284042=854408422-1474868254-null%7C1479290108; ASPSESSIONIDSQRRQDQQ=GFDPHGCCHMOBOFHOONHELOFJ");
		Constants.EXTRA_HEADERS_MAP.put("Host", "www.ip3366.net");
		Constants.EXTRA_HEADERS_MAP.put("Origin", "http://www.ip3366.net");
		Constants.EXTRA_HEADERS_MAP.put("Referer", "http://www.ip3366.net/fetch/");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("key", "20161116142110279")); // 订单号
		nvps.add(new BasicNameValuePair("getnum", "500")); // 提取数量
		nvps.add(new BasicNameValuePair("isp", "0")); // 运营商
		nvps.add(new BasicNameValuePair("anonymoustype", "0")); // 匿名性
		nvps.add(new BasicNameValuePair("start", null)); // 指定IP段
		nvps.add(new BasicNameValuePair("port", null)); // 指定端口
		nvps.add(new BasicNameValuePair("noport", null)); // 排除端口
		nvps.add(new BasicNameValuePair("ipaddress", null)); // 指定地区
		nvps.add(new BasicNameValuePair("unaddress", null)); // 排除地区
		nvps.add(new BasicNameValuePair("area", "1")); // 过滤条件
		nvps.add(new BasicNameValuePair("filter", "1")); // 提取条件
		nvps.add(new BasicNameValuePair("formats", "1")); // 输出格式
		nvps.add(new BasicNameValuePair("splits", null)); // 分隔符
		nvps.add(new BasicNameValuePair("proxytype", "0")); // 代理类型
		nvps.add(new BasicNameValuePair("proxytype", "1")); // 代理类型
		// nvps.add(new BasicNameValuePair("ports", "checkAll()")); //代理端口
		
		Constants.postParamsMap.put(Constants.PROXY_SITE_NAME, nvps);
		
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
				log.error("{}", e);
				Thread.sleep(1000 * 60 * 2);
			}
		}
	}
}
