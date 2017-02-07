package com.proxy.ip;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.proxy.ip.thread.ExecuteThread;
import com.proxy.ip.util.IPTestUtil;
import com.proxy.ip.util.JedisIPPortUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MainVote {

	private static Logger log = LoggerFactory.getLogger(IPTestUtil.class);
	public static void main(String[] args) {
		
		
		/*log.info("args is:{}", args);
		String[] _args = null;
		_args = args[0].split(",");
//		_args = new String[]{"2", "2", "ipp_all"};
		for(String s : _args){
			log.info("_args has:{}", s);
		}
		int type = StringUtils.isNumeric(_args[0])?Integer.parseInt(_args[0]):0;
		log.info("type is:{}", type);
		//从文件的json数据中获取四川的代理ip。并处理重复数据。
		if(type==1) {
			if(_args.length < 2 || !_args[1].endsWith(".txt")){
				return;
			}
			//"D:\\Program Files (x86)\\Tencent\\QQ\\602928621\\FileRecv\\云代理 (4).txt"
			genScProxyIp(_args[1]);
		}
		
		//从文件的json数据中获取四川的代理ip。并处理重复数据。
		if(type==3) {
			if(_args.length < 2 || !_args[1].endsWith(".txt")){
				return;
			}
			//"D:\\Program Files (x86)\\Tencent\\QQ\\602928621\\FileRecv\\云代理 (4).txt"
			genScProxyIpWithCheck(_args[1]);
		}
		
		//redis--vote读取投票一次
		if(type==2) {
			if(_args.length < 2 || !StringUtils.isNumeric(_args[1])){
				return;
			}
			Constants.REDIS_LIST_4_ALL = _args[2];
			vote(Constants.REDIS_LIST_4_ALL, Integer.parseInt(_args[1]));
		}
		
		//读入50w数据
		if(type==4) {
			readAll();
		}*/
		readAll();
		//文件读取投票一次
		//voteOnceFileSource();
		
		//统计个数
		//getTotal();
		//去掉重复
		//dealRepeat();
		//读入静态ip
		//readStaticIpIntoRedis();
	}

	private static void readAll() {
		try {
			List<String> all = FileUtils.readLines(new File("D:\\ip.txt"), "utf-8");
			JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
			for(String ip:all){
				jedis.lpush("IPPALL_LIST", ip, 5);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void genScProxyIpWithCheck(final String fileName) {
		Thread tscip = new Thread(new Runnable() {
			@Override
			public void run() {
				JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
				HashMap<String, Integer> map = Maps.newHashMap();
				List<String> ipPorts = jedis.lpopList("voteip4sc", 5);
				try {
					List<String> ipArr = FileUtils.readLines(new File(fileName), "utf-8");
					for(String ipPort : ipArr){
						String response = ""/*IPTestUtil.ipTest(url, ip, port)*/;
						if(response.contains("四川")){
							if(!ipPorts.contains(ipPort)){
								map.put(ipPort, 1);
							}
						}
					}
					for(Map.Entry<String, Integer> en : map.entrySet()){
						jedis.lpush("voteip4sc", en.getKey(), 5);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		tscip.start();
	}

	private static void readStaticIpIntoRedis() {
		JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		try {
			List<String> ipList = FileUtils.readLines(new File("data/ip.txt"), "utf-8");
			HashMap<String, Integer> map = Maps.newHashMap();
			for(String ip : ipList){
				map.put(ip, 1);
			}
			for(Map.Entry<String, Integer> en : map.entrySet()){
				jedis.lpush("ip4static", en.getKey(), 5);
			}
		} catch (IOException e) {
			log.error("{}", e);
		}
	}

	private static void dealRepeat() {
		Thread tr = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
					HashMap<String, Integer> map = Maps.newHashMap();
					List<String> ipPorts = jedis.lpopList("voteonce4succ", 5);
					while(true){
						String ipPort = jedis.rpop("voteonce", 5);
						if(ipPort == null){
							break;
						}
						if(!ipPorts.contains(ipPort)){
							map.put(ipPort, 1);
						}
					}
					for(Map.Entry<String, Integer> en : map.entrySet()){
						jedis.lpush("voteonce4succ", en.getKey(), 5);
					}
					
					try {
						Thread.sleep(5 * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		tr.start();
	}

	private static void getTotal() {
		JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
		HashMap<String, Integer> map = Maps.newHashMap();
		List<String> ipPorts = jedis.lpopList("voteonce", 5);
		for(String ipPort : ipPorts){
			map.put(ipPort, 1);
		}
		System.out.println(map.size());
	}

	private static void voteOnceFileSource() {
		String url = "http://58.68.240.132/sctop10_vote/index1.php";
		int threadSize = 5000;
		ExecutorService service = Executors.newFixedThreadPool(threadSize);
		try {
			List<String> ipPorts = FileUtils.readLines(new File("D:\\Program Files (x86)\\Tencent\\QQ\\602928621\\FileRecv\\云代理18.txt"), "utf-8");
			for(String ipPort : ipPorts){
				try{
					String[] arr = ipPort.split(":");
					String ip = arr[0];
					String port = arr[1];
					//service.submit(new ExecuteThread(ip, port, url));
				}catch (Exception e) {
					log.error("{}", e);
				}
			}
		} catch (IOException e1) {
			log.error("{}", e1);
		}
	}

	private static void vote(final String keyName, final int threadSize) {
		Thread tvote = new Thread(new Runnable() {
			@Override
			public void run() {
				
				String url = "http://58.68.240.132/sctop10_vote/index1.php";
				ExecutorService service = Executors.newFixedThreadPool(threadSize);
				
				JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
//				while(true){
//					List<String> ipPorts = jedis.lpopList(keyName, 5);
					try{
						//for(String ipPort : ipPorts){
							/*String ipPort = jedis.rpop(keyName, 5);
							String[] arr = ipPort.split(":");
							String ip = arr[0];
							String port = arr[1];*/
							int size = threadSize;
							while(size>0){
								service.submit(new ExecuteThread(null, null, url, jedis));
								Thread.sleep(200);
								size--;
							}
							//Thread.sleep(200);
						//}
						//ipPorts.clear();
						//Thread.sleep(1000 * 60 * 10);
					}catch (Exception e) {
						log.error("{}", e);
					}
//				}
			}
		});
		tvote.start();
	}

	private static void genScProxyIp(final String fileName) {
		Thread tscip = new Thread(new Runnable() {
			@Override
			public void run() {
				JedisIPPortUtil jedis = JedisIPPortUtil.getInstance().init("10.9.201.194", 6379);
				HashMap<String, Integer> map = Maps.newHashMap();
				List<String> ipPorts = jedis.lpopList("voteip4sc", 5);
				try {
					JSONArray ipArr = JSONArray.fromObject(FileUtils.readFileToString(new File(fileName), "gbk"));
					for(int i=0; i<ipArr.size(); i++){
						JSONObject obj = ipArr.getJSONObject(i);
						if(obj.getString("Country").contains("四川")){
							String ipPort = StringUtils.join(obj.getString("Ip"), ":", obj.getString("Port"));
							if(!ipPorts.contains(ipPort)){
								map.put(ipPort, 1);
							}
						}
					}
					log.info("map.size() is :{}", map.size());
					for(Map.Entry<String, Integer> en : map.entrySet()){
						jedis.lpush("voteip4sc", en.getKey(), 5);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		tscip.start();
	}

	private static List<String> getIPPorts(String filePath) {
		try {
			return FileUtils.readLines(new File(filePath), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Lists.newArrayList();
	}
}
