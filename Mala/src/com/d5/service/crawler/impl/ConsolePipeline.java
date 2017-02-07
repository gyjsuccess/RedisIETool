package com.d5.service.crawler.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.d5.crawler.ResultItems;
import com.d5.service.crawler.Pipeline;
import com.d5.service.crawler.Task;
import com.d5.util.KafkaUtil;

public class ConsolePipeline implements Pipeline {

	@Override
	public void process(List<ResultItems> resultItemsList, Task task) {
		for(ResultItems reItems : resultItemsList){
			System.out.println(StringUtils.join("page: ", reItems.getRequest().getUrl(), "; data is:"));
			for (Document doc : reItems.getItems()) {
				System.out.println(doc.toString());
	        }
		}
	}
}
