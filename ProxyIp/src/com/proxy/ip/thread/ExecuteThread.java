package com.proxy.ip.thread;

import com.proxy.ip.Constants;
import com.proxy.ip.util.IPTestUtil;
import com.proxy.ip.util.JedisIPPortUtil;

public class ExecuteThread implements Runnable {
	private String ip;
	private String port;
	private String url;
	private JedisIPPortUtil jedis;
	public ExecuteThread(String ip, String port, String url, JedisIPPortUtil jedis) {
		super();
		this.ip = ip;
		this.port = port;
		this.url = url;
		this.jedis = jedis;
	}
	@Override
	public void run() {
		try {
			while(true){
				String ipPort = jedis.rpop(Constants.REDIS_LIST_4_ALL, 5);
				String[] arr = ipPort.split(":") ;
				String ip = arr[0];
				String port = arr[1];
				IPTestUtil.ipTest(url, ip, port);
				Thread.sleep(100 * 5);
				ipPort = null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
