package com.d5.redis.client.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.redis.client.domain.ContainerKey;
import com.d5.redis.client.domain.Node;
import com.d5.redis.client.integration.PropertyFile;
import com.d5.redis.client.integration.key.DumpKey;

public class ExportService {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String file;
	private int id;
	private int db;
	private ContainerKey containerKey;
	private NodeService service = new NodeService();
	
	public ExportService(String file, int id, int db, ContainerKey containerKey){
		log.info("file is:{}", file);
		this.file = file;
		this.id = id;
		this.db = db < 0 ? 0 : db;
		this.containerKey = containerKey;
	}
	
	public void export() throws IOException {
		File exportFile = new File(file);
		if(exportFile.exists())
			exportFile.delete();
		if(!containerKey.isKey()){
			Set<Node> keys = service.listContainerAllKeys(id, db, containerKey.getContainerKey());
			
			for(Node node: keys) {
				log.info("key is :{}", node.getKey());
				exportOneKey(node.getKey());
			}
		}else{
			log.info("key is :{}", containerKey.getContainerKey());
			exportOneKey(containerKey.getContainerKey());
		}
	}
	
	public void export(String filterType, List<String> filterKeyList) throws IOException {
		File exportFile = new File(file);
		if(exportFile.exists())
			exportFile.delete();
		if(!containerKey.isKey()){
			Set<Node> keys = service.listContainerAllKeys(id, db, containerKey.getContainerKey());
			
			for(Node node: keys) {
				String key = node.getKey();
				if("retain".equalsIgnoreCase(filterType)
						&& !filterKeyList.contains(key)){
					continue;
				}
				
				if("discard".equalsIgnoreCase(filterType)
						&& filterKeyList.contains(key)){
					continue;
				}
				log.info("key is :{}", key);
				exportOneKey(key);
			}
		}else{
			log.info("key is :{}", containerKey.getContainerKey());
			exportOneKey(containerKey.getContainerKey());
		}
	}

	private void exportOneKey(String key) throws IOException,
			UnsupportedEncodingException {
		DumpKey command = new DumpKey(id, db, key);
		command.execute();
		byte[] value = command.getValue();
		String id = PropertyFile.readMaxId(file, Constant.MAXID);
		PropertyFile.write(file, Constant.KEY+id, key);
		PropertyFile.write(file, Constant.VALUE+id, new String(value,Constant.CODEC));
		
		int maxid = Integer.parseInt(id) + 1;
		PropertyFile.write(file, Constant.MAXID, String.valueOf(maxid));
	}
}
 