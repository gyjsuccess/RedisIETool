package com.d5.redis.client.integration;

import java.util.SortedSet;
import java.util.TreeSet;

import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.integration.server.QueryServerVersion;

public abstract class JedisCommandFactory {
	private int id;
	protected SortedSet<JedisCommand> commands = new TreeSet<JedisCommand>();
	
	public JedisCommandFactory(int id) {
		this.id = id;
	}
	
	public JedisCommand getCommand() {
		QueryServerVersion queryVersion = new QueryServerVersion(id);
		queryVersion.execute();
		RedisVersion version = queryVersion.getVersionInfo();
		
		for (JedisCommand command: commands) {
			if (command.getSupportVersion().getVersion() <= version.getVersion()) {
				return command;
			}
		}
		throw new RuntimeException(I18nFile.getText(I18nFile.VERSIONNOTSUPPORT));
	}
}
