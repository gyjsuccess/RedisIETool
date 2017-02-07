package com.d5.common;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.service.data.IDataRedisService;
import com.d5.service.data.impl.DataRedisServiceImpl;
import com.d5.util.CommonUtil;
import com.d5.util.ConfigurationUtil;
import com.d5.util.IPUtils;
import com.google.common.collect.Maps;

/**
 * Created by 01 on 2016/7/5.
 */
public class Constants {
	public static ConfigurationUtil config;
	static {
		config = ConfigurationUtil.getInstance(
				CommonUtil.getHome() + System.getProperty("file.separator") + "conf"
				+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "conf.properties");
	}
	
	private static Logger log = LoggerFactory.getLogger(Constants.class);
	public static final String LOCAL_MAC = IPUtils.getLocalMachineAddress();
	
    public static final int REDIS_INDEX_4_L = config.getInt("crawler.redis.index.4.list", 7);
    public static final int REDIS_INDEX_4_H = config.getInt("crawler.redis.index.4.hash", 8);
    public static final Map<Integer, Integer> LEVEL_MINUTES_MAP = CommonUtil.parseString2Map(
    		config.getProperty("crawler.redo.level.minutes", "1:5,2:30,3:60,4:120"));
    public static final String PARSER_IMPL_CLASS_NAME = config.getString("crawler.parser.impl.class",
			"com.d5.service.business.impl.JsonConfParserImpl");
    public static final String DOWNLOADER_IMPL_CLASS_NAME = config.getString("crawler.downloader.class",
    		"com.d5.service.business.impl.HttpClientDownloader");
    public static final String PARSER_CONF_FILE_TYPE = config.getString("crawler.parser.conf.file.type", "xml");
    public static final String PHANTOMJS_JS_PATH = config.getString("crawler.phantomjs.js.path", "crawl.js");
	public static final String PHANTOMJS_BIN_PATH = config.getString("crawler.phantomjs.bin.path", "phantomjs");
	public static final String PHANTOMJS_PARAMS = config.getString("crawler.phantomjs.params", "");
	public static final String SYNC_REMOTE_COLL_NAME = config.getString("crawler.sync.mongo.remote.collname", null);
	public static final String SYNC_LOCAL_COLL_NAME = config.getString("crawler.sync.mongo.local.collname", null);
	public static final String SYNC_CLEAR_COLL_NAMES = config.getString("crawler.sync.mongo.clear.collnames", null);
	
	public static final String DEAL_SI_SORCE_COLL_NAME = config.getString("crawler.deal.mongo.si.collname.source", null);
	public static final String DEAL_SI_TARGET_COLL_NAME = config.getString("crawler.deal.mongo.si.collname.target", null);
	public static final String DEAL_SI_RESULT_COLL_NAME = config.getString("crawler.deal.mongo.si.collname.result", null);
	
	public static final String DEAL_M_SORCE_COLL_NAME = config.getString("crawler.deal.mongo.m.collname.source", null);
	public static final String DEAL_M_TARGET_COLL_NAME = config.getString("crawler.deal.mongo.m.collname.target", null);
	public static final String DEAL_M_RESULT_COLL_NAME = config.getString("crawler.deal.mongo.m.collname.result", null);
	
	public static final String DEAL_INITSEEDS_ADD_COLL_NAME = config.getString("crawler.deal.initseeds.collname.add", null);
	public static final String DEAL_INITSEEDS_ALL_COLL_NAME = config.getString("crawler.deal.initseeds.collname.all", null);
	//public static final String DEAL_DIGITAL_COLLECT_NAME = config.getString("crawler.deal.digital.collname", null);
	
	public static String C_NAME_4_ADD = config.getString("crawler.piple.collname.add", null);
	
    public static final String C_NAME_4_CATEGORY = "category_dic_collect";
    public static final String C_NAME_4_SITE_INFO = "site_info_dic_collect";
    public static final AtomicInteger QUARTZ_JOB_INDEX = new AtomicInteger(1);
	public static final AtomicInteger QUARTZ_TRIGGER_INDEX = new AtomicInteger(1);
	public static final String QUARTZ_GROUP_NAME = "GROUP1";
	public static final String COLL_NAME_4_DIGITAL = "douban_media_digital_info";
	public static final String C_NAME_4_WEIGHT = "douban_celebrity_movies_weight";
    
    public static final String IP_PORT_4_XFORWARD = "IPPALL_LIST";
    public static final String DOWNLOADER_KEY_NAME = "SEEDS_LIST_4_DOWNLOADER";
    public static final String PARSER_KEY_NAME = "SEEDS_LIST_4_PARSER";
    public static final String IP_PORT_4_PROXY = "ActiveProxy";
    public static final Pattern REGEX_4_IP_PORT = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}):(\\d{2,5})");
    public static final String DATA_DEAL_4_REDIS_IMPL_CLASS_NAME = "com.d5.service.data.impl.DataDeal2RedisServiceImpl";
    public static final String DATA_DEAL_4_MONGODB_IMPL_CLASS_NAME = "com.d5.service.data.impl.DataDeal2MongodbServiceImpl";
    
    public static final IDataRedisService dataRedisService = new DataRedisServiceImpl();
	public static final String ATTR_RELA_COLLECT = "attr_reflection_dic_collect";
	public static final String ALL_CRAW_KEY_NAME = "HASH_4_ALL_CRAW";
	public static final String KEY_LAST_CRAWL_POS = "HASH_4_LAST_POSTION";
	public static final String KEY_DB_MERGE = "HASH_4_DB_MERGE";
	public static final String DATE_FORMATTER = "yyyy-MM-dd HH:mm:ss";
	public static final String KEY_4_WARNNING_QUEUE = "LIST_4_WARNNING_QUEUE";
	public static final String KEY_WEIGHT_QUEUE = "LIST_4_DB_WEIGHT_QUEUE";
	public static final String MAN_UNWARNNING = "HASH_4_MAN_UNWARNNING";
	
	public static final int SOCKET_PORT = 8888;
	public static final String CronExpression = "06:00:00";
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36";
    
    public static final Map<String, String> FILTER_SCRIPT_MAP = Maps.newHashMap();
	public static final Map<String, String> PARAM_ATTR_RELA_MAP = Maps.newHashMap();
	
	public class WarnningConstants {
		public static final String siteId = "siteId";
		public static final String excuteTime = "excuteTime";
		public static final String planExcuteTime = "planExcuteTime";
		public static final String warnningLevel = "warnningLevel";
		public static final String id = "id";
		public static final String entityUrl = "entityUrl";
	}
	
	public class SiteConstants {
		public static final String siteId = "siteId";
	}
}
