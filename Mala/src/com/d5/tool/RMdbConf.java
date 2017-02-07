package com.d5.tool;

import org.apache.commons.lang3.StringUtils;

import com.d5.util.ConfigurationUtil;
import com.d5.util.MongodbUtil;

public class RMdbConf {
	//private static final String remoteMgConfPath = "";
	//private static final ConfigurationUtil conf = ConfigurationUtil.getInstance(remoteMgConfPath);
	public static final ConfigurationUtil conf = MongodbUtil.getConfig();
	public static final String rIp = conf.getString("mongo.remote.hostIp", "");
	public static final int rPort = conf.getInt("mongo.remote.hostPort", -1);
	public static final String rDbname = conf.getString("mongo.remote.DBName", "");
	public static final boolean rNeedAuth = conf.getBoolean("mongo.remote.needAuthenticate", false);
	public static final String rAuthType = conf.getString("mongo.remote.authenticate.type", "");
	public static final String rAuthUser = conf.getString("mongo.remote.user", "");
	public static final String rAuthPasswd = conf.getString("mongo.remote.password", "");
	public static final boolean confInfoIsCorrect = checkConfInfo();
	
	private static boolean checkConfInfo(){
		if(StringUtils.isBlank(rIp) || rPort <= 0){
			return false;
		}
		return true;
	}
}
