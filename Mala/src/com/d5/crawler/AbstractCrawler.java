package com.d5.crawler;

import java.io.Closeable;

import com.d5.service.crawler.ICrawler;
import com.d5.util.MutipleIp;

/**
 * Base class of downloader with some common methods.
 *
 * @author code4crafter@gmail.com
 * @since 0.5.0
 */
public abstract class AbstractCrawler implements ICrawler, Closeable {

    /**
     * A simple method to download a url.
     *
     * @param url url
     * @return html
     */
    public Page download(String url) {
        return download(url, null);
    }

    /**
     * A simple method to download a url.
     *
     * @param url url
     * @param charset charset
     * @return html
     */
    public Page download(String url, String charset) {
    	return download(new Request(url), Site.me().setCharset(charset).toTask());
    }

    protected void onSuccess(Request request) {
    }

    protected void onError(Request request) {
    	MutipleIp.modifyStatus(false);
    }

    protected Page addToCycleRetry(Request request, Site site) {
        Page page = new Page();
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            page.addTargetRequest(request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, 1));
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes >= site.getCycleRetryTimes()) {
                return null;
            }
            page.addTargetRequest(request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes));
        }
        page.setNeedCycleRetry(true);
        return page;
    }
}
