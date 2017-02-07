package com.d5.crawler;

import java.util.List;

import org.bson.Document;

import com.google.common.collect.Lists;

public class ResultItems {
	private Request request;
    private boolean skip;
    private List<Document> items;
	private String collectName;
    
    public Request getRequest() {
        return request;
    }

    public ResultItems setRequest(Request request) {
        this.request = request;
        return this;
    }
    
    public List<Document> getItems(){
    	if(this.items == null){
    		items = Lists.newArrayListWithCapacity(10);
    	}
    	return this.items;
    }
    
    public ResultItems addItems(List<Document> items){
    	if(this.items == null){
    		this.items = Lists.newArrayListWithCapacity(10);
    	}
    	this.items.addAll(items);
    	return this;
    }
    
    /**
     * Whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @return whether to skip the result
     */
    public boolean isSkip() {
        return skip;
    }


    /**
     * Set whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @param skip whether to skip the result
     * @return this
     */
    public ResultItems setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

	public ResultItems setCollectName(String collectName) {
		this.collectName = collectName;
		return this;
	}

	public String getCollectName() {
		return this.collectName;
	}
    
    
}
