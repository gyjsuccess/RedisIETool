package com.d5.redis.client.service;

import com.d5.redis.client.integration.I18nFile;

public class KeyNotExistException extends RuntimeException {
	private static final long serialVersionUID = 958469726423878744L;
	protected int id;
	protected int db;
	private String key;
	
	public KeyNotExistException(int id, int db, String key){
		this.id = id;
		this.db = db;
		this.key = key;
	}

	@Override
	public String getMessage() {
		return I18nFile.getText(I18nFile.KEYNOTEXIST)+": "+key;
	}
}
