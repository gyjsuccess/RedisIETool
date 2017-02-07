package com.d5;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.HttpURLDownloader;
import com.d5.crawler.Site;
import com.d5.crawler.Spider;
import com.d5.data.transfer.MergeMongodbDatas;
import com.d5.data.transfer.RemoteMongoDataSync;
import com.d5.data.transfer.thread.CalculateWeightThread;
import com.d5.service.crawler.PageProcessor;
import com.d5.service.crawler.impl.MongoPipeline;
import com.d5.service.crawler.impl.PageProcessor4XmlConf;
import com.d5.service.crawler.impl.RedisScheduler;
import com.d5.thread.CanExitExecuteThread;
import com.d5.thread.main.InitDataThread;
import com.d5.tool.InitSeedsUtil;
import com.d5.tool.QuartzUtil;
import com.d5.util.CommonUtil;
import com.d5.util.DelayCalculateUtil;
import com.d5.util.MatchUtil;
import com.d5.util.MongodbUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;

public class Main {
    static { //设置环境变量
        //设置日志级别
        System.setProperty(
                "LOG_LEVEL",
                System.getenv("LOG_LEVEL") == null ? "INFO" : System
                        .getenv("LOG_LEVEL"));

        // 爬虫程序主目录，日志文件中使用。
        System.setProperty(
                "CRAWLER_HOME",
                System.getenv("CRAWLER_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_HOME"));

        // 爬虫程序配置文件主目录，配置文件路径获取时使用。
        System.setProperty(
                "CRAWLER_CONF_HOME",
                System.getenv("CRAWLER_CONF_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_CONF_HOME"));
        
        // 爬虫程序浏览器主目录。
        System.setProperty(
                "CRAWLER_BROWER_HOME",
                System.getenv("CRAWLER_BROWER_HOME") == null ? System
                        .getProperty("user.dir") : System
                        .getenv("CRAWLER_BROWER_HOME"));

        /**
         * 项目程序英文别名。如，团购->group_purchase。配置文件路径获取时使用。 默认是""。
         */
        if (System.getenv("PROGRAM_NAME") == null) {
            System.setProperty("PROGRAM_NAME", "");
            System.setProperty("LOG_SUB_DIR", "");
        } else {
			/*
			 * 日志文件路径配置时使用。 根据PROGRAM_NAME来设置具体的值。
			 */
            System.setProperty(
                    "LOG_SUB_DIR",
                    System.getProperty("file.separator")
                            + System.getenv("PROGRAM_NAME"));
            System.setProperty("PROGRAM_NAME", System.getenv("PROGRAM_NAME"));
        }
        
        Constants.FILTER_SCRIPT_MAP.putAll(CommonUtil.initFilterScriptMap());
    }

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String[] args_ = null;
        args_ = new String[]{"10000101", "5", "34", /*"", */"SEEDS_LIST_4_DB_DOWNLOADER"};
        //重新赋值给args_，正式库时去掉注释放开代码。
        //args_ = args;
        
        Constants.PARAM_ATTR_RELA_MAP.putAll(CommonUtil.initAttrRelaMap());
        
        //同步远程数据--媒体库优化需求
        //RemoteMongoDataSync.syncRemoteData(Constants.SYNC_REMOTE_COLL_NAME, Constants.SYNC_LOCAL_COLL_NAME, new Document(), 2000);
        
        //执行抓取
        spiderRun(args_, null);
        
        //生成融合数据--媒体库优化需求
        //MergeMongodbDatas.mergeData(Constants.DEAL_M_SORCE_COLL_NAME, Constants.DEAL_M_TARGET_COLL_NAME, Constants.DEAL_M_RESULT_COLL_NAME, Constants.KEY_DB_MERGE, Constants.REDIS_INDEX_4_H);
        
        //启动其他定时任务
        //startJobs("crawler.otherjob.crons", null);
        
        //启动明星作品权重计算线程--媒体库优化需求
        int tSize = 2;
        ScheduledExecutorService es = Executors.newScheduledThreadPool(tSize);
        for(int i=0; i<tSize; i++){
			es.execute(new CalculateWeightThread(Constants.KEY_WEIGHT_QUEUE, Constants.dataRedisService, 0,
					Constants.C_NAME_4_WEIGHT));
        }
    }

	private static Map<String, JSONObject> initSiteInfoList(String[] args) {
		List<Document> cList = null;
		if(StringUtils.isBlank(args[2])){
			//cList = MongodbUtil.find(Constants.C_NAME_4_CATEGORY, new Document());
			cList = Lists.newArrayList();
		} else {
			cList = MongodbUtil.find(Constants.C_NAME_4_CATEGORY, Filters.in("siteId", args[2].split(",")));
		}
		
		Map<String, JSONObject> map = Maps.newHashMap();
		for(Document doc : cList){
			//siteId 存在否，不存在，添加
			JSONObject categoryInfo = new JSONObject();
			categoryInfo.putAll(doc);
			categoryInfo.remove("_id");
			map.put(categoryInfo.getString("categoryId"), categoryInfo);
			/*Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H, Constants.KEY_NAME_4_CATEGORY, 
					doc.getString("categoryId"), doc.toJson());*/
		}
		
		return map;
	}

	private static void spiderRun(String[] args, JSONObject siteInfo) {
    	checkParams(args);
    	/*
    	 * sCommand------:
    	 * (1-6位){1-立即执行，0反之；1-使用定时任务，0反之；1-使用外部接口，0反之；1-使用XForwardFor，0反之；1-使用MutipleIp，0反之；1-完成后退出，0反之；}
    	 * (7-12位){}1-使用代理，0反之；1-不使用默认startUrls,0反之；
    	 */
    	String sCommand = args[0];
    	boolean immediateRun = isUse(sCommand, 1);
    	String downloaderQueue = "";
    	boolean containQueue = false;
    	int lastInx = args.length - 1;
    	if(args.length >= 4 && args[lastInx].matches("SEEDS_LIST_4_.*_DOWNLOADER")){
    		downloaderQueue = args[lastInx];
    		containQueue = true;
    	}
    	
    	Integer threadSize = 1;
    	
    	//创建siteInfo
    	/*JSONObject siteInfo = new JSONObject();
    	initSiteInfo(siteInfo, args);*/
    	
    	//添加种子信息
    	List<String> startUrls = Lists.newArrayList();
    	
    	//媒体库优化需求--增量 --start
    	if(immediateRun && (StringUtils.isNotBlank(args[2]) && "34".equals(args[2]))){
    		/*RemoteMongoDataSync.syncRemoteData(Constants.SYNC_REMOTE_COLL_NAME,
    				Constants.SYNC_LOCAL_COLL_NAME, new Document(), 2000);
    		InitSeedsUtil.genStartUrls(startUrls,
    				"https://movie.douban.com/subject_search?search_text={name}&cat=1002",
    				Constants.DEAL_INITSEEDS_ADD_COLL_NAME);*/
    		
    	}
    	//媒体库优化需求--增量 --end
    	
    	threadSize = addStartUrls(args, startUrls, containQueue);
    	
    	boolean useDefaultUrls = !isUse(sCommand, 8);
		if(useDefaultUrls  && startUrls.size() < 1){
    	}
    	
    	threadSize = Integer.parseInt(args[1]);
    	
    	boolean useSchedule = isUse(sCommand, 2);
		boolean useXForwardFor = isUse(sCommand, 4);
		boolean useInterface = isUse(sCommand, 3);
		boolean useMutipleIp = isUse(sCommand, 5);
		boolean useProxy = isUse(sCommand, 7);
		
		if(startUrls.size() > 0){
			siteInfo = new JSONObject();
			initSiteInfo(siteInfo, args);
		}
		
		//创建site信息
    	Site site = Site.me()
    			.setUserAgent(Constants.DEFAULT_USER_AGENT).setAcceptStatCode(Sets.newHashSet(200))
				.addHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.addHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7")
				.addHeader("Accept-Encoding", "gzip, deflate, sdch")
				.addHeader("Accept-Language", "zh-CN,zh;q=0.8")
				.addHeader("Connection", "keep-alive")
				.setUseInterface(useInterface)
				.setUseXForwardFor(useXForwardFor)
				.setUseSchedule(useSchedule)
				.setUseMutipleIp(useMutipleIp)
				.setUseProxy(useProxy)
				.setCycleRetryTimes(Constants.config.getInt("crawler.site.times.cycleretry", 3))
				.setRetryTimes(Constants.config.getInt("crawler.site.times.retry", 3))
				.setDomain(Constants.config.getString("crawler.site.domain", null))
				.setRetrySleepTime(Constants.config.getInt("crawler.site.time.retrysleeptime", 5000))
				.setSleepTime(Constants.config.getInt("crawler.site.time.sleeptime", 2000))
				.setTimeOut(Constants.config.getInt("crawler.site.time.timeout", 10000))
				.setSiteInfo(siteInfo)
				.initStartRequest(startUrls)
				.setCategorys(initSiteInfoList(args))
				.setRedisKeyName4Crawled(Constants.KEY_LAST_CRAWL_POS);//记录评论上次爬取位置的hash。
    	
    	//创建pageProcessor
    	PageProcessor pageProcessor = PageProcessor4XmlConf.create(site);
    	
    	//创建spider
    	if(StringUtils.isBlank(downloaderQueue)){
    		downloaderQueue = Constants.DOWNLOADER_KEY_NAME;
    	}
    	boolean exitWhenComplete = isUse(sCommand, 6);
		Spider spider = Spider.create(pageProcessor, downloaderQueue)
					/*.setCrawler(new HttpURLDownloader()
							.setSleepTime(Constants.config.getInt("crawler.spider.time.sleeptime", 2000)))*/
    				.scheduler(new RedisScheduler()).addPipeline(new MongoPipeline())
    				.setImmediateRun(immediateRun)
    				.thread(threadSize)
    				.setExitWhenComplete(exitWhenComplete)
    				;
		//媒体库优化需求--全量 --start
    	if(immediateRun && (StringUtils.isNotBlank(args[2]) && "34".equals(args[2]))){
    		InitSeedsUtil.run(spider, null, null, Enums.SeedsTypeEnum.ALL);
    	}
    	//媒体库优化需求--全量 --end
    	
		if(useSchedule){
			//classCron----->类的全路径:quartz的cronExpression
			startJobs("crawler.spider.crons", spider);
		}
		
    	//启动spider
    	spider.start();
	}

	private static void startJobs(String key, Object obj) {
		String classCronStr = Constants.config.getString(key, "");
		if(StringUtils.isNotBlank(classCronStr)){
			QuartzUtil.getInstance().addClassCrons(Arrays.asList(classCronStr.split("\\|"))).genJobs(obj).start();
		}
	}

	/**
	 * 检查命令参数相应位置是否为1，判断是否使用某一特定的功能。
	 * @param sCommand
	 * @param pos
	 * @return
	 */
	private static boolean isUse(String sCommand, int pos) {
		int len = sCommand.length();
		StringBuffer regString = new StringBuffer();
		for(int inx=1; inx<=len; inx++){
			if(inx == pos){
				regString.append("1");
				continue;
			}
			regString.append("[01]");
		}
		
		return MatchUtil.contain(sCommand, regString.toString());
	}

	private static void initSiteInfo(JSONObject siteInfo, String[] args) {
		if(StringUtils.isNotBlank(args[2])){
			String siteInfoPath = CommonUtil.getHome() + System.getProperty("file.separator") + "conf"
					+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "site_info.json";
			try {
				String jsonStr = FileUtils.readFileToString(new File(siteInfoPath));
				JSONArray siteArr = JSONArray.parseArray(jsonStr);
				JSONObject obj = new JSONObject();
				String site = args[2];
				for(int i=0; i<siteArr.size(); i++){
					obj = siteArr.getJSONObject(i);
					if(site.equals(obj.getString("siteId"))){
						siteInfo.putAll(obj);
						break;
					}
				}
			} catch (IOException e) {
				log.error("read siteinfo file error. cause is :{}", e.getMessage());
			}
		}
	}

	private static Integer addStartUrls(String[] args, List<String> startUrls, boolean containQueue) {
		startUrls.addAll(Arrays.asList(Arrays.copyOfRange(args, 3, args.length - (containQueue?1:0))));
		int size = startUrls.size();
    	if(size > 1){
    		return size;
    	}
    	return 1;
	}

	private static void checkParams(String[] args) {
		// TODO Auto-generated method stub
		
	}

	private static void mainRun(String[] args) {
        int threadPoolSize = 5;
        int downloadThreadCount = 3;

        //启动线程
        log.debug("启动线程");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadPoolSize + 1);
        //添加定时线程
        long period = 24L * 60 * 60 * 1000;
        //增量爬取，每天00:01:00开始执行
        long initDelay = DelayCalculateUtil.getInitDelay(Constants.CronExpression, period);
        //executor.scheduleAtFixedRate(new InitDataThread(args), initDelay, period, TimeUnit.MILLISECONDS);
        executor.execute(new InitDataThread(args));

        //页面下载线程添加
        for(int i=0; i<downloadThreadCount; i++) {
            //executor.submit(new OnlineExecuteThread(Constants.DOWNLOADER_KEY_NAME, Constants.dataRedisService, 5000));
            executor.submit(new CanExitExecuteThread(Constants.DOWNLOADER_KEY_NAME, Constants.dataRedisService, 2000));
        }
        //页面解析线程添加
        for(int k=0; k<threadPoolSize-downloadThreadCount; k++) {
            //executor.submit(new OnlineExecuteThread(Constants.PARSER_KEY_NAME, Constants.dataRedisService, 0));
            executor.submit(new CanExitExecuteThread(Constants.PARSER_KEY_NAME, Constants.dataRedisService, 0));
        }

        log.debug("关闭线程池");
        executor.shutdown();
    }
}
