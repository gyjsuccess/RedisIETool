package com.d5.redis.ie.tool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.redis.client.domain.ContainerKey;
import com.d5.redis.client.integration.Options;
import com.d5.redis.client.service.ExportService;
import com.d5.redis.client.service.ImportService;
import com.d5.redis.ie.util.RegexUtils;

public class Main {
    static { //设置环境变量
        //设置日志级别
        System.setProperty(
                "LOG_LEVEL",
                System.getenv("LOG_LEVEL") ==  null ?
                		"INFO" : System.getenv("LOG_LEVEL"));
        //设置日志目录
        System.setProperty(
                "RIET_LOG_DIR",
                System.getenv("RIET_LOG_DIR") == null ?
                		System.getProperty("user.dir") + File.separatorChar + "log" :
                			System.getenv("RIET_LOG_DIR"));
        
        //设置配置文件目录
        System.setProperty("CONF_HOME",
        		System.getenv("CONF_HOME") == null ?
        				System.getProperty("user.dir") + File.separatorChar + "conf":
        						System.getenv("CONF_HOME"));
    }

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
    	if(args.length < 1 || (args.length == 1 && "--help".equals(args[0]))){
    		log.info(help());
    		return;
    	}
    	Options options = Options.getInstance(null);
    	options.args2Options(args);
    	execExport(options);
    }
    
    
    /**
	 * MethodName：execExport
	 * @author: Administrator
	 * @Date: 2017年1月24日 上午8:43:32
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param args
	 */
	private static void execExport(Options options) {
    	String exportFilePath = options.getString("--o", null);
		try {
			if(Options.isBlank(exportFilePath)){
				if(options.contains("--o")){
					log.error("路径错了！该路径是{}", exportFilePath);
					return;
				} else {
					log.error("必须传入--o参数！");
					return;
				}
			}
			File exportFile = new File(exportFilePath);
			if(exportFile.isDirectory()){
				log.error("路径错了！该路径是文件夹--{}", exportFilePath);
				return;
			}
			
			if(options.containsKey("--e")){
				String filterType = options.getString("--fType", null);
				ExportService eService = new ExportService(exportFilePath, options.getInt("--sId", 1),
						options.getInt("--dbId", -1), new ContainerKey(options.getString("--cKey", "")));
				if(Options.isBlank(filterType)){
					eService.export();
				} else {
					String fKeyStr = options.getString("--fList", null);
					if(Options.isBlank(fKeyStr)){
						log.warn("过滤串传入值错误，有过滤类型时该值必须要传入且不能是空或者空白字符串。执行时未做过滤处理。：{}", fKeyStr);
						eService.export();
					} else {
						if(RegexUtils.find(fKeyStr, "\\w,{2,}\\w")){
							log.warn("过滤串传入值错误，中间不能有连续的多个‘,’。执行时未做过滤处理。：{}", fKeyStr);
							eService.export();
						}
						fKeyStr = RegexUtils.subString(fKeyStr, "(\\w+,)*\\w+");
						List<String> fList = Arrays.asList(fKeyStr.split(","));
						log.debug("{}", fList);
						if(fList.size()>0){
							eService.export(filterType, fList);
						} else {
							log.warn("过滤串传入值错误，未能从中取到要过滤的key。执行时未做过滤处理。：{}", fKeyStr);
							eService.export();
						}
					}
				}
			}
			
			if(options.containsKey("--i")){
				ImportService iService = new ImportService(exportFilePath, options.getInt("--sId", 1),
						options.getInt("--dbId", -1));
				iService.importFile();
			}
		} catch (IOException e) {
			log.error("{}", e.getLocalizedMessage());
		}
	}


	public static String help(){
    	StringBuilder builder = new StringBuilder();
    	builder.append("\n").append("参数及说明如下：").append("\n")
	    	.append("\t").append("--i").append("\t").append("导入").append("\t").append("\n")
	    	.append("\t").append("--e").append("\t").append("导出").append("\t").append("\n")
	    	.append("\t").append("--sId").append("\t").append("服务器配置信息编号").append("\t").append("\n")
	    	.append("\t").append("--dbId").append("\t").append("数据库编号").append("\t").append("\n")
	    	.append("\t").append("--fType").append("\t").append("导出时，过滤key的方式：保留，retain；丢弃，discard。").append("\t").append("\n")
	    	.append("\t").append("--fList").append("\t").append("过滤列表，用‘,’分隔。与发Type搭配使用").append("\t").append("\n")
	    	.append("\t").append("--cKey").append("\t").append("container层级路径，以‘:’结尾；或者是指定的key").append("\t").append("\n")
	    	.append("\t").append("--o").append("\t").append("导入、导出时的文件路径").append("\t").append("\n")
	    	.append("\n").append("\t").append("使用时，参数没有绝对的先后顺序；导入和导出可以同时使用。");
    	return builder.toString();
    }
}
