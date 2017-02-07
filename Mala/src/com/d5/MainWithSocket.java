package com.d5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.manage.IManager;
import com.d5.manage.impl.SpiderManager;
import com.d5.manage.thread.CommandServiceThread;
import com.d5.thread.CountableThreadPool;

public class MainWithSocket {
	static { //设置环境变量
        //设置日志级别
        System.setProperty(
                "LOG_LEVEL",
                System.getenv("LOG_LEVEL") == null ? "DEBUG" : System
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

    }

    private static Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		String[] args_ = {};
		//args_ = args;
		mainRun(args_);
	}
	
	private static void mainRun(String[] args) {
		log.debug("主模块启动中...");
		IManager manager = new SpiderManager();
		CountableThreadPool cTPool = new CountableThreadPool(1);
		cTPool.execute(new CommandServiceThread(manager));
	}
}
