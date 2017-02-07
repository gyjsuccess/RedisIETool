package com.command.util;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.command.common.Constants;

public class QuartzUtil {
	private final List<String> classCronList = new ArrayList();
	private Scheduler sched;
	private Logger log = LoggerFactory.getLogger(QuartzUtil.class);
	
	private QuartzUtil() {}
	
	public static QuartzUtil getInstance() {
		return new QuartzUtil();
	}
	
	private void init(){
		try {
			// First we must get a reference to a scheduler
			SchedulerFactory sf = new StdSchedulerFactory();
			sched = sf.getScheduler();
			// jobs can be scheduled before sched.start() has been called
		} catch (Exception e) {
			log.error("{}", e);
		}
	}
	
	public QuartzUtil addClassCron(String classCron) {
		classCronList.add(classCron);
		return this;
	}
	
	public QuartzUtil addClassCrons(List<String> _classCronList) {
		classCronList.addAll(_classCronList);
		return this;
	}
	
	public QuartzUtil genJobs(Object obj){
		init();
		
		JobDetail job = null;
		CronTrigger trigger = null;
		Date ft = null;
		try {
			for(String classCron : classCronList){
				String className = classCron.replaceAll(":.*", "");
				String cronExpression = classCron.replaceAll(".*:", "");
				job = newJob(Class.forName(className).asSubclass(Job.class))
						.withIdentity("job" + Constants.QUARTZ_JOB_INDEX.getAndIncrement(), Constants.QUARTZ_GROUP_NAME).build();

				trigger = newTrigger()
						.withIdentity("trigger" + Constants.QUARTZ_TRIGGER_INDEX.getAndIncrement(), Constants.QUARTZ_GROUP_NAME)
								.withSchedule(cronSchedule(cronExpression)).build();
				
				if(obj != null){
					job.getJobDataMap().put(job.getKey().toString(), obj);
				}

				ft = sched.scheduleJob(job, trigger);
				log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
						+ trigger.getCronExpression());
				
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		
		return this;
	}
	
	public QuartzUtil shutDown(boolean waitForJobsToComplete){
		try {
			if(sched != null){
				if(!sched.isShutdown()){
					sched.shutdown(waitForJobsToComplete);
				}
			} else {
				throw new NullPointerException();
			}
		} catch (SchedulerException e) {
			log.error("{}", e);
		}
		return this;
	}

	public QuartzUtil start(){
		try {
			if(sched != null){
				sched.start();
			} else {
				throw new NullPointerException();
			}
		} catch (SchedulerException e) {
			log.error("{}", e);
		}
		return this;
	}
}
