package com.d5.thread;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MonitorThreadPoolExecutor extends ScheduledThreadPoolExecutor {

	public MonitorThreadPoolExecutor(int corePoolSize) {

		super(corePoolSize);

	}

	protected void beforeExecute(Thread paramThread, Runnable paramRunnable) {

		System.out.println("work_task before:" + paramThread.getName());

	}

	protected void afterExecute(Runnable r, Throwable t) {

		super.afterExecute(r, t);

		System.out.println("work_task after worker thread is :" + r);

	}

	protected void terminated() {

		System.out.println("terminated getCorePoolSize:" + this.getCorePoolSize() + "；getPoolSize:" + this.getPoolSize()
				+ "；getTaskCount:" + this.getTaskCount() + "；getCompletedTaskCount:"

				+ this.getCompletedTaskCount() + "；getLargestPoolSize:" + this.getLargestPoolSize() + "；getActiveCount:"
				+ this.getActiveCount());

		System.out.println("ThreadPoolExecutor terminated:");

	}

}