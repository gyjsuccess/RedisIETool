package com.command.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.command.thread.RuntimeThread;

/**
 * Created by 01 on 2016/8/10.
 */
public class CommandExecJob implements Job {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public CommandExecJob () {
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jobKey = context.getJobDetail().getKey().toString();
		log.info("CommandExecJob {} 执行开始", jobKey);
		//获取任务数据
		String command = (String) context.getJobDetail().getJobDataMap().get(jobKey);
		log.debug("command in CommandExecJob is:{}", command);
		//启动命令行执行线程
		new Thread(new RuntimeThread(command, null)).start();
		
		log.info("CommandExecJob {} 执行开始", jobKey);
	}
}
