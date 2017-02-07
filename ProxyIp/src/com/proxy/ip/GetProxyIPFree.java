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

public class GetProxyIPFree {
	private static Logger log = LoggerFactory.getLogger(GetProxyIPFree.class);
	private static RequestConfig rConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000)
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
		String[] urls = new String[]{"http://vxer.daili666api.com/ip/?tid=555770909422058&num=5000",
				"http://www.89ip.cn/api/?&tqsl=500&sxa=&sxb=&tta=&ports=&ktip=&cf=1",
				"http://www.66ip.cn/mo.php?sxb=&tqsl=500&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=http%3A%2F%2Fwww.66ip.cn%2F%3Fsxb%3D%26tqsl%3D10%26ports%255B%255D2%3D%26ktip%3D%26sxa%3D%26radio%3Dradio%26submit%3D%25CC%25E1%2B%2B%25C8%25A1"};
		List<String> allList = jedis.lpopList("IPPALL_LIST", 5);
		while (true) {
			List<String> ipString = Lists.newArrayList();
			
			try {
				for(String url : urls){
					CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(rConfig).build();
					HttpUriRequest httpRequest = createIpGetRequest(url);
					CloseableHttpResponse httpres = httpClient.execute(httpRequest);
					int code = httpres.getStatusLine().getStatusCode();
					//System.out.println("code is :" + code);
					if (200 == code) {
						HttpEntity en = httpres.getEntity();
						String content = EntityUtils.toString(en, "utf-8");
						httpClient.close();
						EntityUtils.consume(en);
						httpres.close();
						//log.info(content);
						String regexRule="((\\d{1,3}\\.){3}\\d{1,3}:\\d+)";
						ipString.addAll(RegexUtils.patternStringList(regexRule,content));
					}
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
			
			int count = 0;
			for (String ipPort:ipString) {
				try {
					if (!allList.contains(ipPort)) // 判断是否重复
					{
						count++;
						jedis.lpush("IPPALL_LIST", ipPort, 5); // 存入redis
						allList.add(ipPort);
					}
				} catch (Exception e) {
					Thread.sleep(1000 * 2);
				}
			}
			log.info("add " + count + " new ip into newvoteip");
			Thread.sleep(1000 * 60 * 5);
		}
	}
}
