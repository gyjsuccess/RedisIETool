package com.d5.service.crawler.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.ThreadSafe;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.crawler.ResultItems;
import com.d5.service.crawler.Pipeline;
import com.d5.service.crawler.Task;
import com.d5.util.FilePersistentBase;

/**
 * Store results in files.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
public class FilePipeline extends FilePersistentBase implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * create a FilePipeline with default path"/data/webmagic/"
     */
    public FilePipeline() {
        setPath("/data/webmagic/");
    }

    public FilePipeline(String path) {
        setPath(path);
    }

    @Override
    public void process(List<ResultItems> resultItemsList, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        try {
        	int indx = 0;
        	for(ResultItems reItems : resultItemsList){
        		indx ++;
				PrintWriter printWriter = new PrintWriter(
						new OutputStreamWriter(
								new FileOutputStream(getFile(StringUtils.join(path,
										DigestUtils.md5Hex(reItems.getRequest().getUrl()), "_", indx, ".html"))),
								"UTF-8"));
	            printWriter.println("url:\t" + reItems.getRequest().getUrl());
	            for (Document doc : reItems.getItems()) {
	            	printWriter.println(doc.toString());
	            }
	            printWriter.close();
    		}
			
        } catch (IOException e) {
            logger.warn("write file error", e);
        }
    }
}
