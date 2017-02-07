package com.d5.redis.client.domain;

import com.d5.redis.client.integration.I18nFile;

public enum NodeType {
	ROOT, SERVER, DATABASE, CONTAINER, STRING, HASH, LIST, SET, SORTEDSET;

	@Override
	public String toString() {
		if (this.name() == SERVER.name())
			return I18nFile.getText(I18nFile.SERVER);
		else if (this.name() == DATABASE.name())
			return I18nFile.getText(I18nFile.DATABASE);
		else if (this.name() == CONTAINER.name())
			return I18nFile.getText(I18nFile.CONTAINER);
		else if (this.name() == STRING.name())
			return I18nFile.getText(I18nFile.STRING);
		else if (this.name() == HASH.name())
			return I18nFile.getText(I18nFile.HASH);
		else if (this.name() == LIST.name())
			return I18nFile.getText(I18nFile.LIST);
		else if (this.name() == SET.name())
			return I18nFile.getText(I18nFile.SET);
		else if (this.name() == SORTEDSET.name())
			return I18nFile.getText(I18nFile.ZSET);
		else 
			return "";
		
	}
}
