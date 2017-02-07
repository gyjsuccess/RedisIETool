package com.d5.redis.client.service;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.redis.client.integration.PropertyFile;
import com.d5.redis.client.integration.key.RestoreKey;

public class ImportService {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String file;
	private int id;
	private int db;
	
	public ImportService(String file, int id, int db){
		this.file = file;
		this.id = id;
		this.db = db < 0 ? 0 : db;
	}
	
	public void importFile() throws IOException {
		int maxid = Integer.valueOf(PropertyFile.readMaxId(file, Constant.MAXID));
		for(int i = 0 ; i < maxid; i++) {
			String key = PropertyFile.read(file, Constant.KEY + i);
			String value = PropertyFile.read(file, Constant.VALUE + i);
			log.info("key is :{}", key);
			RestoreKey command = new RestoreKey(id, db, key, value.getBytes(Constant.CODEC));
			command.execute();
		}
		
	}
}
