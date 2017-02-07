package com.proxy.ip.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * 
 */

public class HttpClientUtils {
	private static final Pattern patternForCharset = Pattern.compile("charset\\s*=\\s*['\"]*([^\\s;'\"]*)");
	private static String PROXY_SITE_NAME;
	private static Logger log = LoggerFactory.getLogger(HttpClientUtils.class);
	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000)
			.build();
	private static HttpClientUtils instance;
	public static Map<String, String> EXTRA_HEADERS_MAP = Maps.newHashMap();
	public static Map<String, List<NameValuePair>> postParamsMap = Maps.newHashMap();
	
	public static HttpClientUtils getInstance(){
		if(instance == null){
			instance = new HttpClientUtils();
		}
		return instance;
	}
	
	public static HttpClientUtils setProxySiteName(String proxySiteName){
		PROXY_SITE_NAME = proxySiteName;
		return instance;
	}
	
	public static HttpClientUtils setRequestConfig(RequestConfig rConfig){
		requestConfig = rConfig;
		return instance;
	}
	
	public static HttpClientUtils addExtraHeader(String key, String value){
		EXTRA_HEADERS_MAP.put(key, value);
		return instance;
	}
	
	public static Map<String, String> getExtraHeadersMap(){
		return EXTRA_HEADERS_MAP;
	}
	
	public static HttpClientUtils addPostParam(List<NameValuePair> paramPairs){
		postParamsMap.put(PROXY_SITE_NAME, paramPairs);
		return instance;
	}
	
	private static HttpPost createIpPostRequest(String url) {
		HttpPost post = new HttpPost(url);
		addHeaders(post, url);
		try {
			post.setEntity(new UrlEncodedFormEntity(getPostData(PROXY_SITE_NAME)));
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

	private static List<? extends NameValuePair> getPostData(String proxySiteName) {
		return postParamsMap.get(proxySiteName);
	}

	private static void addHeaders(HttpUriRequest httpGet, String string) {
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
		for(Map.Entry<String, String> en : EXTRA_HEADERS_MAP.entrySet()){
			httpGet.setHeader(en.getKey(), en.getValue());
		}
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
	}

	public static String download(String url, String method) throws IOException, Exception {
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpUriRequest httpRequest = createRequest(url, method);
		CloseableHttpResponse httpres = null;
		try {
			httpres = httpClient.execute(httpRequest);
			int code = httpres.getStatusLine().getStatusCode();
			if (200 == code) {
				String content = getContent(null, httpres);
				httpClient.close();
				httpres.close();
				return content;
			}
		} finally {
			try {
                if (httpres != null) {
                    EntityUtils.consume(httpres.getEntity());
                }
            } catch (IOException e) {
                log.warn("close response fail", e);
            }
		}
		return null;
	}
	
	private static String getContent(String charset, HttpResponse httpResponse) throws IOException, SocketTimeoutException {
    	HttpEntity entity = httpResponse.getEntity();
    	if(isGzip(entity)){
    		entity = new GzipDecompressingEntity(entity);
    	}
        if (charset == null) {
            byte[] contentBytes = IOUtils.toByteArray(entity.getContent());
            String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
            if (htmlCharset != null) {
                return new String(contentBytes, htmlCharset);
            } else {
                log.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
                return new String(contentBytes);
            }
        } else {
            return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        }
    }

	private static HttpUriRequest createRequest(String url, String method) {
		if (method == null || method.equalsIgnoreCase(Method.GET)) {
			return createIpGetRequest(url);
		} else if (method.equalsIgnoreCase(Method.POST)) {
            return createIpPostRequest(url);
        } else if (method.equalsIgnoreCase(Method.HEAD)) {
            return null;
        } else if (method.equalsIgnoreCase(Method.PUT)) {
            return null;
        } else if (method.equalsIgnoreCase(Method.DELETE)) {
            return null;
        } else if (method.equalsIgnoreCase(Method.TRACE)) {
            return null;
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
	}
	
	private static String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
        String charset;
        // charset
        // 1、encoding in http header Content-Type
        String value = httpResponse.getEntity().getContentType().getValue();
        charset = getCharset(value);
        if (StringUtils.isNotBlank(charset)) {
            log.debug("Auto get charset: {}", charset);
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
        log.debug("Auto get charset: {}", charset);
        return charset;
    }
	
	public static String getCharset(String contentType) {
        Matcher matcher = patternForCharset.matcher(contentType);
        if (matcher.find()) {
            String charset = matcher.group(1);
            if (Charset.isSupported(charset)) {
                return charset;
            }
        }
        return null;
    }
	
	private static boolean isGzip(HttpEntity httpEntity) {
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
	
	public static void main(String[] args) {
		String[] urls = {
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479976796964&sign=eaef5e13b0bad2a2189737a1f10a6bba&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp1&data=%7B%22itemId%22%3A%22531126380595%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977116610&sign=13e0f25431084d3cdb44321b998138c1&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp12&data=%7B%22itemId%22%3A%22531126380595%22%2C%22cursor%22%3A%222%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977302951&sign=87853a0e60264104a2374c801a69e913&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp1&data=%7B%22itemId%22%3A%2240584465722%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977403740&sign=e1e8e7daa7dbd9ac638ef0f3245a128f&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp1&data=%7B%22itemId%22%3A%2236899374282%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977446544&sign=3cc9e93e970f87a996ab776c1ef5ec93&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp12&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%222%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977464567&sign=b333506c52bd245be50e4a873ebc8f69&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp23&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%223%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977481782&sign=bf1194740fd285b996816d0bd23cdde5&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp34&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%224%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977494325&sign=6e871762a445966643a992305b6c6e63&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp45&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%225%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977504701&sign=95d4343de1f0c81d7906807bc3507730&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp56&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%226%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977543509&sign=1c0ef593883a6333131b8eebe0a77a42&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp67&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%227%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977557653&sign=0e2f61929d669458be7a5bfc94b0a3db&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp78&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%228%22%7D",
				"https://api.m.taobao.com/h5/mtop.taobao.ocean.quest.list.pc/1.0/?appKey=12574478&t=1479977569418&sign=dbe6fef5736de14ccb8c7cc16a18ff79&api=mtop.taobao.ocean.quest.list.pc&v=1.0&type=jsonp&dataType=jsonp&callback=mtopjsonp89&data=%7B%22itemId%22%3A%2236899374282%22%2C%22cursor%22%3A%229%22%7D"
		};
		
		/*for (String url : urls){
			try {
				System.out.println(URLDecoder.decode(url,"utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}*/
		try {
			HttpClientUtils.addExtraHeader("Cookie", "thw=cn; cna=kpS9EO6MZgICAXcEp+TbjLWv; v=0; t=641ac0447f209aa7457594ce2bf21877; _m_h5_tk=bc940490b2134a412a1194562c8abf6d_1479980824763; _m_h5_tk_enc=0202497566cb1c2cef3b6f455486047e; _tb_token_=77ef957e3e5b1; mt=ci%3D-1_0; uc1=cookie14=UoWwLQnqa7ltgg%3D%3D; l=AicnDzdiJcKNzC4/QwbrUGbcN1HwqvuD; isg=ApycK19BKJnZbNxFgBXYWPQ8bbpHWUA_AzT-wXacrgdnwT5Lnie8zf85V7bT; cookie2=17e962a0b8090af0fdbfe2426db26bd6");
			HttpClientUtils.addExtraHeader("Accept-Encoding", "gzip, deflate, sdch");
			System.out.println(HttpClientUtils.download(urls[urls.length-2], Method.GET));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

abstract class Method {
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String TRACE = "TRACE";
    public static final String CONNECT = "CONNECT";
}
