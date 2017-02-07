package com.d5.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.proxy.Proxy;
import com.d5.service.crawler.Task;
import com.d5.util.HttpConstant;
import com.d5.util.MatchUtil;
import com.d5.util.MutipleIp;
import com.d5.util.RedisDataUtil;
import com.d5.util.UrlUtils;
import com.google.common.collect.Sets;

import net.sf.json.regexp.RegexpUtils;


/**
 * The http downloader based on HttpClient.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
public class HttpClientCrawlerBindIp extends AbstractCrawler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

    private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    private CloseableHttpClient getHttpClient(Site site, Proxy proxy) {
        if (site == null) {
            return httpClientGenerator.getClient(null, proxy);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site, proxy);
                    httpClients.put(domain, httpClient);
                }
            }
        }
        return httpClient;
    }

    @Override
    public Page download(Request request, Task task) {
        Site site = null;
        if (task != null) {
            site = task.getSite();
        }
        Set<Integer> acceptStatCode;
        String charset = null;
        Map<String, String> headers = null;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
            if(site.getUseXForwardFor()){
            	headers.put("X-Forwarded-For", RedisDataUtil.getXIP());
            }
            String host = MatchUtil.find(request.getUrl(), "//((\\w+\\.)+\\w+)/", "" , 1);
            if(StringUtils.isNotBlank(host)){
            	headers.put("Host", host);
            }
        } else {
            acceptStatCode = Sets.newHashSet(200);
        }
        
        headers.putAll(request.getHeaders());
        
        logger.info("downloading page {}", request.getUrl());
        CloseableHttpResponse httpResponse = null;
        int statusCode=0;
        try {
            HttpHost proxyHost = null;
            Proxy proxy = null; //TODO
            /*if (site.getHttpProxyPool() != null && site.getHttpProxyPool().isEnable()) {
                proxy = site.getHttpProxyFromPool();
                proxyHost = proxy.getHttpHost();
            } else if(site.getHttpProxy()!= null){
                proxyHost = site.getHttpProxy();
            }*/
            
            if(site.getUseProxy()){
            	proxyHost = RedisDataUtil.getProxyIpPort();
            }
            
            HttpUriRequest httpUriRequest = null;
            try {
            	httpUriRequest = getHttpUriRequest(request, site, headers, proxyHost);
            	httpResponse = getHttpClient(site, proxy).execute(httpUriRequest);
            	statusCode = httpResponse.getStatusLine().getStatusCode();
                request.putExtra(Request.STATUS_CODE, statusCode);
                if (statusAccept(acceptStatCode, statusCode)) {
                    Page page = handleResponse(request, charset, httpResponse, task);
                    //onSuccess(request);	
                    return page;
                } else {
                    logger.warn("code error " + statusCode + "\t" + request.getUrl());
                    return handleError(site, request);
                }
			} finally {
				if(httpUriRequest != null){
					httpUriRequest.abort();
				}
				if(httpResponse != null){
					httpResponse.close();
				}
			}
        } catch (Exception e) {
            logger.warn("download page " + request.getUrl() + " error", e);
            return handleError(site, request);
        } finally {
        	request.putExtra(Request.STATUS_CODE, statusCode);
            /*if (site.getHttpProxyPool()!=null && site.getHttpProxyPool().isEnable()) {
                site.returnHttpProxyToPool((HttpHost) request.getExtra(Request.PROXY), (Integer) request
                        .getExtra(Request.STATUS_CODE));
            }*/
            try {
                if (httpResponse != null) {
                    //ensure the connection is released back to pool
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                logger.warn("close response fail", e);
            }
        }
    }

    private Page handleError(Site site, Request request) {
    	if (site.getCycleRetryTimes() > 0) {
        	onError(request);
            return addToCycleRetry(request, site);
        }
        onError(request);
        return null;
	}

	@Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        return acceptStatCode.contains(statusCode);
    }

    protected HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers,HttpHost proxy) {
        RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
		try {
			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
			        .setConnectionRequestTimeout(site.getTimeOut())
			        .setSocketTimeout(site.getTimeOut())
			        .setConnectTimeout(site.getTimeOut())
			        .setCookieSpec(CookieSpecs.BEST_MATCH);
			if(site.isUseMutipleIp()){
				requestConfigBuilder = requestConfigBuilder.setLocalAddress(
						InetAddress.getByName(MutipleIp.getNewBindIp()));
			}
			if (proxy !=null) {
				requestConfigBuilder.setProxy(proxy);
				request.putExtra(Request.PROXY, proxy);
			}
	        requestBuilder.setConfig(requestConfigBuilder.build());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return requestBuilder.build();
    }

    protected RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            //default get
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            RequestBuilder requestBuilder = RequestBuilder.post();
            NameValuePair[] nameValuePair = (NameValuePair[]) request.getExtra("nameValuePair");
            if (nameValuePair != null && nameValuePair.length > 0) {
                requestBuilder.addParameters(nameValuePair);
            }
            return requestBuilder;
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return RequestBuilder.head();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
            return RequestBuilder.put();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return RequestBuilder.delete();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
            return RequestBuilder.trace();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException, SocketTimeoutException {
        Page page = new Page();
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        String content = getContent(charset, httpResponse);
        page.setRawText(content);
        return page;
    }

    protected String getContent(String charset, HttpResponse httpResponse) throws IOException, SocketTimeoutException {
    	HttpEntity entity = null;
    	InputStream in = null;
    	try {
    		entity = httpResponse.getEntity();
        	if(isGzip(entity)){
        		entity = new GzipDecompressingEntity(entity);
        	}
        	in = entity.getContent();
            if (charset == null) {
                byte[] contentBytes = IOUtils.toByteArray(in);
                String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
                if (htmlCharset != null) {
                    return new String(contentBytes, htmlCharset);
                } else {
                    logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
                    return new String(contentBytes);
                }
            } else {
                return IOUtils.toString(in, charset);
            }
		} finally {
			if(in != null){
				in.close();
			}
			/*if(entity != null){
				EntityUtils.consume(entity);
			}*/
		}
    }
    
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

    protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
        String charset = null;
        String value = null;
        // charset
        // 1、encoding in http header Content-Type
        try {
        	value = httpResponse.getEntity().getContentType().getValue();
            charset = UrlUtils.getCharset(value);
		} catch (Exception e) {
		}
        if (StringUtils.isNotBlank(charset)) {
            logger.debug("Auto get charset: {}", charset);
            return charset;
        }
        // use default charset to decode first time
        Charset defaultCharset = Charset.defaultCharset();
        String content = new String(contentBytes, defaultCharset.name());
        // 2、charset in meta
        if (StringUtils.isNotEmpty(content)) {
            Document document = Jsoup.parse(content);
            Elements links = document.select("meta");
            for (Element link : links) {
                // 2.1、html4.01 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                String metaContent = link.attr("content");
                String metaCharset = link.attr("charset");
                if (metaContent.indexOf("charset") != -1) {
                    metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                    charset = metaContent.split("=")[1];
                    break;
                }
                // 2.2、html5 <meta charset="UTF-8" />
                else if (StringUtils.isNotEmpty(metaCharset)) {
                    charset = metaCharset;
                    break;
                }
            }
        }
        logger.debug("Auto get charset: {}", charset);
        // 3、todo use tools as cpdetector for content decode
        return charset;
    }

	@Override
	public void close() throws IOException {
		
	}
}
