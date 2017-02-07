package com.d5.service.crawler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.crawler.Page;
import com.d5.crawler.ResultItems;
import com.d5.crawler.Site;
import com.d5.service.crawler.PageProcessor;
import com.d5.service.crawler.Task;
import com.d5.tool.XmlSelectorConfParser;

public class PageProcessor4XmlConf implements PageProcessor {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Site site;
	
	private PageProcessor4XmlConf(Site site){
		this.site = site;
	}
	
	private PageProcessor4XmlConf(){
	}
	
	public static PageProcessor4XmlConf create(Site site){
		return new PageProcessor4XmlConf(site);
	}

	@Override
	public void process(Page page, Task task) {
		new XmlSelectorConfParser(task).parser(page);
		for(ResultItems reItems : page.getResultItemsList()){
			reItems.setRequest(page.getRequest());
		}
	}

	@Override
	public Site getSite() {
		return this.site;
	}
	
}
