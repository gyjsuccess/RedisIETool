package com.command.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationUtil extends Properties {
	private static Logger log = LoggerFactory.getLogger(ConfigurationUtil.class);
	private static final long serialVersionUID = 50440463580273222L;
	
	private ConfigurationUtil(){
	}

	public static synchronized ConfigurationUtil getInstance(String path) {
		return new ConfigurationUtil(path);
	}

	public String getProperty(String key, String defaultValue) {
		String val = getProperty(key);
		return (val == null || val.isEmpty()) ? defaultValue : val;
	}

	public String getString(String name, String defaultValue) {
		return this.getProperty(name, defaultValue);
	}

	public int getInt(String name, int defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Integer.parseInt(val);
	}

	public long getLong(String name, long defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Integer.parseInt(val);
	}

	public float getFloat(String name, float defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Float.parseFloat(val);
	}

	public double getDouble(String name, double defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Double.parseDouble(val);
	}

	public byte getByte(String name, byte defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Byte.parseByte(val);
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		String val = this.getProperty(name);
		return (val == null || val.isEmpty()) ? defaultValue : Boolean.parseBoolean(val);
	}
	
	public String parseParam(String source, String key){
		String val = this.getProperty(key);
    	String valTemp = val;
		for(String str : valTemp.split(",")){
			String[] keys = str.split(":");
			String val_ = keys[0];
			for(String str_ : keys[1].split("`")){
				if(source.equals(str_)){
					val = val_;
					break;
				}
			}
			if(!valTemp.equals(val)){//取第一次遇到的值
				break;
			}
		}
		return val;
	}

	public ConfigurationUtil(String path) {
		try {
			String CONFIG_FILE = path;
			log.info("CONFIG_FILE:{}", CONFIG_FILE);
			BufferedReader is = new BufferedReader(
					new InputStreamReader(new FileInputStream(CONFIG_FILE), "utf-8"));
			this.load(is);
			is.close();
		} catch (IOException e) {
		}
	}
}