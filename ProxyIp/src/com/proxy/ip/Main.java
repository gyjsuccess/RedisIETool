package com.proxy.ip;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.ip.util.IPTestUtil;

public class Main {

	private static Logger log = LoggerFactory.getLogger(IPTestUtil.class);
	public static void main(String[] args) throws InterruptedException {
		String type = args.length > 0?args[0]:"0";
		if("1".equals(type)){
			try {
				GetProxyIP.main(args);
			} catch (IOException e) {
				log.error("{}", e.getMessage());
			} catch (Exception e) {
				log.error("{}", e.getMessage());
			}
		}
		
		if("2".equals(type)){
			try {
				GetProxyIP4Dx.main(args);
			} catch (IOException e) {
				log.error("{}", e.getMessage());
			} catch (Exception e) {
				log.error("{}", e.getMessage());
			}
		}
	}
}
