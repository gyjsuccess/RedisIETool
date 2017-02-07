package com.proxy.ip;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.ip.util.JedisIPPortUtil;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;

/**
 * 2016.10.17
 */

public class ProxyIpClean {
	private static final JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
	private static Logger log = LoggerFactory.getLogger(ProxyIpClean.class);
	public static void main(String[] args){
		String url = "http://www.ip.cn";
		ExecutorService eService = Executors.newFixedThreadPool(200);
		try {
			List<String> ipPorts = jedis.lpopList("IPPALL_LIST", 5);//FileUtils.readLines(new File("D:\\ip.txt"), "utf-8");
			class CleanThread implements Runnable {
				private String url;
				private String ip;
				private String port;
				public CleanThread(String url, String ip, String port){
					this.url = url;
					this.ip = ip;
					this.port = port;
				}
				@Override
				public void run() {
					ipTest(url, ip, port);
				}
			}
			for(String ipPort : ipPorts){
				String[] arr = ipPort.split(":");
				String ip = arr[0];
				String port = arr[1];
				eService.submit(new CleanThread(url, ip, port));
			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	public static void ipTest(String url, String ip, String port) {	
		try {
			HttpUriRequest httpRequest = new HttpGet(url);
			addHeaders(httpRequest);
			
			RequestConfig requestConfig = createRequestConfig(ip, port);
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
						
			CloseableHttpResponse httpres =  httpClient.execute(httpRequest);
			
			int statusCode = httpres.getStatusLine().getStatusCode();
			log.info("StatusCode:{}", statusCode);
			if(statusCode == 200){
                HttpEntity entity = httpres.getEntity();
                String response = EntityUtils.toString(entity,"utf-8");
                try {
                	boolean isActive = TestParser(response, ip);
                	if(isActive){
                		jedis.lpush("ActiveProxy", ip + ":" + port, 5);
                	}
				} catch (XpathSyntaxErrorException e) {
				}
                EntityUtils.consume(entity);
            }
			
			httpClient.close();
			
		} catch (Exception e) {
		}
	}

	private static boolean TestParser(String response, String ip) throws XpathSyntaxErrorException {
		String xpath = "//html/body/div[@class='container-fluid']/div[@id='result']/div[@class='well']/p[1]/code//text()";
        JXDocument jxDocument = new JXDocument(response);
        List<Object> rs = jxDocument.sel(xpath);
        for (Object o : rs) {
        	String outIp = o.toString();
        	if(outIp.length() < 1) break;
            log.info(ip + " --- Out IP:{}", outIp);
            if(ip.equals(outIp)){
            	return true;
            }
        }
        return false;
	}
	
	private static void addHeaders(HttpUriRequest httpGet) {
		httpGet.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
	}

	private static RequestConfig createRequestConfig(String ip, String port){
		HttpHost proxy = new HttpHost(ip, Integer.parseInt(port));	
		return RequestConfig.custom().setProxy(proxy) 
										.setConnectTimeout(new Integer(1000 * 3 ))	
										.setSocketTimeout(new Integer(1000 * 3 )).build();	
	}
}
