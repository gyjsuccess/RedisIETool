package com.d5.service.crawler.impl;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.d5.crawler.ResultItems;
import com.d5.service.crawler.Pipeline;
import com.d5.service.crawler.Task;
import com.d5.util.KafkaUtil;

public class KafkaPipeline implements Pipeline {
	@Override
	public void process(List<ResultItems> resultItemsList, Task task) {
		for(ResultItems reItems : resultItemsList){
			for (Document doc : reItems.getItems()) {
				Document data = new Document();
				filter(doc, data, task);
				KafkaUtil.sendDataToKafka(task.getSite().getKafkaTopic(), data.toString());
	        }
		}
	}

	private void filter(Document doc, Document data, Task task){
		List<String> kafkaColumns = Arrays.asList(task.getSite().getKafkaCols4New());
		if (doc.containsKey("isNew") && ("2".equals(doc.getString("isNew"))
				|| "0".equals(doc.getString("isNew")))) {
			kafkaColumns = Arrays.asList(task.getSite().getKafkaCols4Old());
		}
		for(String key : doc.keySet()){
			if(kafkaColumns.contains(key)){
				data.append(key, doc.get(key));
			}
		}
	}
}
