package com.proxy.ip.test;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;

public class IPTest{
	public static void main(String[] args) {
		String url = "http://www.ip.cn";
		//url = "http://58.68.240.134/sctop10_vote/index1.php";
		String ip = "59.75.128.205";
		String port = "808";
		IPTestUtil.ipTest(url, ip, port);
	}
}

/**
 * 测试使用代理ip能否爬取到数据
 * 2016.10.17
 */
class IPTestUtil {
	private static Logger log = LoggerFactory.getLogger(IPTestUtil.class);
	public static void ipTest(String url, String ip, String port) {	//测试ip是否可用
		try {
			HttpUriRequest httpRequest = createRequest(url, "P");	//获取URL	
			addHeaders(httpRequest);
			
			boolean isTest = "http://www.ip.cn".equals(url);
			
			//设置config
			RequestConfig requestConfig = createRequestConfig(ip, port);
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
						
			//获取http响应数据			
			CloseableHttpResponse httpres =  httpClient.execute(httpRequest);
			
			int statusCode = httpres.getStatusLine().getStatusCode();
			log.info("StatusCode:{}", statusCode);
			if(statusCode == 200){
                // 获取到一个HttpEntity实例
                HttpEntity entity = httpres.getEntity();
                String response = EntityUtils.toString(entity,"utf-8");
                try {
                	if(isTest){
                		TestParser(response);
                	} else {
                		log.info(StringUtils.join(ip, ":", port, "----response is : {}"), response);
                	}
				} catch (XpathSyntaxErrorException e) {
				}
                
                EntityUtils.consume(entity);
            }
			
			//关闭http客户端
			httpClient.close();
			
		} catch (Exception e) {
			log.error("{}", e);
		}		
	}
	
	private static void TestParser(String response) throws XpathSyntaxErrorException {
		String xpath = "//html/body/div[@class='container-fluid']/div[@id='result']/div[@class='well']/p[1]/code//text()";
        JXDocument jxDocument = new JXDocument(response);
        List<Object> rs = jxDocument.sel(xpath);
        for (Object o : rs) {
            log.info("Out IP:{}", o.toString());
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
		nvps.add(new BasicNameValuePair("id", "14"));
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
        httpGet.setHeader("Cookie", "__utma=226521935.73826752.1323672782.1325068020.1328770420.6;");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
	}
	
	private static RequestConfig createRequestConfig(String ip, String port){
		//设置代理
		HttpHost proxy = new HttpHost(ip, Integer.parseInt(port));	//代理配置	
		return RequestConfig.custom().setProxy(proxy)	//代理设置
										.setConnectTimeout(new Integer(1000 * 10))	//超时时间设置
										.setSocketTimeout(new Integer(1000 * 10)).build();	//超时时间设置
	}
}
	
