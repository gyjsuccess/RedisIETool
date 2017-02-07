package com.d5.service.business.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.HttpClientCrawlerByGen;
import com.d5.crawler.HttpClientCrawlerMyself;
import com.d5.crawler.Request;
import com.d5.crawler.Site;
import com.d5.service.business.IExecuteService;
import com.d5.service.crawler.Task;
import com.d5.util.HttpConstant;

import net.sf.json.JSONObject;

/**
 * Created by 01 on 2016/7/5.
 */
public class HttpClientDownloader implements IExecuteService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final Task task = new Task(){
		private String uuid;
		private Site site;
		@Override
		public String getUUID() {
			if (uuid != null) {
	            return uuid;
	        }
	        if (site != null) {
	            return site.getDomain();
	        }
	        uuid = UUID.randomUUID().toString();
	        return uuid;
		}

		@Override
		public Site getSite() {
			return Site.me().setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2763.0 Safari/537.36").setRetryTimes(3).setSleepTime(3000).setTimeOut(10000).setUseGzip(true);
		}
		
	};
	
    /**
     * 下载网页源码到内存。即是将下载的网页源码返回给程序，不是输出到文件中。
     *
     * @param url
     * @return
     */
    private String download2Memery(String url, String referer) {
        log.debug("当前正在下载：" + url);
        //return UrlUtils.fixAllRelativeHrefs(HttpClientCrawlerMyself.getContentByGetRequest(url, true, false), url);
        //return UrlUtils.fixAllRelativeHrefs(ChromeCrawler.download(url), url);
        if(StringUtils.isBlank(referer)){
        	return new HttpClientCrawlerByGen().download(new Request(url), task).getHtml();
        }else{
        	task.getSite().addHeader("Referer", referer);
        	return new HttpClientCrawlerByGen().download(new Request(url), task).getHtml();
        }
    }

    /**
     * 下载网页源码到文件。即是将网页源码保存到磁盘文件中。
     *
     * @param url
     * @return
     */
    private String download2File(String url) {
        try {
            FileUtils.writeStringToFile(new File(""),
                    HttpClientCrawlerMyself.getContentByGetRequest(url, true, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载网页源码，并序列化。
     *
     * @param url
     * @return
     */
    private String download2Serial(String url) {
        //获取数据
        String source = HttpClientCrawlerMyself.getContentByGetRequest(url, true, false);
        //TODO 将数据序列化到数据库

        return null;
    }

    /**
     * url访问获取到源码之后，根据url的级别获取对应的页面解析实现类。并写入解析队列。
     * @param info
     */
    @Override
    public void execute(String info) {
        //解析数据
        JSONObject infoObj = JSONObject.fromObject(info);
        String url = infoObj.getString(Enums.JsonColums.Url.toString());
        String referer = null;
        if(infoObj.containsKey(HttpConstant.Header.REFERER)){
        	referer = infoObj.getString(HttpConstant.Header.REFERER);
        }
        //执行下载
        String htmlSources = download2Memery(url, referer);
        log.debug("htmlSources length is :" + htmlSources.length());
        infoObj.put(Enums.JsonColums.HtmlSources.toString(), htmlSources);
        infoObj.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.PARSER_IMPL_CLASS_NAME);
        //写入信息到队列中
        Constants.dataRedisService.addInfo2List(Constants.REDIS_INDEX_4_L, Constants.PARSER_KEY_NAME, infoObj.toString());
    }
}
