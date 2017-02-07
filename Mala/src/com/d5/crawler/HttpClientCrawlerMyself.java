package com.d5.crawler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.util.MutipleIp;

/**
 * 封装HttpClient请求
 * @author
 *
 */
public class HttpClientCrawlerMyself {
	private static Logger log = LoggerFactory.getLogger(HttpClientCrawlerMyself.class);

	/**
	 * GET方法发送请求，获取网页内容
	 * @param requestUrl 请求地址
	 * @param isBrowser 是否需要伪装成浏览器访问
	 * @param addProxy 是否需要添加代理
	 * @return
	 */
	public static String getContentByGetRequest(String requestUrl, boolean isBrowser, boolean addProxy){
		try {
			Thread.sleep(100 * Configuration.SleepSeconds.toInteger());//每次获取先进行n秒的休眠
		} catch (InterruptedException e1) {
			log.error("线程休眠失败,堆栈轨迹如下", e1);
		}
		String content = null;
		CloseableHttpClient httpClient = HttpConnectionManager.getHttpClient();
		//HttpGet getRequest = new HttpGet(requestUrl);
		RequestBuilder requestBuilder = RequestBuilder.get();
		requestBuilder.setUri(requestUrl);
		Builder requestConfigBuilder = RequestConfig.custom()
				/*.setConnectionRequestTimeout(3000)
				.setConnectTimeout(10000)
				.setSocketTimeout(50000)*/;
		try {
			requestConfigBuilder.setLocalAddress(InetAddress.getByName(MutipleIp.getNewBindIp()));
		} catch (UnknownHostException e1) {
			log.error("{}", e1);
		}
		requestBuilder.setConfig(requestConfigBuilder.build());
		HttpUriRequest getRequest = requestBuilder.build();

		//伪装成浏览器访问
		if(isBrowser){
			config(getRequest);
		}

		//设置代理
		if(addProxy){
			addProxy(getRequest);
		}
		

		//设置超时

		HttpResponse response = null;
		HttpEntity entity = null;
		int responseCode = 200;
		try {
			int cycleCount = 0;
			boolean trying = true;
			while (trying) {
				if(responseCode>299 &responseCode<399){
					new Thread();
					//如果获取不成功则休眠5分钟,5分钟后重新获取
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes.toInteger());
				}else{
					if(responseCode!=200){
						new Thread();
						//如果获取不成功则休眠3分钟,3分钟后重新获取
						Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes1.toInteger());
					}
				}

				//执行GET请求
				response = httpClient.execute(getRequest);

				//HTTP应答状态行信息
				StatusLine respStatusLine = response.getStatusLine();
				responseCode = respStatusLine.getStatusCode();
				if(responseCode != 200 && responseCode != 202){
					if(!(cycleCount < Configuration.CycleMaxCount.toInteger())){
						//记录失败的url
						//RedisPoolUtils.addData2Jedis(RedisPoolUtils.getJedisResource(3), url,"1");
						trying = false;
						content = "";
						continue;
					}
					cycleCount++;//只循环n次
				} else {
					trying = false;
				}
			}

			//获取响应实体
			entity = response.getEntity();

			if(null != entity){
				content = EntityUtils.toString(isGzip(entity) ? new GzipDecompressingEntity(entity): entity,
						Configuration.Charset.toString());
				//关闭输入流
				EntityUtils.consume(entity);

				//判断响应是否分块编码
            	/*if(entity.isChunked()){
            		content = "";
            	}*/
			}

			//HTTP应答报文头信息
			if(Configuration.DealHeader.toBoolean()){
				dealHeader(response);
			}

		} catch (ConnectTimeoutException cte){
			//Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
			log.error("请求通信[" + requestUrl + "]时连接超时,堆栈轨迹如下", cte);
		} catch (SocketTimeoutException ste){
			log.error("请求通信[" + requestUrl + "]时读取超时,堆栈轨迹如下", ste);
		}catch(ClientProtocolException cpe){
			//该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
			log.error("请求通信[" + requestUrl + "]时协议异常,堆栈轨迹如下", cpe);
		}catch(ParseException pe){
			log.error("请求通信[" + requestUrl + "]时解析异常,堆栈轨迹如下", pe);
		}catch(IOException ioe){
			//该异常通常是网络原因引起的,如HTTP服务器未启动等
			log.error("请求通信[" + requestUrl + "]时网络异常,堆栈轨迹如下", ioe);
		}catch (Exception e){
			log.error("请求通信[" + requestUrl + "]时偶遇异常,堆栈轨迹如下", e);
		} finally {
			//关闭连接,释放资源
			getRequest.abort();
		}

		return content;
	}

	/**
	 * POST方法发送请求，获取网页内容
	 * @param requestUrl 请求地址
	 * @param isBrowser 是否需要伪装成浏览器访问
	 * @param addProxy 是否需要添加代理
	 * @return
	 */
	public static String getContentByPostRequest(String requestUrl, boolean isBrowser, boolean addProxy){
		try {
			Thread.currentThread();
			//new Thread().sleep(100*5);//每次获取先进行0.5秒的休眠
			Thread.sleep(100 * Configuration.SleepSeconds.toInteger());//每次获取先进行n秒的休眠
		} catch (InterruptedException e1) {
			log.error("线程休眠失败,堆栈轨迹如下", e1);
		}
		String content = null;
		CloseableHttpClient httpClient = HttpConnectionManager.getHttpClient();
		HttpPost postRequest = new HttpPost(requestUrl);

		//伪装成浏览器访问
		if(isBrowser){
			config(postRequest);
		}

		//设置代理
		if(addProxy){
			addProxy(postRequest);
		}

		HttpResponse response = null;
		HttpEntity entity = null;
		int responseCode = 200;
		try {
			int cycleCount = 0;
			boolean trying = true;
			while (trying) {
				if(responseCode>299 &responseCode<399){
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes.toInteger());
				}else{
					if(responseCode!=200){
						new Thread();
						//如果获取不成功则休眠3分钟,3分钟后重新获取
						Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes1.toInteger());
					}
				}

				//执行POST请求
				response = httpClient.execute(postRequest);

				//HTTP应答状态行信息
				StatusLine respStatusLine = response.getStatusLine();
				responseCode = respStatusLine.getStatusCode();
				if(responseCode != 200 && responseCode != 202){
					if(!(cycleCount < Configuration.CycleMaxCount.toInteger())){
						//记录失败的url
						//RedisPoolUtils.addData2Jedis(RedisPoolUtils.getJedisResource(3), url,"1");
						trying = false;
						content = "";
						continue;
					}
					cycleCount++;//只循环n次
				} else {
					trying = false;
				}
			}

			//获取响应实体
			entity = response.getEntity();

			if(null != entity){
				content = EntityUtils.toString(isGzip(entity) ? new GzipDecompressingEntity(entity): entity,
						Configuration.Charset.toString());
				//关闭输入流
				EntityUtils.consume(entity);

				//判断响应是否分块编码
            	/*if(entity.isChunked()){
            		content = "";
            	}*/
			}

			//HTTP应答报文头信息
			if(Configuration.DealHeader.toBoolean()){
				dealHeader(response);
			}

		} catch (ConnectTimeoutException cte){
			//Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
			log.error("请求通信[" + requestUrl + "]时连接超时,堆栈轨迹如下", cte);
		} catch (SocketTimeoutException ste){
			log.error("请求通信[" + requestUrl + "]时读取超时,堆栈轨迹如下", ste);
		}catch(ClientProtocolException cpe){
			//该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
			log.error("请求通信[" + requestUrl + "]时协议异常,堆栈轨迹如下", cpe);
		}catch(ParseException pe){
			log.error("请求通信[" + requestUrl + "]时解析异常,堆栈轨迹如下", pe);
		}catch(IOException ioe){
			//该异常通常是网络原因引起的,如HTTP服务器未启动等
			log.error("请求通信[" + requestUrl + "]时网络异常,堆栈轨迹如下", ioe);
		}catch (Exception e){
			log.error("请求通信[" + requestUrl + "]时偶遇异常,堆栈轨迹如下", e);
		} finally {
			//关闭连接,释放资源
			postRequest.releaseConnection();
			postRequest.abort();
		}

		return content;
	}

	/**
	 * GET方法发送请求，获取网页内容
	 * @param requestUrl 请求地址
	 * @param isBrowser 是否需要伪装成浏览器访问
	 * @param addProxy 是否需要添加代理
	 * @return
	 */
	public static String getContentByGetRequestWitshAuthenty(String requestUrl, boolean isBrowser, boolean addProxy){
		try {
			Thread.currentThread();
			//new Thread().sleep(100*5);//每次获取先进行0.5秒的休眠
			Thread.sleep(100 * Configuration.SleepSeconds.toInteger());//每次获取先进行n秒的休眠
		} catch (InterruptedException e1) {
			log.error("线程休眠失败,堆栈轨迹如下", e1);
		}
		String content = null;
		CloseableHttpClient httpClient = HttpConnectionManager.getHttpClient();
		HttpGet getRequest = new HttpGet(requestUrl);

		//伪装成浏览器访问
		if(isBrowser){
			config(getRequest);
		}

		//设置代理
		if(addProxy){
			addProxy(getRequest);
		}

		HttpResponse response = null;
		HttpEntity entity = null;
		int responseCode = 200;
		try {
			int cycleCount = 0;
			boolean trying = true;
			while (trying) {
				if(responseCode>299 &responseCode<399){
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes.toInteger());
				}else{
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes1.toInteger());
				}

				//执行GET请求
				response = httpClient.execute(getRequest);

				//HTTP应答状态行信息
				StatusLine respStatusLine = response.getStatusLine();
				responseCode = respStatusLine.getStatusCode();
				if(responseCode != 200 && responseCode != 202){
					if(!(cycleCount < Configuration.CycleMaxCount.toInteger())){
						//记录失败的url
						//RedisPoolUtils.addData2Jedis(RedisPoolUtils.getJedisResource(3), url,"1");
						trying = false;
						content = "";
						continue;
					}

					AuthState authState = null;
					HttpHost authhost = null;
					if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
						// Target host authentication required
					}
					if (responseCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
						// Proxy authentication required
					}

					if (authState != null) {
						trying = true;
					} else {
						trying = false;
					}

					cycleCount++;//只循环n次
				} else {
					trying = false;
				}
			}
			//获取响应实体
			entity = response.getEntity();

			if(null != entity){
				content = EntityUtils.toString(isGzip(entity) ? new GzipDecompressingEntity(entity): entity,
						Configuration.Charset.toString());
				//关闭输入流
				EntityUtils.consume(entity);
				//判断响应是否分块编码
				if(entity.isChunked()){
					content = "";
				}
			}

			//HTTP应答报文头信息
			if(Configuration.DealHeader.toBoolean()){
				dealHeader(response);
			}
		} catch (ConnectTimeoutException cte){
			//Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
			log.error("请求通信[" + requestUrl + "]时连接超时,堆栈轨迹如下", cte);
		} catch (SocketTimeoutException ste){
			log.error("请求通信[" + requestUrl + "]时读取超时,堆栈轨迹如下", ste);
		}catch(ClientProtocolException cpe){
			//该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
			log.error("请求通信[" + requestUrl + "]时协议异常,堆栈轨迹如下", cpe);
		}catch(ParseException pe){
			log.error("请求通信[" + requestUrl + "]时解析异常,堆栈轨迹如下", pe);
		}catch(IOException ioe){
			//该异常通常是网络原因引起的,如HTTP服务器未启动等
			log.error("请求通信[" + requestUrl + "]时网络异常,堆栈轨迹如下", ioe);
		}catch (Exception e){
			log.error("请求通信[" + requestUrl + "]时偶遇异常,堆栈轨迹如下", e);
		} finally {
			//关闭连接,释放资源
			getRequest.releaseConnection();
			getRequest.abort();
		}

		return content;
	}

	/**
	 * POST方法发送请求，获取网页内容
	 * @param requestUrl 请求地址
	 * @param isBrowser 是否需要伪装成浏览器访问
	 * @param addProxy 是否需要添加代理
	 * @return
	 */
	public static String getContentByPostRequestWitshAuthenty(String requestUrl, boolean isBrowser, boolean addProxy){
		try {
			Thread.currentThread();
			//new Thread().sleep(100*5);//每次获取先进行0.5秒的休眠
			Thread.sleep(100 * Configuration.SleepSeconds.toInteger());//每次获取先进行n秒的休眠
		} catch (InterruptedException e1) {
			log.error("线程休眠失败,堆栈轨迹如下", e1);
		}
		String content = null;
		CloseableHttpClient httpClient = HttpConnectionManager.getHttpClient();
		HttpPost postRequest = new HttpPost(requestUrl);

		//伪装成浏览器访问
		if(isBrowser){
			config(postRequest);
		}

		//设置代理
		if(addProxy){
			addProxy(postRequest);
		}

		HttpResponse response = null;
		HttpEntity entity = null;
		int responseCode = 200;
		try {
			int cycleCount = 0;
			boolean trying = true;
			while (trying) {
				if(responseCode>299 &responseCode<399){
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes.toInteger());
				}else{
					new Thread();
					//如果获取不成功则休眠3分钟,3分钟后重新获取
					Thread.sleep(1000 * 60 * Configuration.FailSleepMinutes1.toInteger());
				}
				//执行POST请求
				response = httpClient.execute(postRequest);

				//HTTP应答状态行信息
				StatusLine respStatusLine = response.getStatusLine();
				responseCode = respStatusLine.getStatusCode();
				if(responseCode != 200 && responseCode != 202){
					if(!(cycleCount < Configuration.CycleMaxCount.toInteger())){
						//记录失败的url
						//RedisPoolUtils.addData2Jedis(RedisPoolUtils.getJedisResource(3), url,"1");
						trying = false;
						content = "";
						continue;
					}

					AuthState authState = null;
					HttpHost authhost = null;
					if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
						// Target host authentication required
					}
					if (responseCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
						// Proxy authentication required
					}
					if (authState != null) {
						trying = true;
					} else {
						trying = false;
					}

					cycleCount++;//只循环n次
				} else {
					trying = false;
				}

			}

			//获取响应实体
			entity = response.getEntity();

			if(null != entity){
				content = EntityUtils.toString(isGzip(entity) ? new GzipDecompressingEntity(entity): entity,
						Configuration.Charset.toString());
				//关闭输入流
				EntityUtils.consume(entity);

				//判断响应是否分块编码
				if(entity.isChunked()){
					content = "";
				}
			}

			//HTTP应答报文头信息
			if(Configuration.DealHeader.toBoolean()){
				dealHeader(response);
			}
		} catch (ConnectTimeoutException cte){
			//Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
			log.error("请求通信[" + requestUrl + "]时连接超时,堆栈轨迹如下", cte);
		} catch (SocketTimeoutException ste){
			log.error("请求通信[" + requestUrl + "]时读取超时,堆栈轨迹如下", ste);
		}catch(ClientProtocolException cpe){
			//该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
			log.error("请求通信[" + requestUrl + "]时协议异常,堆栈轨迹如下", cpe);
		}catch(ParseException pe){
			log.error("请求通信[" + requestUrl + "]时解析异常,堆栈轨迹如下", pe);
		}catch(IOException ioe){
			//该异常通常是网络原因引起的,如HTTP服务器未启动等
			log.error("请求通信[" + requestUrl + "]时网络异常,堆栈轨迹如下", ioe);
		}catch (Exception e){
			log.error("请求通信[" + requestUrl + "]时偶遇异常,堆栈轨迹如下", e);
		} finally {
			//关闭连接,释放资源
			postRequest.releaseConnection();
			postRequest.abort();
		}

		return content;
	}

	/**
	 * 为httpPost增加登录信息
	 * @param httpPost
	 */

	public static void addLoginInfo(HttpPost httpPost) {
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair(Configuration.LoginUserKey.toString(),
				Configuration.LoginUserValue.toString()));
		nvps.add(new BasicNameValuePair(Configuration.LoginPasswdKey.toString(),
				Configuration.LoginPasswdValue.toString()));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
	}

	/**
	 * 处理HTTP响应的headers
	 * @param response
	 */
	private static void dealHeader(HttpResponse response) {
		StringBuilder respHeaderDatas = new StringBuilder();
		for(Header header : response.getAllHeaders()){
			respHeaderDatas.append(header.toString()).append("\r\n");
		}
		System.out.println(respHeaderDatas.toString());
	}

	/**
	 * 判断响应是否支持GZip
	 * @param httpEntity
	 */
	public static boolean isGzip(HttpEntity httpEntity) {
		boolean isGzip = false;
		Header ceheader = httpEntity.getContentEncoding();
		if (ceheader != null) {
			HeaderElement[] codecs = ceheader.getElements();
			for (int i = 0; i < codecs.length; i++) {
				if (codecs[i].getName().equalsIgnoreCase("gzip")) {
					//返回头中含有gzip
					isGzip = true;
				}
			}
		}

		return isGzip;
	}

	/**
	 * 判断响应是否支持GZip
	 * @param httpResponse
	 */
	private static boolean isGzip(HttpResponse httpResponse) {
		Header[] headers = httpResponse.getHeaders("Content-Encoding");
		boolean isGzip = false;
		for(Header h:headers){
			if(h.getValue().equals("gzip")){
				//返回头中含有gzip
				isGzip = true;
			}
		}

		return isGzip;
	}

	/**
	 * 为HTTP请求增加代理
	 * @param getRequest
	 */
	private static void addProxy(HttpUriRequest getRequest) {
		HttpHost proxy = new HttpHost("122.225.254.50", 8000);
		RequestConfig config = RequestConfig.custom()
				.setProxy(proxy)
				.build();
		HttpGet request = new HttpGet("/");
		request.setConfig(config);
	}

	/**
	 * 为HTTP请求设置headers
	 * @param getRequest
	 */
	private static void config(HttpUriRequest getRequest) {
		getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 MicroMessenger/6.5.2.501 NetType/WIFI WindowsWechat");
		getRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		getRequest.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		getRequest.setHeader("Cache-Control", "max-age=0");
		getRequest.setHeader("Connection", "keep-alive");
		getRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
		getRequest.setHeader("Accept-Encoding", "deflate, gzip");

		// 配置请求的超时设置
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(3000)
				.setConnectTimeout(10000)
				.setSocketTimeout(50000)
				.build();
	}
}




enum Configuration {
	Charset("UTF-8"), DealHeader(false), LoginUserKey("username"),
	LoginUserValue("user1"), LoginPasswdKey("password"), LoginPasswdValue("password1"),
	CycleMaxCount(5), SleepSeconds(5),
	FailSleepMinutes(5), FailSleepMinutes1(3);

	private Object value;

	private Configuration(Object value){
		this.value = value;
	}

	// 覆盖方法
	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	public Integer toInteger() {
		return Integer.valueOf(toString());
	}

	public boolean toBoolean() {
		return Boolean.valueOf(toString());
	}
}
