package com.command.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.command.common.Constants;

public class RuntimeThread implements Runnable{
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String command;
	private String workPathStr;
	
	public RuntimeThread(String command, String workPathStr) {
		super();
		this.command = command;
		this.workPathStr = workPathStr;
	}

	@Override
	public void run() {
		log.info("RuntimeThread run start.");
		Runtime rt = null;
		Process proc = null;
		try {
			log.debug("inited command is :{}", command);
			command = StringUtils.isNotBlank(command) ? command : Constants.getConfig().getProperty("command", "");
			if(StringUtils.isNotBlank(command)){
				log.debug("command is :{}", command);
				rt = Runtime.getRuntime();
				File workPath = null;
				workPathStr = StringUtils.isNotBlank(workPathStr)? workPathStr : Constants.getConfig().getProperty("workpath", "");
				if(StringUtils.isNotBlank(workPathStr)){
					workPath = new File(workPathStr);
					
					//工作目录存在，且是目录
					if(workPath.exists() && workPath.isDirectory()){
						proc = rt.exec(genCommand(command), null, workPath);
					} else {
						proc = rt.exec(genCommand(command));
					}
				} else {
					proc = rt.exec(genCommand(command));
				}
			}
			receiveSTDOUT(proc);
			receiveSTDERR(proc);
			proc.waitFor();
		} catch (Exception e) {
			log.error("{}", e);
		} finally {
			if(proc != null){
				log.info("destroy proc");
				proc.destroy();
			}
		}
		
		log.info("RuntimeThread run end.");
	}

	private String[] genCommand(String cmd){
		String[] cArr = new String[3];
		String os = "L";
		String cOs = Constants.getConfig().getString("os", "");
		if(StringUtils.isNotBlank(cOs) && cOs.length() == 1){
			os = cOs;
		}
		
		if(StringUtils.equalsIgnoreCase("w", os)){
			cArr[0] = "cmd";
			cArr[1] = "/c";
		}
		
		if(StringUtils.equalsIgnoreCase("l", os)){
			cArr[0] = "/bin/sh";
			cArr[1] = "-c";
		}
		
		cArr[2] = cmd;
		return cArr;
	}
	
	private void receiveSTDOUT(Process process){
		InputStream inputStream = null;
		BufferedReader bufferedRreader = null;
		String line = null;
		try {
			inputStream = process.getInputStream();
			bufferedRreader = new BufferedReader(new InputStreamReader(inputStream));
			if ((line = bufferedRreader.readLine()) != null) {
				log.info(line);
			}
		} catch (Exception e) {
			log.error("{}", e);
		} finally {
			try {
				if(inputStream != null){
					inputStream.close();
				}
				if(bufferedRreader != null){
					bufferedRreader.close();
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
	
	private void receiveSTDERR(Process process){
		InputStream inputStream = null;
		BufferedReader bufferedRreader = null;
		String line = null;
		try {
			inputStream = process.getErrorStream();
			bufferedRreader = new BufferedReader(new InputStreamReader(inputStream));
			if ((line = bufferedRreader.readLine()) != null) {
				log.error(line);
			}
		} catch (Exception e) {
			log.error("{}", e);
		} finally {
			try {
				if(inputStream != null){
					inputStream.close();
				}
				if(bufferedRreader != null){
					bufferedRreader.close();
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
}
