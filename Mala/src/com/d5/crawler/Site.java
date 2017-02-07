package com.d5.crawler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.alibaba.fastjson.JSONObject;
import com.d5.common.Enums;
import com.d5.proxy.Proxy;
import com.d5.proxy.ProxyPool;
import com.d5.proxy.SimpleProxyPool;
import com.d5.service.crawler.Task;
import com.d5.util.UrlUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Object contains setting for crawler.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @see us.codecraft.webmagic.processor.PageProcessor
 * @since 0.1.0
 */
public class Site implements Serializable{

	private static final long serialVersionUID = 10000L;

	private String domain;

    private String userAgent;

    private Map<String, String> defaultCookies = new LinkedHashMap<String, String>();

    private Table<String, String, String> cookies = HashBasedTable.create();

    private String charset;
    
    private String redisKeyName4Downloader;

    /**
     * startUrls is the urls the crawler to start with.
     */
    private List<Request> startRequests = new ArrayList<Request>();

    private int sleepTime = 0;

    private int retryTimes = 0;

    private int cycleRetryTimes = 0;

    private int retrySleepTime = 1000;

    private int timeOut = 5000;

    private static final Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();

    private Set<Integer> acceptStatCode = DEFAULT_STATUS_CODE_SET;

    private Map<String, String> headers = new HashMap<String, String>();

    private HttpHost httpProxy;

    private UsernamePasswordCredentials usernamePasswordCredentials; //代理用户名密码设置

    private ProxyPool httpProxyPool;

    private boolean useGzip = true;
    
    private JSONObject siteInfo;
    
    private String redisKeyName4Crawled;

	private String periodExpression = "24 * 60 * 60 * 1000";

	private String cronExpression = "00:01:00";

	private String kafkaTopic = "test";

	private boolean useMutipleIp = false;

	private String[] KAFKA_COLS_4_NEW = null;

	private String[] KAFKA_COLS_4_OLD = null;

	private boolean useInterface = false;

	private boolean useSchedule = false;

	private boolean useXForwardFor = false;

	private boolean useProxy = false;
	
	private Map<String, JSONObject> categorys;

    static {
        DEFAULT_STATUS_CODE_SET.add(200);
    }

    /**
     * new a Site
     *
     * @return new site
     */
    public static Site me() {
        return new Site();
    }

    /**
     * Add a cookie with domain {@link #getDomain()}
     *
     * @param name name
     * @param value value
     * @return this
     */
    public Site addCookie(String name, String value) {
        defaultCookies.put(name, value);
        return this;
    }

    /**
     * Add a cookie with specific domain.
     *
     * @param domain domain
     * @param name name
     * @param value value
     * @return this
     */
    public Site addCookie(String domain, String name, String value) {
        cookies.put(domain, name, value);
        return this;
    }

    /**
     * set user agent
     *
     * @param userAgent userAgent
     * @return this
     */
    public Site setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * get cookies
     *
     * @return get cookies
     */
    public Map<String, String> getCookies() {
        return defaultCookies;
    }

    /**
     * get cookies of all domains
     *
     * @return get cookies
     */
    public Map<String,Map<String, String>> getAllCookies() {
        return cookies.rowMap();
    }

    /**
     * get user agent
     *
     * @return user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * get domain
     *
     * @return get domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * set the domain of site.
     *
     * @param domain domain
     * @return this
     */
    public Site setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Set charset of page manually.<br>
     * When charset is not set or set to null, it can be auto detected by Http header.
     *
     * @param charset charset
     * @return this
     */
    public Site setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * get charset set manually
     *
     * @return charset
     */
    public String getCharset() {
        return charset;
    }

    public int getTimeOut() {
        return timeOut;
    }

    /**
     * set timeout for downloader in ms
     *
     * @param timeOut timeOut
     * @return this
     */
    public Site setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    /**
     * Set acceptStatCode.<br>
     * When status code of http response is in acceptStatCodes, it will be processed.<br>
     * {200} by default.<br>
     * It is not necessarily to be set.<br>
     *
     * @param acceptStatCode acceptStatCode
     * @return this
     */
    public Site setAcceptStatCode(Set<Integer> acceptStatCode) {
        this.acceptStatCode = acceptStatCode;
        return this;
    }

    /**
     * get acceptStatCode
     *
     * @return acceptStatCode
     */
    public Set<Integer> getAcceptStatCode() {
        return acceptStatCode;
    }

    /**
     * get start urls
     *
     * @return start urls
     * @see #getStartRequests
     * @deprecated
     */
    @Deprecated
    public List<String> getStartUrls() {
        return UrlUtils.convertToUrls(startRequests);
    }

    public List<Request> getStartRequests() {
        return startRequests;
    }

    /**
     * Add a url to start url.<br>
     * Because urls are more a Spider's property than Site, move it to {@link Spider#addUrl(String...)}}
     *
     * @param startUrl startUrl
     * @return this
     * @see Spider#addUrl(String...)
     * @deprecated
     */
    public Site addStartUrl(String startUrl) {
        return addStartRequest(new Request(startUrl));
    }

    /**
     * Add a url to start url.<br>
     * Because urls are more a Spider's property than Site, move it to {@link Spider#addRequest(Request...)}}
     *
     * @param startRequest startRequest
     * @return this
     * @see Spider#addRequest(Request...)
     * @deprecated
     */
    public Site addStartRequest(Request startRequest) {
        this.startRequests.add(startRequest);
        if (domain == null && startRequest.getUrl() != null) {
            domain = UrlUtils.getDomain(startRequest.getUrl());
        }
        return this;
    }
    
    public Site initStartRequest(List<String> startUrls) {
    	String domain = "";
    	for(String url : startUrls){
    		if(StringUtils.isBlank(domain) && this.domain == null){
    			domain = UrlUtils.getDomain(url);
    		}
    		JSONObject info = JSONObject.parseObject("{}");
	        info.put(Enums.JsonColums.InitData.toString(), getSiteInfo() == null ? "{}": getSiteInfo().toString());
			this.startRequests.add(new Request(url).putExtra(Request.DATAS, info.toString()));
    	}
    	if (this.domain == null) {
            this.domain = domain;
        }
        return this;
    }

    /**
     * Set the interval between the processing of two pages.<br>
     * Time unit is micro seconds.<br>
     *
     * @param sleepTime sleepTime
     * @return this
     */
    public Site setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    /**
     * Get the interval between the processing of two pages.<br>
     * Time unit is micro seconds.<br>
     *
     * @return the interval between the processing of two pages,
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * Get retry times immediately when download fail, 0 by default.<br>
     *
     * @return retry times when download fail
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Put an Http header for downloader. <br>
     * Use {@link #addCookie(String, String)} for cookie and {@link #setUserAgent(String)} for user-agent. <br>
     *
     * @param key   key of http header, there are some keys constant in {@link HeaderConst}
     * @param value value of header
     * @return this
     */
    public Site addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Set retry times when download fail, 0 by default.<br>
     *
     * @param retryTimes retryTimes
     * @return this
     */
    public Site setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    /**
     * When cycleRetryTimes is more than 0, it will add back to scheduler and try download again. <br>
     *
     * @return retry times when download fail
     */
    public int getCycleRetryTimes() {
        return cycleRetryTimes;
    }

    /**
     * Set cycleRetryTimes times when download fail, 0 by default. <br>
     *
     * @param cycleRetryTimes cycleRetryTimes
     * @return this
     */
    public Site setCycleRetryTimes(int cycleRetryTimes) {
        this.cycleRetryTimes = cycleRetryTimes;
        return this;
    }

    public HttpHost getHttpProxy() {
        return httpProxy;
    }

    /**
     * set up httpProxy for this site
     *
     * @param httpProxy httpProxy
     * @return this
     */
    public Site setHttpProxy(HttpHost httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public boolean isUseGzip() {
        return useGzip;
    }

    public int getRetrySleepTime() {
        return retrySleepTime;
    }

    /**
     * Set retry sleep times when download fail, 1000 by default. <br>
     *
     * @param retrySleepTime retrySleepTime
     * @return this
     */
    public Site setRetrySleepTime(int retrySleepTime) {
        this.retrySleepTime = retrySleepTime;
        return this;
    }

    /**
     * Whether use gzip. <br>
     * Default is true, you can set it to false to disable gzip.
     *
     * @param useGzip useGzip
     * @return this
     */
    public Site setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
        return this;
    }

    public Task toTask() {
        return new Task() {
            @Override
            public String getUUID() {
                return Site.this.getDomain();
            }

            @Override
            public Site getSite() {
                return Site.this;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Site site = (Site) o;

        if (cycleRetryTimes != site.cycleRetryTimes) return false;
        if (retryTimes != site.retryTimes) return false;
        if (sleepTime != site.sleepTime) return false;
        if (timeOut != site.timeOut) return false;
        if (acceptStatCode != null ? !acceptStatCode.equals(site.acceptStatCode) : site.acceptStatCode != null)
            return false;
        if (charset != null ? !charset.equals(site.charset) : site.charset != null) return false;
        if (defaultCookies != null ? !defaultCookies.equals(site.defaultCookies) : site.defaultCookies != null)
            return false;
        if (domain != null ? !domain.equals(site.domain) : site.domain != null) return false;
        if (headers != null ? !headers.equals(site.headers) : site.headers != null) return false;
        if (startRequests != null ? !startRequests.equals(site.startRequests) : site.startRequests != null)
            return false;
        if (userAgent != null ? !userAgent.equals(site.userAgent) : site.userAgent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (defaultCookies != null ? defaultCookies.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        result = 31 * result + (startRequests != null ? startRequests.hashCode() : 0);
        result = 31 * result + sleepTime;
        result = 31 * result + retryTimes;
        result = 31 * result + cycleRetryTimes;
        result = 31 * result + timeOut;
        result = 31 * result + (acceptStatCode != null ? acceptStatCode.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Site{" +
                "domain='" + domain + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", cookies=" + defaultCookies +
                ", charset='" + charset + '\'' +
                ", startRequests=" + startRequests +
                ", sleepTime=" + sleepTime +
                ", retryTimes=" + retryTimes +
                ", cycleRetryTimes=" + cycleRetryTimes +
                ", timeOut=" + timeOut +
                ", acceptStatCode=" + acceptStatCode +
                ", headers=" + headers +
                '}';
    }

    /**
     * Set httpProxyPool, String[0]:ip, String[1]:port <br>
     *
     * @param proxyPool proxyPool
     * @return this
     */
    public Site setHttpProxyPool(ProxyPool proxyPool) {
        this.httpProxyPool = proxyPool;
        return this;
    }

    /**
     * Set httpProxyPool, String[0]:ip, String[1]:port <br>
     *
     * @param httpProxyList httpProxyList
     * @return this
     */
    public Site setHttpProxyPool(List<String[]> httpProxyList, boolean isUseLastProxy) {
        this.httpProxyPool=new SimpleProxyPool(httpProxyList, isUseLastProxy);
        return this;
    }

    public Site enableHttpProxyPool() {
        this.httpProxyPool=new SimpleProxyPool();
        return this;
    }

    public UsernamePasswordCredentials getUsernamePasswordCredentials() {
        return usernamePasswordCredentials;
    }

    public Site setUsernamePasswordCredentials(UsernamePasswordCredentials usernamePasswordCredentials) {
        this.usernamePasswordCredentials = usernamePasswordCredentials;
        return this;
    }

    public ProxyPool getHttpProxyPool() {
        return httpProxyPool;
    }

    public Proxy getHttpProxyFromPool() {
        return httpProxyPool.getProxy();
    }

    public void returnHttpProxyToPool(HttpHost proxy,int statusCode) {
        httpProxyPool.returnProxy(proxy,statusCode);
    }

    public Site setSiteInfo(JSONObject siteInfo){
    	if(siteInfo != null){
    		this.siteInfo = siteInfo;
    	}
    	return this;
    }

	public JSONObject getSiteInfo() {
		return this.siteInfo;
	}

	public String getRedisKeyName4Downloader() {
		return this.redisKeyName4Downloader;
	}
	
	protected Site setRedisKeyName4Downloader(String redisKeyName4Downloader){
		this.redisKeyName4Downloader = redisKeyName4Downloader;
		return this;
	}

	public String getRedisKeyName4Crawled() {
		return this.redisKeyName4Crawled;
	}
	
	public Site setRedisKeyName4Crawled(String redisKeyName4Crawled){
		this.redisKeyName4Crawled = redisKeyName4Crawled;
		return this;
	}

	public String getCronExpression() {
		return this.cronExpression;
	}

	public String getPeriodExpression() {
		return this.periodExpression ;
	}
	
	public Site setPeriodExpression(String periodExpression){
		this.periodExpression = periodExpression;
		return this;
	}
	
	public Site setCronExpression(String cronExpression){
		this.cronExpression  = cronExpression;
		return this;
	}

	public String getKafkaTopic() {
		return this.kafkaTopic ;
	}
	
	public Site setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
		return this;
	}

	public boolean isUseMutipleIp() {
		return this.useMutipleIp ;
	}

	public Site setUseMutipleIp(boolean useMutipleIp) {
		this.useMutipleIp = useMutipleIp;
		return this;
	}
	
	public Site setKafkaCols4New(String colsString) {
		KAFKA_COLS_4_NEW = colsString.split("\\|");
		return this;
	}
	
	public Site setKafkaCols4Old(String colsString) {
		KAFKA_COLS_4_OLD = colsString.split("\\|");
		return this;
	}

	public String[] getKafkaCols4New() {
		return KAFKA_COLS_4_NEW;
	}

	public String[] getKafkaCols4Old() {
		return KAFKA_COLS_4_OLD;
	}

	public boolean getUseInterface() {
		return this.useInterface;
	}
	
	public Site setUseInterface(boolean useInterface) {
		this.useInterface = useInterface;
		return this;
	}

	public boolean getUseSchedule() {
		return this.useSchedule;
	}
	
	public Site setUseSchedule(boolean useSchedule) {
		this.useSchedule = useSchedule;
		return this;
	}

	public Site setUseXForwardFor(boolean useXForwardFor) {
		this.useXForwardFor  = useXForwardFor;
		return this;
	}
	
	public boolean getUseXForwardFor(){
		return this.useXForwardFor;
	}

	public boolean getUseProxy() {
		return this.useProxy ;
	}
	
	public Site setUseProxy(boolean useProxy){
		this.useProxy = useProxy;
		return this;
	}

	public Map<String, JSONObject> getCategorys() {
		return categorys;
	}

	public Site setCategorys(Map<String, JSONObject> categorys) {
		this.categorys = categorys;
		String domain = "";
    	for(Map.Entry<String, JSONObject> en: categorys.entrySet()){
    		String url = en.getValue().getString("url");
    		if(StringUtils.isBlank(domain) && this.domain == null){
    			domain = UrlUtils.getDomain(url);
    		}
    		JSONObject info = JSONObject.parseObject("{}");
	        info.put(Enums.JsonColums.InitData.toString(),
	        		JSONObject.parseObject("{\"categoryId\":\"" +
	        				en.getValue().getString("categoryId") + "\"}"));
			this.startRequests.add(new Request(url).putExtra(Request.DATAS, info.toString()));
    	}
    	if (this.domain == null) {
            this.domain = domain;
        }
		return this;
	}
}
