package com.command.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.command.util.ConfigurationUtil;

/**
 * Created by 01 on 2016/7/5.
 */
public class Constants {
	private static ConfigurationUtil config;
	private static Logger log = LoggerFactory.getLogger(Constants.class);
    public static final AtomicInteger QUARTZ_JOB_INDEX = new AtomicInteger(1);
	public static final AtomicInteger QUARTZ_TRIGGER_INDEX = new AtomicInteger(1);
	public static final String QUARTZ_GROUP_NAME = "GROUP1";
	
	public static ConfigurationUtil getConfig(){
		synchronized (QUARTZ_GROUP_NAME) {
			if(config == null){
				config = ConfigurationUtil.getInstance(
						System.getProperty("HOME") + System.getProperty("file.separator") + "conf"
						+ System.getProperty("file.separator") + "conf.properties");
			}
		}
		return config;
	}
	
	public static void setConfig(ConfigurationUtil conf) {
		synchronized (QUARTZ_GROUP_NAME) {
			if(config == null){
				config = conf;
			}
		}
	}
}