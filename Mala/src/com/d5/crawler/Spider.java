package com.d5.crawler;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.d5.common.Constants;
import com.d5.service.crawler.ICrawler;
import com.d5.service.crawler.PageProcessor;
import com.d5.service.crawler.Pipeline;
import com.d5.service.crawler.Scheduler;
import com.d5.service.crawler.SpiderListener;
import com.d5.service.crawler.Task;
import com.d5.service.crawler.impl.ConsolePipeline;
import com.d5.service.crawler.impl.QueueScheduler;
import com.d5.thread.ScheduleCountableThreadPool;
import com.d5.thread.main.InterfaceMainThread;
import com.d5.tool.AllCrawFlagDeal;
import com.d5.util.CommonUtil;
import com.d5.util.HttpConstant;
import com.d5.util.UrlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Spider extends Thread implements Task{
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private ICrawler crawler;
	private List<SpiderListener> listeners = Lists.newArrayList();
	private Site site;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private final static int STAT_INIT = 0;
	private final static int STAT_RUNNING = 1;
    private final static int STAT_STOPPED = 2;
    private AtomicInteger stat = new AtomicInteger(STAT_INIT);
    private boolean destroyWhenExit = true;
    private ScheduleCountableThreadPool threadPool;
    private int threadNum = 1;
    private ScheduledExecutorService executorService;
    private Date startTime;
    private List<Request> startRequests;
    private Scheduler scheduler = new QueueScheduler();
	private boolean exitWhenComplete = true;
	private boolean spawnUrl = true;
	private PageProcessor pageProcessor;
	private List<Pipeline> pipelines = Lists.newArrayList();
	private ReentrantLock newUrlLock = new ReentrantLock();
    private Condition newUrlCondition = newUrlLock.newCondition();
    private int emptySleepTime = 30000;
	private String executeTime;
	private String uuid;
	private boolean isAllCraw = false;
	private static final Set<String> CRAW_FLAG_CODE_SET = Sets.newHashSet(new String[]{"0", "1"});
    private Set<String> acceptFlagCode = CRAW_FLAG_CODE_SET;
	private boolean immediateRun = true;
	private int moreThreadCount = 0;
    
    private Spider(){
	}
	
	private Spider(PageProcessor pageProcessor, String redisKeyName4Downloader){
		this.pageProcessor = pageProcessor;
	    this.site = pageProcessor.getSite();
	    if(this.site == null){
	    	this.site = Site.me().setUserAgent(Constants.DEFAULT_USER_AGENT).setAcceptStatCode(Sets.newHashSet(200))
					.addHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.addHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7")
					.addHeader("Accept-Encoding", "gzip, deflate")
					.addHeader("Accept-Language", "zh-cn,zh;q=0.5")
					.addHeader("Connection", "keep-alive")
					.setRetryTimes(3).setTimeOut(3000);
	    }
	    this.site.setRedisKeyName4Downloader(redisKeyName4Downloader);
	    this.startRequests = Lists.newArrayList();
	    this.startRequests.addAll(pageProcessor.getSite().getStartRequests());
	}
	
	public static Spider create(PageProcessor pageProcessor, String redisKeyName4Downloader){
		return new Spider(pageProcessor, redisKeyName4Downloader);
	}
	
	private boolean flagAccept(Set<String> acceptFlagCode, String flagCode) {
        return acceptFlagCode.contains(flagCode);
    }

	@Override
	public String getUUID() {
		if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return CommonUtil.generateShortUuid(DigestUtils.md5Hex(site.getDomain()));
        }
		uuid = CommonUtil.generateShortUuid();
		return uuid;
	}

	@Override
	public Site getSite() {
		return this.site;
	}
	
	private void initComponent() {
        if (crawler == null) {
            this.crawler = new HttpClientCrawlerBindIp();
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        crawler.setThread(threadNum);
        if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new ScheduleCountableThreadPool(threadNum + moreThreadCount, executorService);
            } else {
                threadPool = new ScheduleCountableThreadPool(threadNum + moreThreadCount);
            }
        }
        if (startRequests != null ) {
        	if(immediateRun){
        		for (Request request : startRequests) {
                    scheduler.push(request, this);
                }
        	}
            startRequests.clear();
        }
        startTime = new Date();
    }
	
	public Spider setImmediateRun(boolean immediateRun){
		this.immediateRun = immediateRun;
		return this;
	}
	
	@Override
	public void run() {
		checkRunningStat();
		
        basicInit();
		
        initComponent();
        
        startOtherThread();
        
        logger.info("Spider " + getUUID() + " started!");
        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
        	Request polledRequest = scheduler.poll(this);
        	if (polledRequest == null) {
                if (threadPool.getThreadAlive() <= moreThreadCount) {
                	if(!isAllCraw){
                    	AllCrawFlagDeal.writeAllCrawFlag(getUUID(), "1");
                    	isAllCraw = true;
                    }
                	Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
                			site.getRedisKeyName4Crawled(), getUUID(), executeTime);
                    if(!this.site.getUseSchedule() && !this.site.getUseInterface() && exitWhenComplete){
                    	break;
                    }
                }
                // wait until new url added
                waitNewUrl();
            } else {
            	JSONObject obj = JSONObject.parseObject(String.valueOf(polledRequest.getExtra(Request.DATAS)));
            	if(obj != null && obj.containsKey(HttpConstant.Header.REFERER)){
            		String referer = obj.getString(HttpConstant.Header.REFERER);
                	if(StringUtils.isNotBlank(referer)){
                		polledRequest.addHeader("Referer", referer);
                	}
            	}
            	final Request requestFinal = polledRequest;
            	threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processRequest(requestFinal);
                            onSuccess(requestFinal);
                        } catch (Exception e) {
                            onError(requestFinal);
                            logger.error("process request " + requestFinal + " error", e);
                        } finally {
                            signalNewUrl();
                        }
                    }
                });
            }
        }
        stat.set(STAT_STOPPED);
        // release some resources
        if (destroyWhenExit) {
            close();
        }
        if(!isAllCraw){
        	AllCrawFlagDeal.writeAllCrawFlag(getUUID(), "1");
        	isAllCraw = true;
        	Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
        			site.getRedisKeyName4Crawled(), getUUID(), executeTime);
        }
		
	}

	private void startOtherThread() {
		/*if(this.site.getUseSchedule()){
        }*/
        
        if(this.site.getUseInterface()){
        	threadPool.submit(new InterfaceMainThread(200));
        }
	}

	private void basicInit() {
		String allCrawFlag = AllCrawFlagDeal.getAllCrawFlag(getUUID());
		if(StringUtils.isBlank(allCrawFlag)){
        	AllCrawFlagDeal.writeAllCrawFlag(getUUID(), "0");
        }
        
        executeTime = DateTime.now().toString(Constants.DATE_FORMATTER);
        
        if(flagAccept(acceptFlagCode, allCrawFlag)){
        	//TODO 
        }
        
        if("1".equals(allCrawFlag)){
        	isAllCraw = true;
        }
        
        /*if(this.site.getUseSchedule()){
        	this.moreThreadCount++;
        }*/
        
        if(this.site.getUseInterface()){
        	this.moreThreadCount++;
        }
	}

	private void processRequest(Request request) {
        Page page = crawler.download(request, this);
        if (page == null) {
        	logger.error("url {} get page info faild", request.getUrl());
        	return;
        }
        // for cycle retry
        if (page.isNeedCycleRetry()) {
            extractAndAddRequests(page, true);
            log.debug("{} RetrySleepTime start", this.getName());
            sleepSelf(site.getRetrySleepTime());
            log.debug("{} RetrySleepTime end", this.getName());
            return;
        }
        pageProcessor.process(page, this);
        extractAndAddRequests(page, spawnUrl);
        if(page.getResultItemsList() != null){
        	for (Pipeline pipeline : pipelines) {
                pipeline.process(page.getResultItemsList(), this);
            }
        }
        //for proxy status management
        request.putExtra(Request.STATUS_CODE, page.getStatusCode());
        log.debug("{} SleepTime start", this.getName());
        sleepSelf(site.getSleepTime());
        log.debug("{} SleepTime start", this.getName());
    }
	
	public boolean isSpawnUrl() {
        return spawnUrl;
    }
	
	/**
     * Whether add urls extracted to download.<br>
     * Add urls to download when it is true, and just download seed urls when it is false. <br>
     * DO NOT set it unless you know what it means!
     *
     * @param spawnUrl spawnUrl
     * @return this
     * @since 0.4.0
     */
    public Spider setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }
	
	private void extractAndAddRequests(Page page, boolean spawnUrl) {
        if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetRequests())) {
            for (Request request : page.getTargetRequests()) {
                addRequest(request);
            }
        }
    }
	
	private void addRequest(Request request) {
        if (site.getDomain() == null && request != null && request.getUrl() != null) {
            site.setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }
	
	/**
     * Add urls with information to crawl.<br>
     *
     * @param requests requests
     * @return this
     */
    public Spider addRequest(List<Request> requests) {
        for (Request request : requests) {
            addRequest(request);
        }
        signalNewUrl();
        return this;
    }
	
	private void sleepSelf(int time) {
        try {
            this.sleep(time);
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }
	
	private void onSuccess(Request request){
		for(SpiderListener listen : listeners){
			listen.onSuccess(request);
		}
	}
	
	private void onError(Request request){
		for(SpiderListener listen : listeners){
			listen.onError(request);
		}
	}
	
	private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("Spider is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }
	
	public void close() {
        destroyEach(crawler);
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("Spider is already running!");
        }
    }
    
    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }
    
    public void start() {
        runAsync();
    }
    
    public void stopRun() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            logger.info("Spider " + getUUID() + " stop success!");
        } else {
            logger.info("Spider " + getUUID() + " stop fail!");
        }
    }
    
    /**
     * start with more than one threads
     *
     * @param threadNum threadNum
     * @return this
     */
    public Spider thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }
    
    public Spider setCrawler(ICrawler crawler) {
        checkIfRunning();
        this.crawler = crawler;
        return this;
    }
    
    /**
     * start with more than one threads
     *
     * @param executorService executorService to run the spider
     * @param threadNum threadNum
     * @return this
     */
    public Spider thread(ExecutorService executorService, int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }
    
    public Spider setExecutorService(ScheduledExecutorService executorService) {
        checkIfRunning();
        this.executorService = executorService;
        return this;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     *
     * @param startUrls startUrls
     * @return this
     */
    public Spider startUrls(List<String> startUrls) {
        checkIfRunning();
        this.startRequests = UrlUtils.convertToRequests(startUrls);
        return this;
    }

    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     *
     * @param startRequests startRequests
     * @return this
     */
    public Spider startRequest(List<Request> startRequests) {
        checkIfRunning();
        this.startRequests = startRequests;
        return this;
    }
    
    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }
    
    /**
     * Set wait time when no url is polled.<br><br>
     *
     * @param emptySleepTime In MILLISECONDS.
     */
    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }

	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}
	
	/**
     * add a pipeline for Spider
     *
     * @param pipeline pipeline
     * @return this
     * @see Pipeline
     * @since 0.2.1
     */
    public Spider addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }

    /**
     * set pipelines for Spider
     *
     * @param pipelines pipelines
     * @return this
     * @see Pipeline
     * @since 0.4.1
     */
    public Spider setPipelines(List<Pipeline> pipelines) {
        checkIfRunning();
        this.pipelines = pipelines;
        return this;
    }

    /**
     * clear the pipelines set
     *
     * @return this
     */
    public Spider clearPipeline() {
        pipelines = new ArrayList<Pipeline>();
        return this;
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    /**
     * set scheduler for Spider
     *
     * @param scheduler scheduler
     * @return this
     * @Deprecated
     * @see #setScheduler(us.codecraft.webmagic.scheduler.Scheduler)
     */
    public Spider scheduler(Scheduler scheduler) {
        return setScheduler(scheduler);
    }

    /**
     * set scheduler for Spider
     *
     * @param scheduler scheduler
     * @return this
     * @see Scheduler
     * @since 0.2.1
     */
    private Spider setScheduler(Scheduler scheduler) {
        checkIfRunning();
        Scheduler oldScheduler = this.scheduler;
        this.scheduler = scheduler;
        if (oldScheduler != null) {
            Request request;
            while ((request = oldScheduler.poll(this)) != null) {
                this.scheduler.push(request, this);
            }
        }
        return this;
    }
    
    public boolean getIsAllCraw(){
    	return this.isAllCraw;
    }
    
    public Spider setExitWhenComplete(boolean exitWhenComplete){
    	this.exitWhenComplete = exitWhenComplete;
    	return this;
    }
}
