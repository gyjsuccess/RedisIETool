package com.d5.demos;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.google.common.collect.Lists;

public class Demo010 {

	public static void main(String[] args) throws Exception {
		Logger log = LoggerFactory.getLogger(Demo010.class);
		// First we must get a reference to a scheduler
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();
		// jobs can be scheduled before sched.start() has been called
		
		List<String> classCronList = Lists.newArrayList();
		classCronList.add("com.d5.demos.SimpleJob:0/2 * * * * ?");
		classCronList.add("com.d5.demos.SimpleJob:0/5 * * * * ?");
		
		JobDetail job = null;
		CronTrigger trigger = null;
		Date ft = null;
		for(String classCron : classCronList){
			String className = classCron.replaceAll(":.*", "");
			String cronExpression = classCron.replaceAll(".*:", "");
			// job 1 will run every 20 seconds
			job = newJob(Class.forName(className).asSubclass(Job.class))
					.withIdentity("job" + Constants.QUARTZ_JOB_INDEX.getAndIncrement(), Constants.QUARTZ_GROUP_NAME).build();

			trigger = newTrigger()
					.withIdentity("trigger" + Constants.QUARTZ_TRIGGER_INDEX.getAndIncrement(), Constants.QUARTZ_GROUP_NAME)
							.withSchedule(cronSchedule(cronExpression)).build();

			ft = sched.scheduleJob(job, trigger);
			log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
					+ trigger.getCronExpression());
			
		}
		sched.start();
	}

}
