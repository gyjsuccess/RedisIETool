package com.d5.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;

import com.d5.common.Constants;
import com.d5.util.HttpConstant;
import com.d5.util.MatchUtil;
import com.d5.util.UrlUtils;

import net.sf.json.JSONObject;

/**
 * Object storing extracted result and urls to fetch.<br>
 * Not thread safe.<br>
 * Main method：                                               <br>
 * {@link #getUrl()} get url of current page                   <br>
 * {@link #getHtml()}  get content of current page                 <br>
 * {@link #putField(String, Object)}  save extracted result            <br>
 * {@link #getResultItems()} get extract results to be used in {@link us.codecraft.webmagic.pipeline.Pipeline}<br>
 * {@link #addTargetRequests(java.util.List)} {@link #addTargetRequest(String)} add urls to fetch                 <br>
 *
 * @author code4crafter@gmail.com <br>
 * @see us.codecraft.webmagic.downloader.Downloader
 * @see us.codecraft.webmagic.processor.PageProcessor
 * @since 0.1.0
 */
public class Page {

    private Request request;

    private String html;

    private String rawText;

    private int statusCode;

    private boolean needCycleRetry;

    private List<Request> targetRequests = Lists.newArrayListWithCapacity(10);

	private List<ResultItems> resultItemsList;

    public Page() {
    }

    public Page setSkip(boolean skip) {
        return this;
    }

    /**
     * store extract results
     *
     * @param key key
     * @param field field
     */
    public void putField(String key, Object field) {
    }

    /**
     * get html content of page
     *
     * @return html
     */
    public String getHtml() {
        if (html == null) {
            html = UrlUtils.fixAllRelativeHrefs(rawText, request.getUrl());
        }
        return html;
    }

    public List<Request> getTargetRequests() {
        return this.targetRequests;
    }

    /**
     * add urls to fetch
     *
     * @param requests requests
     */
    public void addTargetRequests(List<String> requests) {
        synchronized (targetRequests) {
            for (String s : requests) {
                if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                    continue;
                }
                //s = UrlUtils.canonicalizeUrl(s, url.toString());
                targetRequests.add(new Request(s));
            }
        }
    }

    /**
     * add urls to fetch
     *
     * @param requests requests
     * @param priority priority
     */
    public void addTargetRequests(List<String> requests, long priority) {
        synchronized (targetRequests) {
            for (String s : requests) {
                if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                    continue;
                }
                //s = UrlUtils.canonicalizeUrl(s, url.toString());
                targetRequests.add(new Request(s).setPriority(priority));
            }
        }
    }

    /**
     * add url to fetch
     *
     * @param requestString requestString
     */
    public void addTargetRequest(String requestString) {
        if (StringUtils.isBlank(requestString) || requestString.equals("#")) {
            return;
        }
        synchronized (targetRequests) {
            //requestString = UrlUtils.canonicalizeUrl(requestString, url.toString());
            targetRequests.add(new Request(requestString));
        }
    }

    /**
     * add requests to fetch
     *
     * @param request request
     */
    public void addTargetRequest(Request request) {
        synchronized (targetRequests) {
            targetRequests.add(request);
        }
    }

    /**
     * get request of current page
     *
     * @return request
     */
    public Request getRequest() {
        return request;
    }

    public boolean isNeedCycleRetry() {
        return needCycleRetry;
    }

    public void setNeedCycleRetry(boolean needCycleRetry) {
        this.needCycleRetry = needCycleRetry;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getRawText() {
        return rawText;
    }

    public Page setRawText(String rawText) {
        this.rawText = rawText;
        return this;
    }

    public void setRequest(Request request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "Page [request=" + request + ", html=" + html + ", rawText=" + rawText + ", statusCode=" + statusCode
				+ ", needCycleRetry=" + needCycleRetry + ", targetRequests=" + targetRequests + ", resultItemsList="
				+ resultItemsList + "]";
	}

	public List<ResultItems> getResultItemsList() {
		if(this.resultItemsList == null){
			this.resultItemsList = new ArrayList<ResultItems>();
		}
		return this.resultItemsList;
	}

	public void addTargetRequests_(List<Request> requests) {
		synchronized (targetRequests) {
			targetRequests.addAll(requests);
        }
	}
}
