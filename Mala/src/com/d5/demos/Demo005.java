package com.d5.demos;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.d5.thread.MonitorThreadPoolExecutor;

public class Demo005 {
	public static void main(String[] args) {
		ScheduledExecutorService executorService = new MonitorThreadPoolExecutor(3);
		executorService.scheduleWithFixedDelay(new Thread(new Runnable(){
			@Override
			public void run() {
				System.out.println(StringUtils.join("Thread -- Runnable---:", Thread.currentThread().getName()));
				try {
					new Thread().sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}), 5, 5, TimeUnit.SECONDS);
		
		executorService.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				System.out.println(StringUtils.join("Runnable---:", Thread.currentThread().getName()));
				try {
					new Thread().sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}, 0, 2, TimeUnit.SECONDS);
		
		for(int inx=0; inx<"Runnable".length(); inx++){
			executorService.execute(new Runnable(){
				@Override
				public void run() {
					System.out.println(StringUtils.join("Runnable -- one---:", Thread.currentThread().getName()));
					try {
						new Thread().sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			});
		}
	}
}
