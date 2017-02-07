package com.command.exec.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.command.common.Constants;
import com.command.thread.RuntimeThread;
import com.command.util.QuartzUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Main {
    static { //设置环境变量
        //设置日志级别
        System.setProperty(
                "LOG_LEVEL",
                System.getenv("LOG_LEVEL") == null ?
                		"INFO" :
                		System.getenv("LOG_LEVEL"));

        System.setProperty("HOME", 
        		System.getenv("CE_HOME") == null ?
        				System.getProperty("user.dir") : 
        				System.getenv("CE_HOME"));
    }

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
    	int type = -1;
    	String command = null;
    	if(StringUtils.isNumericSpace(args[0])){
    		type = Integer.parseInt(args[0]);
    	}
    	if(args.length == 2){
    		command = args[1];
    	}
    	try {
    		if(type == 0){
        		new Thread(new RuntimeThread(command, null)).start();
        		return;
        	}
    		if(type == 1) {
        		startJobs("cron", null);
        		return;
        	}
    		if(type == 2) {
        		startJobs("cron.json.path");
        		return;
        	}
    		String mesg = "只能传入0或者1或者2。0：单次执行，即调用执行一次；1：定时执行，即多次重复调用执行2：定时执行，多个任务定时重复调用执行。";
    		log.error("传入的参数错误，{}", mesg);
		} catch (Exception e) {
			log.error("{}", e);
		}    	
    }
    
    /**
     * 
     * @param key
     * @param obj
     */
    private static void startJobs(String key, Object obj) {
		String classCronStr = Constants.getConfig().getString(key, "");
		if(StringUtils.isNotBlank(classCronStr)){
			QuartzUtil.getInstance().addClassCrons(Arrays.asList(classCronStr.split("\\|"))).genJobs(obj).start();
		}
	}
    /**
     * 
     * @param key
     */
    private static void startJobs(String key) {
		String jsonFileName = Constants.getConfig().getString(key, "");
		log.debug("jsonFileName is:{}", jsonFileName);
		File jsonFile = new File(jsonFileName);
		String cronJsonStr = null;
		JSONArray cronArr = null;
		try {
			if(jsonFile.exists() && jsonFile.isFile()){
				cronJsonStr = FileUtils.readFileToString(jsonFile);
			}
			log.debug("cronJsonStr is :{}", cronJsonStr);
			if(StringUtils.isNotBlank(cronJsonStr)){
				cronArr = JSONArray.fromObject(cronJsonStr);
			}
			if(cronArr == null){
				log.error("读取到任务配置信息失败！");
				return;
			}
			List<String> classCronList = new ArrayList();
			for(int i=0; i<cronArr.size(); i++){
				JSONObject jObj = cronArr.getJSONObject(i);
				String command = null;
				String cron = null;
				if(jObj.containsKey("cron")){
					cron = jObj.getString("cron");
				}
				if(jObj.containsKey("command")){
					command = jObj.getString("command");
				}
				if(StringUtils.isNotBlank(cron)){
					classCronList.clear();
					classCronList.add(cron);
					QuartzUtil.getInstance().addClassCrons(classCronList).genJobs(command).start();
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
	}
}
