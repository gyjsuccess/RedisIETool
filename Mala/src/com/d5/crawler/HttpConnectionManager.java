package com.d5.crawler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class HttpConnectionManager {
	private static PoolingHttpClientConnectionManager connectionManager;
	/**
	 * 最大连接数
	 */
	public final static int MAX_TOTAL_CONNECTIONS = 800;
	/**
	 * 获取连接的最长等待时间
	 */
	public final static int WAIT_TIMEOUT = 60000;
	/**
	 * 每个路由最大连接数
	 */
	public final static int MAX_ROUTE_CONNECTIONS = 400;
	/**
	 * 连接超时时间
	 */
	public final static int CONNECT_TIMEOUT = 10000;
	/**
	 * 读取超时时间
	 */
	public final static int READ_TIMEOUT = 10000;
	/**
	 * 连接池中 连接请求执行被阻塞的超时时间
	 */
	public static final long CONN_MANAGER_TIMEOUT = 60000;
	/**
	 * 套接字超时时间
	 */
	public static final int SOCKET_TIMEOUT = 50000;
	/**
	 * 目标主机
	 */
	public static final String TARGET_HOST = "";
	/**
	 * 目标主机端口号
	 */
	public static final int TARGET_HOST_PORT = 80;

	private static CloseableHttpClient defaultHttpClient;

	private static HttpRequestRetryHandler httpRequestRetryHandler;
	
	static {
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
				.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
				.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create().register("http", plainsf)
				.register("https", sslsf).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);

		connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		connectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
		// 将最大连接数增加到200
		connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // 将每个路由基础的连接增加到20
		connectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        // 将目标主机的最大连接数增加到50
        HttpHost localhost = new HttpHost(TARGET_HOST,TARGET_HOST_PORT);
        connectionManager.setMaxPerRoute(new HttpRoute(localhost), MAX_ROUTE_CONNECTIONS);

		// 请求重试处理
		httpRequestRetryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				if (executionCount >= 5) {// 如果已经重试了5次，就放弃                    
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试                    
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常                    
                    return false;
                }                
                if (exception instanceof InterruptedIOException) {// 超时                    
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达                    
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝                    
                    return false;
                }
                if (exception instanceof SSLException) {// ssl握手异常                    
                    return false;
                }
                
				HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {                    
                    return true;
                }
                return false;
			}
		};

	}

	public static CloseableHttpClient getHttpClient() {
		if(defaultHttpClient == null) {
			defaultHttpClient = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.setRetryHandler(httpRequestRetryHandler)
					.setConnectionManagerShared(true).build();
		}
		return defaultHttpClient;
	}
}
