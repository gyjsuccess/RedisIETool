package com.d5.redis.client.integration.key;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.d5.redis.client.domain.ContainerKey;
import com.d5.redis.client.domain.Node;
import com.d5.redis.client.domain.NodeType;
import com.d5.redis.client.domain.RedisVersion;
import com.d5.redis.client.dto.Order;
import com.d5.redis.client.integration.ConfigFile;
import com.d5.redis.client.integration.JedisCommand;

public class ListContainers extends JedisCommand {
	private int db;
	private String key;
	private Set<Node> containers = new TreeSet<Node>();
	private Order order;
	private boolean flat;
	private final String separator = ConfigFile.getSeparator();
	
	public ListContainers(int id, int db, String key, boolean flat, Order order) {
		super(id);
		this.db = db;
		this.key = key;
		this.order = order;
		this.flat = flat;
	}
	
	public ListContainers(int id, int db, String key, boolean flat) {
		super(id);
		this.db = db;
		this.key = key;
		this.order = Order.Ascend;
		this.flat = flat;
	}

	@Override
	public void command() {
		jedis.select(db);
		Set<String> nodekeys = null;
		int length;
		if (key != null) {
			nodekeys = jedis.keys(key + "*");
			length = key.length();
		} else {
			nodekeys = jedis.keys("*"+separator+"*");
			length = 0;
		}

		if(!flat){
			Iterator<String> it = nodekeys.iterator();
			while (it.hasNext()) {
				String[] ckey = it.next().substring(length).split(separator);
				if (ckey.length > 1) {
					NodeType nodeType = NodeType.CONTAINER;
	
					Node node = new Node(id, db, ckey[0], nodeType, order);
					containers.add(node);
				}
			}
		}else{
			Iterator<String> it = nodekeys.iterator();
			while (it.hasNext()) {
				String ckey = it.next().substring(length);
				ContainerKey containerKey = new ContainerKey(ckey);
				String container = containerKey.getContainerOnly();
				
				if (container.length() > 0) {
					NodeType nodeType = NodeType.CONTAINER;
	
					Node node = new Node(id, db, container, nodeType, order);
					containers.add(node);
				}
			}
		}

	}

	public Set<Node> getContainers() {
		return containers;
	}

	@Override
	public RedisVersion getSupportVersion() {
		return RedisVersion.REDIS_1_0;
	}

}
