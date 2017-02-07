package com.d5.manage;

import com.d5.crawler.Spider;

public interface IManager {
	/**
	 * 
	 * @param spider
	 */
	public String regist(Spider spider);
	
	/**
	 * 
	 */
	public String showAll();
	
	/**
	 * 
	 * @param spiderUUID
	 */
	public String show(String spiderUUID);
	
	/**
	 * 
	 * @param spiderUUID
	 */
	public String stop(String spiderUUID);
	
	/**
	 * 
	 * @param spiderUUID
	 */
	public String start(String spiderUUID);
	
	/**
	 * 
	 * @param args
	 */
	public String addNew(String[] args);
	
	/**
	 * 
	 */
	public String stopAll();
	
	/**
	 * 
	 */
	public String startAll();
	
	/**
	 * 
	 * @param args
	 */
	public String reloadXml(String[] args);
}
