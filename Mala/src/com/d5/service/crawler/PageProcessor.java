package com.d5.service.crawler;

import com.d5.crawler.Page;
import com.d5.crawler.Site;

/**
 * Interface to be implemented to customize a crawler.<br>
 * <br>
 * In PageProcessor, you can customize:
 * <br>
 * start urls and other settings in {@link Site}<br>
 * how the urls to fetch are detected               <br>
 * how the data are extracted and stored             <br>
 *
 * @author code4crafter@gmail.com <br>
 * @see Site
 * @see Page
 * @since 0.1.0
 */
public interface PageProcessor {

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page page
     * @param task task
     */
    public void process(Page page, Task task);

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    public Site getSite();
}
