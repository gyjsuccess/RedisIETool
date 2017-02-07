package com.d5.tool;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.eclipse.jetty.util.UrlEncoded;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Lists;
import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.crawler.Page;
import com.d5.crawler.Request;
import com.d5.crawler.ResultItems;
import com.d5.factory.GetTemplateScriptFactory;
import com.d5.service.crawler.Task;
import com.d5.util.CommonUtil;
import com.d5.util.Dom4jUtil;
import com.d5.util.HttpConstant;
import com.d5.util.MatchUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * xml格式的解析配置模板文件
 * 
 * xml文档加载处，要增加文档结构检查！
 */
public class XmlSelectorConfParser {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Task task;
    
    private XmlSelectorConfParser(){
    }
    
    public XmlSelectorConfParser(Task task){
    	this.task = task;
    }
    /**
     * 解析传入的Page信息，输出url集合和数据集合。
     *
     * @param info
     * @return
     */
    public void parser(Page page){
    	//String info = page.getHtml();
    	Request request = page.getRequest();
    	//准备
        log.debug("HtmlParserImpl 执行中");
        JSONObject dataObj = JSONObject.fromObject(request.getExtra(Request.DATAS));
        dataObj.put(Enums.JsonColums.Url.toString(), request.getUrl());
        
        String htmlSources = page.getHtml();
        //log.debug("htmlSources length is :{}", htmlSources.length());
        //log.debug("htmlSources is :{}", htmlSources);
        int sourceLength = htmlSources.length();
        String script = GetTemplateScriptFactory.getTemplateScript(dataObj.toString());
        if(StringUtils.isBlank(script)){
            log.debug("获取解析脚本失败。 request.getUrl() is:" + request.getUrl());
            return;
        }
        
        //解析
        org.dom4j.Element rootElement = Dom4jUtil.getRootElement(script);
        List<org.dom4j.Element> templates = rootElement.elements("template");
        for(org.dom4j.Element template : templates){
        	Map<String, String> tempMap = new HashMap<String, String>();
	        genTempData(dataObj, template, tempMap);
	        
        	String onlyPage = template.elementTextTrim("onlypage");
        	if(StringUtils.isNotBlank(onlyPage)){
        		String curPageNum = cleanValue(template.element("onlypage"), tempMap.get("url"), "");
        		if(!onlyPage.equals(curPageNum)){
        			continue;
        		}
        	}
	        //org.dom4j.Element template = rootElement.element("template");
	        String collectName = template.elementTextTrim("collectname");
	        //String queueName = template.elementTextTrim("queuename");
	        String sourceDataType = template.elementTextTrim("datatype").toLowerCase();
	        List<org.bson.Document> dataContainer = new LinkedList<org.bson.Document>();
	        List<Map<String, String>> urlContainer = new ArrayList<Map<String, String>>(10);
	        org.bson.Document data = InitData(dataObj, template);
	        
	        if("mala_article_info_collect".equals(collectName)){
        		log.debug("collectName is:{}", collectName);
        	}
	        if("html".equals(sourceDataType)){
	        	org.dom4j.Element sdElement = template.element("sourcedeal");
	            if(sdElement != null){
	        		htmlSources = cleanValue(sdElement, htmlSources, "");
	        	}
	        	Document doc = Jsoup.parse(htmlSources);
	        	parseGetData(doc.select("html"), 0, dataContainer, urlContainer, template, data, tempMap);
	        }else{
	        	String dataString = (String) SerializationUtils.clone(htmlSources);
	            org.dom4j.Element sdElement = template.element("sourcedeal");
	            if(sdElement != null){
	            	dataString = cleanValue(sdElement, dataString, "");
	            }
	            JSONObject dataJson = JSONObject.fromObject(dataString);
	            dataString = null;
	            parseGetData(dataJson, 0, dataContainer, urlContainer, template, data, tempMap);
	        }
	        log.debug("urlContainer.size() is :{}", urlContainer.size());
	        getNextPage(urlContainer, template, dataObj, sourceLength, tempMap);
	        
	        boolean isEnd = contentFilter(dataContainer, tempMap, template);
	        log.debug("urlContainer.size() is :{}", urlContainer.size());
	        //解析结果序列化
	        if(StringUtils.isNotBlank(collectName) && dataContainer.size() > 0){
	        	page.getResultItemsList().add(new ResultItems().setCollectName(collectName).addItems(dataContainer));
	        }
	        log.debug("urlContainer.size() is :{}", urlContainer.size());
	        if(/*StringUtils.isNotBlank(queueName) && */urlContainer.size() > 0){
	        	page.addTargetRequests_(genTargetRequests(urlContainer, isEnd,
	        			rootElement.elementTextTrim("pattern"), page, template));
	        }
	        
	        savePageNum(template, tempMap);
        }
        return ;
    }

    /**
     * 增量抓取时，获取上次爬取的最大页码，并生成相应页码的url地址。
     * @param urlContainer
     * @param url
     * @param template
     * @param data
     */
    private void getCrawledMaxPageNum(List<Map<String, String>> urlContainer, String url,
			org.dom4j.Element template, org.bson.Document data) {
    	if(AllCrawFlagDeal.isEqual(task.getUUID(), "1")){
    		String getPageNumStr = template.elementTextTrim("getpagenum");
    		if(StringUtils.isNotBlank(getPageNumStr)){
    			org.dom4j.Element gmpnEle = template.element("getpagenum");
    			String curPageNum = cleanValue(gmpnEle, url, "");
    			if(StringUtils.isNotBlank(curPageNum) && StringUtils.equals(curPageNum, "1")){
    				String fieldName = "";
    				org.dom4j.Element repreatKeyEle = template.element("repeatkey");
        			if(repreatKeyEle != null){
        				fieldName = cleanValue(repreatKeyEle, url, "");
        			}else{
        				fieldName = getPageUrl(gmpnEle, url, "1");
        			}
    				String maxPageNum = Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
    						gmpnEle.attributeValue("repeatstore"), fieldName);
    				if(StringUtils.isNotBlank(maxPageNum) && maxPageNum.compareTo("1") > 0){
    					Map<String, String> map = new HashMap<String, String>();
    					map.put("url", getPageUrl(gmpnEle, url, maxPageNum));
    					addReferer(map, data, template);
    					urlContainer.add(map);
    					log.debug("getCrawledMaxPageNum ---addURL执行了。maxPageNum is :{}", maxPageNum);
    				}
    			}
    		}
    	}
    	log.debug("getCrawledMaxPageNum 执行了。");
	}

    /**
     * 保存当前抓取的页码值。
     * @param template
     * @param tempMap
     */
	private void savePageNum(org.dom4j.Element template, Map<String, String> tempMap) {
    	String savePageNumStr = template.elementTextTrim("savepagenum");
    	if(StringUtils.isNotBlank(savePageNumStr)){
    		org.dom4j.Element spmEle = template.element("savepagenum");
    		String curPageNum = cleanValue(spmEle, tempMap.get("url"), "");
    		if(StringUtils.isNotBlank(curPageNum)){
    			String fieldName = "";
    			org.dom4j.Element repreatKeyEle = template.element("repeatkey");
    			if(repreatKeyEle != null){
    				fieldName = cleanValue(repreatKeyEle, tempMap.get("url"), "");
    			}else{
    				fieldName = getPageUrl(spmEle, tempMap.get("url"), "1");
    			}
    			if(StringUtils.isNotBlank(fieldName)){
    				String oldNum = Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
    						spmEle.attributeValue("repeatstore"), fieldName);
    				if(StringUtils.isNotBlank(oldNum)){
    					if(compareString(oldNum, curPageNum) == 1){
    						Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
            						spmEle.attributeValue("repeatstore"), fieldName, curPageNum);
    						log.debug("savePageNum --- curPageNum={}", curPageNum);
    					}
    				}else{
    					Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
        						spmEle.attributeValue("repeatstore"), fieldName, curPageNum);
    					log.debug("savePageNum --- curPageNum={}", curPageNum);
    				}
    			}
    		}
    	}
    	log.debug("savePageNum 执行了。");
	}

	/**
	 * 数字字符串大小比较
	 * @param dest
	 * @param source
	 * @return
	 */
	private int compareString(String dest, String source) {
		if(StringUtils.isNumericSpace(dest) && StringUtils.isNumericSpace(source)){
			int destInt = Integer.parseInt(dest);
			int sourceInt = Integer.parseInt(source);
			if(destInt > sourceInt){
				return -1;
			}
			if(destInt == sourceInt){
				return 0;
			}
			if(destInt < sourceInt){
				return 1;
			}
		}
		return -2;
	}

	/**
	 * 拼接网页url地址
	 * @param spmEle
	 * @param url
	 * @param pageNum
	 * @return
	 */
	private String getPageUrl(org.dom4j.Element spmEle, String url, String pageNum) {
		String findAll = spmEle.attributeValue("findall4url");
		if(StringUtils.isNotBlank(findAll)){
			List<String> urlParts = MatchUtil.findAll(url, findAll);
			if(urlParts.size() > 1){
				return StringUtils.join(urlParts.get(0), pageNum, urlParts.get(1));
			}
			return url;
		}
		return url;
	}

	/**
	 * 数据过滤，去掉本次抓取结果中不需要的数据；关键字段判断翻页是否结束。
	 * @param dataContainer
	 * @param tempMap
	 * @param template
	 * @return
	 */
	private boolean contentFilter(List<org.bson.Document> dataContainer, Map<String, String> tempMap, org.dom4j.Element template) {
    	boolean isEnd = false;
    	org.dom4j.Element contentFilter = template.element("contentfilter");
    	if(contentFilter != null){
    		String increment = contentFilter.attributeValue("increment");
    		if(StringUtils.isNotBlank(increment) && "1".equals(increment)){
    			if(!AllCrawFlagDeal.isEqual(task.getUUID(), "1")){
    				return false;
    			}
    		}
    		String filter = contentFilter.elementTextTrim("filter");
        	if(StringUtils.isNotBlank(filter)){
        		String fieldName = tempMap.get("url");
				org.dom4j.Element repreatKeyEle = template.element("repeatkey");
    			if(repreatKeyEle != null){
    				fieldName = cleanValue(repreatKeyEle, fieldName, "");
    			}
        		if("all".equals(filter) && Integer.parseInt(Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
        					contentFilter.attributeValue("repeatstore"), fieldName)) > 0){
        			log.debug("contentFilter-----fieldName is:{}", fieldName);
        			dataContainer.clear();
        		} else {
        			List<org.bson.Document> dataContainer_ = Lists.newArrayList();
        			String[] columns = filter.split("\\|");
        			String rownotnullcol = contentFilter.element("filter").attributeValue("rownotnullcol");
        			int indx = 0;
        			
        			org.bson.Document docPre = null;
        			for(org.bson.Document doc : dataContainer){
        				if(CommonUtil.equals(docPre, doc)){
        					continue;
        				}
        				docPre = doc;
        				indx ++;
        				String removeFirstRow = template.elementTextTrim("removefirstrow");
        				if(StringUtils.isNotBlank(removeFirstRow) && "true".equals(removeFirstRow)){
        					String curPageNum = cleanValue(template.element("removefirstrow"), tempMap.get("url"), "");
            				if("1".equals(curPageNum) && indx==1){
            					continue;
            				}
        				}
        				if(StringUtils.isNotBlank(rownotnullcol)){
        					if(StringUtils.isNotBlank(doc.getString(rownotnullcol))){
        						dataContainer_.add(doc);
        					}else{
        						continue;
        					}
        				}else{
        					dataContainer_.add(doc);
        				}
        				for(String column : columns){
        					doc.remove(column);
        				}
        				boolean end = endNext(doc, contentFilter.element("endnext"), dataContainer_);
        				if(end){
        					if(!isEnd){
        						isEnd = true;
        					}
        				}
        			}
        			
        			dataContainer.clear();
        			dataContainer.addAll(dataContainer_);
        		}
        	}
    	}
    	log.debug("contentFilter 执行了");
    	return isEnd;
	}
    
	/**
	 * 增量抓取时，通过关键的字段判断新旧数据，判断翻页是否结束。
	 * @param doc
	 * @param ele
	 * @param dataContainer
	 * @return
	 */
    private boolean endNext(org.bson.Document doc, org.dom4j.Element ele, List<org.bson.Document> dataContainer){
    	log.debug("endNext： 增量爬取判断。");
    	if(!AllCrawFlagDeal.isEqual(task.getUUID(), "1") || ele == null){
    		return false;
    	}
    	
    	String keyName = task.getSite().getRedisKeyName4Crawled();
		if(StringUtils.isBlank(keyName)){
			return false;
		}
		String lastPos = Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
				keyName, task.getUUID());
		log.debug("lastPos = {}", lastPos);
		if(StringUtils.isBlank(lastPos)){
			return false;
		}
		
		String orderType = ele.attributeValue("ordertype");
		//String type = ele.attributeValue("type");
		if(compareValue(doc, ele, lastPos) < 0){
			if(StringUtils.isNotBlank(orderType) && "2".equals(orderType)){
				return true;
			}
			dataContainer.remove(doc);
		}
		
    	return false;
    }
    
    /**
     * 比值函数。比较之前可以对值进行转换。
     * @param doc
     * @param ele
     * @param lastPos
     * @return
     */
    private int compareValue(org.bson.Document doc, org.dom4j.Element ele, String lastPos){
    	log.debug("doc is:{}", doc);
    	log.debug("ele is:{}", ele);
    	log.debug("lastPos is:{}", lastPos);
    	String columnName = ele.getTextTrim();
    	if(StringUtils.isNotBlank(columnName)){
    		String value = doc.getString(columnName);
    		convertValue(value, ele.attributeValue("convert"));
    		log.debug("value = {}", value);
    		
    		return value.compareTo(lastPos);
    	}
    	return 0;
    }
    
    /**
     * 值转换函数。将值转换为特定的值。
     * @param value
     * @param eleAttr
     */
	private void convertValue(String value, String eleAttr){
    	if(StringUtils.isBlank(eleAttr)){
    		return;
    	}
    	String[] tValue = eleAttr.split("\\|");
    	String[] targets = tValue[0].split(",");
    	String[] tVals = tValue[1].split(",");
    	int inx = 0;
    	for(String t : targets){
    		if(value.equals(t)){
    			value = tVals[inx];
    			break;
    		}
    		inx ++;
    	}
    }

	/**
	 * 产生TempMap的数据
	 * @param dataObj
	 * @param template
	 * @param tempMap
	 */
	private void genTempData(JSONObject dataObj, org.dom4j.Element template,
			Map<String, String> tempMap) {
    	String tempColsStr = template.elementTextTrim("tempdata");
    	if(StringUtils.isBlank(tempColsStr)){
    		return;
    	}
    	String[] tempCols = tempColsStr.split("\\|");
    	for(String key : tempCols){
    		tempMap.put(key, getData(dataObj, key));
    	}
	}

	/**
	 * 从json中获取数据值。可以获取全部头中的数据。
	 * @param dataObj
	 * @param key
	 * @return
	 */
    private String getData(JSONObject dataObj, String key) {
    	String d1 = "";
    	if(dataObj.containsKey(key)){
    		d1 = dataObj.getString(key);
    	}
    	String d2 = "";
    	if(dataObj.containsKey("InitData")){
    		JSONObject initObj = JSONObject.fromObject(dataObj.getString("InitData"));
    		if(initObj.containsKey(key)){
    			d2 = initObj.getString(key);
    		}
    	}
    	if(StringUtils.isNotBlank(d2)){
    		return d2;
    	}
		return d1;
	}

    /**
     * 获取下一页url地址。通过一些配置进行计算页码值。使用于在页面中无法直接获取到下一页的url地址。
     * @param urlContainer
     * @param template
     * @param dataObj
     * @param sourceLength
     * @param tempMap
     */
	private void getNextPage(List<Map<String, String>> urlContainer,
			org.dom4j.Element template, JSONObject dataObj, int sourceLength,
			Map<String, String> tempMap) {
    	org.dom4j.Element pageEle = template.element("page");
    	if(pageEle == null){
    		return;
    	}
    	pageEle = pageEle.element("next");
    	String curUrl = dataObj.getString(Enums.JsonColums.Url.toString());
    	String curPageStartNum = cleanValue(pageEle, curUrl, "");
    	String expression = pageEle.elementTextTrim("expression");
    	String nextPageStartNum = Calculator.calculate(curPageStartNum + expression);
    	String maxPageNumStr = null;
    	try{
    		maxPageNumStr = pageEle.attributeValue("maxpagenum");
    		int maxPageNum = -1;
    		int nextPageNum = Integer.parseInt(nextPageStartNum);
        	if(StringUtils.isNotBlank(maxPageNumStr)){
        		maxPageNum = Integer.parseInt(maxPageNumStr);
        	}
        	int endPageNum = maxPageNum;
        	if(AllCrawFlagDeal.isEqual(task.getUUID(), "1")){
        		String endPageStr = pageEle.elementTextTrim("endpage");
            	if(StringUtils.isNotBlank(endPageStr)){
            		endPageNum = Integer.parseInt(endPageStr);
            	}
        	}
        	
        	if(nextPageNum > endPageNum){
        		log.debug("翻页结束。");
        		return;
        	}
    	}catch(Exception e){
    		log.error("getNextPage Error Is : {}", e);
    	}
    	Map<String, String> urlMap = new HashMap<String, String>();
    	String key = pageEle.elementTextTrim("key");
    	urlMap.put(Enums.JsonColums.Url.toString(), 
    			curUrl.replaceAll(StringUtils.join(key, "\\d+"), StringUtils.join(key, nextPageStartNum)));
    	urlContainer.add(urlMap);
    	log.debug("getNextPage 执行了");
	}

	/**
	 * 从header头中获取到网站通用基础数据。
	 * @param dataObj
	 * @param template
	 * @return
	 */
	private org.bson.Document InitData(JSONObject dataObj,
			org.dom4j.Element template) {
    	String initData = "{}";
        String init = template.elementTextTrim("initdata");
    	if(StringUtils.isNotBlank(init)){
    		if(dataObj.containsKey(Enums.JsonColums.InitData.toString())){
            	initData = dataObj.getString(Enums.JsonColums.InitData.toString());
            	org.bson.Document data = org.bson.Document.parse(initData);
            	String[] initArr = init.split("\\|");
            	if(initArr.length == 1 && "all".equals(initArr[0])){
            		return data;
            	}else {
            		List<String> initColumns = Arrays.asList(initArr);
            		List<String> delColumns = new ArrayList<String>();
            		for(String key : data.keySet()){
            			if(!initColumns.contains(key)){
            				delColumns.add(key);
            			}
            		}
            		for(String delKey : delColumns){
            			data.remove(delKey);
            		}
            		
            		return data;
            	}
            }
    	}
		return org.bson.Document.parse(initData);
	}

	/**
	 * 解析json格式的页面内容
	 * @param dataJson
	 * @param deepth
	 * @param dataContainer
	 * @param urlContainer
	 * @param template
	 * @param basicData
	 * @param tempMap
	 */
	private void parseGetData(JSONObject dataJson, int deepth,//只支持两层
			List<org.bson.Document> dataContainer, List<Map<String, String>> urlContainer,
			org.dom4j.Element template, org.bson.Document basicData, Map<String, String> tempMap) {
    	org.bson.Document data = SerializationUtils.clone(basicData);
    	List<org.dom4j.Element> selectors = template.elements("selector");
		for(org.dom4j.Element selector : selectors){
			data = SerializationUtils.clone(basicData);
			String selectorExpress = selector.elementTextTrim("expression");
			JSONArray objArr = new JSONArray();
			if(StringUtils.isBlank(selectorExpress)){
				objArr.add(dataJson);
			}else{
				try{
					objArr = dataJson.getJSONArray(selectorExpress);
				}catch(Exception e){
					try{
						objArr.add(dataJson.getJSONObject(selectorExpress));
					}catch(Exception e1){
						e1.printStackTrace();
						log.error("get JSONdata fail. selectorExpress is :" + selectorExpress);
					}
				}
			}
			for(int ix=0; ix<objArr.size(); ix++){
				JSONObject obj = objArr.getJSONObject(ix);
				List<org.dom4j.Element> columns = selector.elements("column");
				for(org.dom4j.Element col : columns){
					String key = col.attributeValue("name");
					String value = null;
					String type = col.attributeValue("type");
					switch(type){
						case "ID":
							value = genEntityId(col, data, tempMap);
							break;
						case "constant": 
							value = col.attributeValue("value");
							break;
						case "url":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							if(StringUtils.isNotBlank(value)){
								value = formatorValue(col, value, data, tempMap);
							}
							addUrl2Container(urlContainer, data, value, col, tempMap);
							break;
						case "URL":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							if(StringUtils.isNotBlank(value)){
								value = formatorValue(col, value, data, tempMap);
								addUrl2Container(urlContainer, data, value, col, tempMap);
							}
							value = null;
							break;
						case "htmlsource":
							value = dataJson.toString();
							break;
						case "HtmlSource":
							value = dataJson.toString();
							break;
						case "DATE":
							value = DateTime.now().toString(Constants.DATE_FORMATTER);
							break;
						case "date":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							value = CommonUtil.convertDate(value);
							break;
						case "integer":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							if(StringUtils.isNotBlank(value)){
								value = formatorValue(col, value, data, tempMap);
							}
							break;
						default:
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
					}
					if(value != null){
						if("integer".equals(type) && StringUtils.isNumeric(value)){
							data.append(key, Integer.parseInt(value));
							continue;
						}
						data.append(key, value);
					}
				}
				
				List<org.bson.Document> dataContainer_ = new ArrayList<org.bson.Document>();
				parseGetData(obj, deepth + 1, dataContainer_, urlContainer, selector, data, tempMap);
				for(org.bson.Document d : dataContainer_){
					data.putAll(d);
					if(deepth < 1 && data.size() > 0){
						dataContainer.add(SerializationUtils.clone(data));
					}
				}
				if(/*deepth > 0 && */data.size() > 0){
					dataContainer.add(SerializationUtils.clone(data));
				}
			}
			data.clear();
		}
	}
	/**
	 * 提取网页元素的相关信息作为数据值。
	 * @param selector
	 * @param obj
	 * @return
	 */
	private String getValue(org.dom4j.Element selector, JSONObject obj) {
		String selectorExpress = selector.elementTextTrim("attrname");
		if(StringUtils.isBlank(selectorExpress)){
			return "";
		}
		return obj.getString(selectorExpress);
	}

	/**
	 * 解析html格式的页面内容
	 * @param doc
	 * @param deepth
	 * @param dataContainer
	 * @param urlContainer
	 * @param template
	 * @param basicData
	 * @param tempMap
	 */
	private void parseGetData(Elements doc, int deepth,
			List<org.bson.Document> dataContainer, List<Map<String, String>> urlContainer,
			org.dom4j.Element template, org.bson.Document basicData, Map<String, String> tempMap) {
    	org.bson.Document data = SerializationUtils.clone(basicData);
    	List<org.dom4j.Element> selectors = template.elements("selector");
		for(org.dom4j.Element selector : selectors){
			boolean whole = wholeDeal(selector, data, tempMap, doc, dataContainer);
			if(whole){
				continue;
			}
			String selectorExpress = selector.elementTextTrim("expression");
			Elements curElements = new Elements();
			if(StringUtils.isBlank(selectorExpress)){
				curElements.addAll(doc);
			}else{
				//curElements.addAll(doc.select(selectorExpress));
				addElements(doc, selector, curElements);
			}
			//if("div#ct > div.pgs.mtm.mbm.cl > div > a.nxt".equals(selectorExpress)){
				log.debug(selectorExpress + "--" + curElements.size());
			//}
			//selector取到想要的数据
			for(Element ele : curElements){
				Elements singleEles = new Elements();
				singleEles.add(ele);
				List<org.dom4j.Element> columns = selector.elements("column");
				for(org.dom4j.Element col : columns){
					String key = col.attributeValue("name");
					String value = null;
					String type = col.attributeValue("type");
					switch(type){
						case "ID":
							value = genEntityId(col, data, tempMap);
							break;
						case "constant": 
							value = col.attributeValue("value");
							break;
						case "url":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							if(StringUtils.isNotBlank(value)){
								value = formatorValue(col, value, data, tempMap);
							}
							addUrl2Container(urlContainer, data, value, col, tempMap);
							break;
						case "URL":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							if(StringUtils.isNotBlank(value)){
								value = formatorValue(col, value, data, tempMap);
								addUrl2Container(urlContainer, data, value, col, tempMap);
							}
							value = null;
							break;
						case "htmlsource":
							value = curElements.html();
							break;
						case "HtmlSource":
							value = curElements.outerHtml();
							break;
						case "DATE":
							value = DateTime.now().toString(Constants.DATE_FORMATTER);
							break;
						case "date":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							value = CommonUtil.convertDate(value);
							break;
						case "integer":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
							break;
						default:
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
					}
					if(value != null){
						if("integer".equals(type) && StringUtils.isNumeric(value)){
							data.append(key, Integer.parseInt(value));
							continue;
						}
						data.append(key, value);
					}
				}
				
				List<org.bson.Document> dataContainer_ = new ArrayList<org.bson.Document>();
				parseGetData(singleEles, deepth + 1, dataContainer_, urlContainer, selector, data, tempMap);
				for(org.bson.Document d : dataContainer_){
					data.putAll(d);
				}
				
				if(/*deepth > 0 && */data.size() > 0){
					dataContainer.add(SerializationUtils.clone(data));
				}
			}
		}
	}
	
	private boolean wholeDeal(org.dom4j.Element selector, org.bson.Document data, Map<String, String> tempMap, Elements doc, List<org.bson.Document> dataContainer) {
		String wholeStr = selector.element("expression").attributeValue("whole");
		if(StringUtils.isBlank(wholeStr)){
			return false;
		} else {
			if("1".equals(wholeStr)){
				String selectorExpress = selector.elementTextTrim("expression");
				Elements curElements = new Elements();
				if(StringUtils.isBlank(selectorExpress)){
					curElements.addAll(doc);
				}else{
					//curElements.addAll(doc.select(selectorExpress));
					addElements(doc, selector, curElements);
				}
				List<org.dom4j.Element> columns = selector.elements("column");
				for(org.dom4j.Element col : columns){
					String key = col.attributeValue("name");
					String value = null;
					String type = col.attributeValue("type");
					switch(type){
						case "htmlsource":
							value = curElements.html();							
							break;
						case "HtmlSource":
							value = curElements.outerHtml();							
							break;
						case "string":
							value = getValue(col, curElements);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
							break;							
							//广电舆情--获取文章内容中图片url与图片标题--jiefeng.wen--start
						case "picTitleJsonArr":		
							JSONArray lArray = new JSONArray();							
							for(int i = 0, len = curElements.size(); i < len; i++){
								JSONObject jsonObject = new JSONObject();
								String urlStr = curElements.get(i).attr("src");
                                String titleStr = curElements.get(i).attr("alt");
                                jsonObject.put("title", "");
//                                jsonObject.put("url", "");
                                if((len-1) == i){
                                	if(StringUtils.isNotBlank(urlStr)){
                                		if(!urlStr.startsWith("http")){     //仅适用于url链接是在文章url上拼接得情况，如：广电舆情中的新华网、人民网数据获取                            			
                                			if(!urlStr.startsWith("/")){
                                				String baseUrl = data.getString("entityUrl");
                                    			int index = baseUrl.lastIndexOf('/');
                                    			baseUrl = baseUrl.substring(0, index+1);
                                    			urlStr = baseUrl + urlStr;
                                			}else{
                                				String baseUrl = data.getString("entityUrl");                                    			
                                				int index = baseUrl.indexOf('/', 7);
                                    			baseUrl = baseUrl.substring(0, index);
                                    			urlStr = baseUrl + urlStr;
                                			}                                			
                                		} 
                                		jsonObject.put("url", urlStr);
                                		if(StringUtils.isNotBlank(titleStr)){
                                			jsonObject.put("title", titleStr);
                                		}/*else {
                                			jsonObject.put("title", " ");
										}*/
                                		lArray.add(jsonObject);
                                	}
                                	break;
                                }
                                
                                if(StringUtils.isNotBlank(urlStr)){
                                	if(!urlStr.startsWith("http")){   //仅适用于url链接是在文章url上拼接得情况，如：广电舆情中的新华网、人民网数据获取                             			
                                		if(!urlStr.startsWith("/")){
                            				String baseUrl = data.getString("entityUrl");
                                			int index = baseUrl.lastIndexOf('/');
                                			baseUrl = baseUrl.substring(0, index+1);
                                			urlStr = baseUrl + urlStr;
                            			}else{
                            				String baseUrl = data.getString("entityUrl");                                    			
                            				int index = baseUrl.indexOf('/', 7);
                                			baseUrl = baseUrl.substring(0, index);
                                			urlStr = baseUrl + urlStr;
                            			}
                            		} 
                                	jsonObject.put("url", urlStr);
                                	if(StringUtils.isNotBlank(titleStr)){
                                		jsonObject.put("title", titleStr);
                                		lArray.add(jsonObject);
                                		continue;
                                	}else {
										String urlStrNext = curElements.get(i+1).attr("src");
										if(StringUtils.isBlank(urlStrNext)){
											titleStr = curElements.get(i+1).text();
											if(StringUtils.isNotBlank(titleStr)){
												jsonObject.put("title", titleStr);
											}/*else {
												jsonObject.put("title", "");
											}*/										
										}
										lArray.add(jsonObject);
										continue;
									}
                                }else{
                                	continue;
                                }								
								
							}
							value = lArray.toString();	
							break;
						//广电舆情--获取文章内容中图片url与图片标题--jiefeng.wen--end
						//广电舆情--文章关键词用逗号隔开--jiefeng.wen--start
						case "keywords":							
							StringBuffer strBuf = new StringBuffer();							
							for(Element ele:curElements){
								strBuf.append(ele.text()).append(',');
								}
							String strTemp = strBuf.toString();
							value = strTemp.substring(0, strTemp.length()-1);
						//广电舆情--文章关键词用逗号隔开--jiefeng.wen--end	
							break;
						default:							
					}
					if(value != null){						
						data.append(key, value);
					}
				}
				if(/*deepth > 0 && */data.size() > 0){
					dataContainer.add(SerializationUtils.clone(data));
				}
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 生成实体ID
	 * @param col
	 * @param data
	 * @param tempMap
	 * @return
	 */
	private String genEntityId(org.dom4j.Element col, org.bson.Document data, Map<String, String> tempMap) {
		String colName = col.attributeValue("name");
		String entityFlag = col.attributeValue("isentity");
		String entityId = "";
		boolean isEntity = false;
		if(StringUtils.isNotBlank(entityFlag) && "true".equals(entityFlag)) {
			isEntity = true;
		}
		if(data.containsKey(colName)){
			if(data.containsKey("isNew") && data.getString("isNew").equals("2")) {
				return data.getString(colName);
			}
			if(isEntity){
				entityId = data.getString(colName);
			}
		}
		org.dom4j.Element repreatKeyEle = col.element("repeatkey");
		String url = tempMap.get("url");
		String fieldName = url;
		String storageName = "";
		if(isEntity && repreatKeyEle != null){
			fieldName = cleanValue(repreatKeyEle, fieldName, "");
			storageName = repreatKeyEle.attributeValue("repeatstore");
			if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, storageName, fieldName)){
				data.put("isNew", "0");//旧帖子
				entityId = Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H,
						storageName, fieldName);
			}else{
				data.put("isNew", "1");//新帖子
				if(StringUtils.isBlank(entityId)){
					entityId = StringUtils.join(Constants.LOCAL_MAC, CommonUtil.generateShortUuid());
				}
				Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H,
						storageName, fieldName, entityId);
			}
			return entityId;
		} else {
			entityId = StringUtils.join(Constants.LOCAL_MAC, CommonUtil.generateShortUuid());
		}
		
		return entityId;
	}

	/**
	 * 将url加入到url集合中
	 * @param urlContainer
	 * @param data
	 * @param value
	 * @param col
	 * @param tempMap
	 */
	private void addUrl2Container(List<Map<String, String>> urlContainer,
			org.bson.Document data, String value, org.dom4j.Element col,
			Map<String, String> tempMap) {
		Map<String, String> map = new HashMap<String, String>();
		String url = tempMap.get("url");
		if(AllCrawFlagDeal.isEqual(task.getUUID(), "1")){
			String incrementDiscard = col.elementTextTrim("incrementdiscard");
			if(StringUtils.isNotBlank(incrementDiscard)){
				if(StringUtils.equals(incrementDiscard, 
						cleanValue(col.element("incrementdiscard"), url, ""))){
					log.debug("incrementDiscard 检查执行了。url is :{}", url);
					return;
				}
			}
			if(isReachEndPage(col, tempMap, "endpage")){
				log.debug("翻页结束。");
				return;
			}
			
		}else{
			if(isReachEndPage(col, tempMap, "maxpagenum")){
				log.debug("翻页结束。");
				return;
			}
		}
		
		getCrawledMaxPageNum(urlContainer, value, col, data);
		
		map.put("url", value);
		String headerReferer = col.attributeValue("headerreferer");
		if(StringUtils.isNotBlank(headerReferer) && "true".equals(headerReferer)){
			map.put(HttpConstant.Header.REFERER, url);
		}
		
		String repeatStore = col.attributeValue("repeatstore");
		if(StringUtils.isNotBlank(repeatStore)){
			map.put("repeatstore", repeatStore);
			String crawlRepeat = col.attributeValue("crawlrepeat");
			if(StringUtils.isNotBlank(crawlRepeat)){
				map.put("crawlrepeat", crawlRepeat);
			}else{
				map.put("crawlrepeat", "0");
			}
		}
		addReferer(map, data, col);
		urlContainer.add(map);
	}
	
	/**
	 * 添加referer相关的信息。
	 * @param map
	 * @param data
	 * @param col
	 */
	private void addReferer(Map<String, String> map, org.bson.Document data, org.dom4j.Element col) {
		String refererStr = col.attributeValue("referer");
		if(StringUtils.isNotBlank(refererStr)){
			String[] refererArr = refererStr.split("\\|");
			for(String referer : refererArr) {
				if(data.containsKey(referer)){
					map.put(referer, String.valueOf(data.get(referer)));
				}
			}
		}
	}

	/**
	 * 判断翻页是否到达了结束页。
	 * @param col
	 * @param tempMap
	 * @param tagName
	 * @return
	 */
	private boolean isReachEndPage(org.dom4j.Element col, Map<String, String> tempMap, String tagName){
		boolean res = false;
		String str = col.elementTextTrim(tagName);
		if(StringUtils.isNotBlank(str)){
			String curPageStr = cleanValue(col.element(tagName), tempMap.get("url"), "");
			int curPageNum = Integer.parseInt(curPageStr);
			int nextPageNum = curPageNum + 1;
			int endPageNum = Integer.parseInt(str);
			if(nextPageNum > endPageNum){
				res = true;
			}
		}
		
		return res;
	}
	/**
	 * 产生可以数学计算的表达式。
	 * @param nextEle
	 * @param value
	 * @return
	 */
	private String genCanCalculateExpression(org.dom4j.Element nextEle, String value) {
		String resultExp = "";
		String expression = nextEle.elementTextTrim("expression");
		if(StringUtils.isNotBlank(expression)){
			List<String> replaces = MatchUtil.findAll(expression, "\\?");
			if(replaces.size() > 0){
				resultExp = expression;
				for(String replace : replaces){
					resultExp = resultExp.replace(replace, value);
				}
			}
		}
		return resultExp;
	}

	/**
	 * 特殊处理。输出特定格式的数据值。
	 * @param col
	 * @param value
	 * @param data
	 * @param tempMap
	 * @return
	 */
	private String formatorValue(org.dom4j.Element col, String value,
			org.bson.Document data, Map<String, String> tempMap) {
		org.dom4j.Element formator = col.element("formatter");
		if(formator != null){
			String templateF = formator.elementTextTrim("template");
			log.debug("templateF--:" + templateF + ",value--:" + value);
			List<String> replaces = MatchUtil.findAll(templateF, "\\{([A-Za-z]+[0-9]*)+\\}");
			if(replaces.size() > 0){
				List<org.dom4j.Element> refArr = formator.elements("ref");
				int index = 0;
				String targetStr = templateF;
	            for(String replace : replaces){
	                if("{self}".equals(replace)){
	                    targetStr = targetStr.replace(replace, value);
	                    log.debug("targetStr--:" + targetStr + ",replace--:" + replace + ",value--:" + value);
	                    continue;
	                } else if ("{timestamp}".equals(replace)) {
	                	String timestamp = String.valueOf(DateTime.now().getMillis());
	                	targetStr = targetStr.replace(replace, timestamp);
	                	continue;
	                }else{
	                	org.dom4j.Element ref = refArr.get(index);
	                	String refValue = null;
	                	String isTempData = ref.attributeValue("tempdata");
	                	String refColName = ref.getTextTrim();
	                	if(StringUtils.isNotBlank(isTempData) && "true".equals(isTempData)){
	                		refValue = tempMap.get(refColName);
	                		if(AllCrawFlagDeal.isEqual(task.getUUID(), "1")){
	                			String isId = ref.attributeValue("isid");
	                			if(StringUtils.isNotBlank(isId) && "true".equals(isId)){
	                				org.dom4j.Element repEle = col.element("repeatkey");
	                				if(null != repEle){
	                					String fildName = cleanValue(repEle, tempMap.get("url"), "");
	                					String storageName = repEle.attributeValue("repeatstore");
	                					if(StringUtils.isNotBlank(storageName) && StringUtils.isNotBlank(fildName)){
	                						if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, storageName, fildName)){
	                							refValue = Constants.dataRedisService.getInfoFromMap(Constants.REDIS_INDEX_4_H, storageName, fildName);
	                						}
	                					}
	                				}
	                			}
	                		}
	                	}else{
	                		refValue = data.getString(refColName);
	                	}
	                	refValue = cleanValue(ref, refValue, value);
	                	log.debug("targetStr--:" + targetStr + ",replace--:" + replace + ",refValue--:" + refValue);
	                    targetStr = targetStr.replace(replace, refValue);
	                }
	                index ++;
	            }
	            
	            value = targetStr;
			}
			List<String> calExpressions = MatchUtil.findAll(templateF, "\\{&?\\w+=:\\??([\\*\\+-/]\\d+)+\\}");
			for(String calExpression : calExpressions){
				String key = MatchUtil.find(calExpression, "&?\\w+=", "");
				String expression = MatchUtil.find(calExpression, "([\\*\\+-/]\\d+)+", "");
				String url = tempMap.get("url");
				String pageNum = MatchUtil.find(url, key + "(\\d+)", "", 1);
				String nextPageNum = Calculator.calculate(pageNum + expression);
				value = value.replaceAll(StringUtils.join(key, "\\d+"), StringUtils.join(key, nextPageNum));
			}
		}
		return value;
	}
	/**
	 * html页面格式解析，获取元素相关信息作为数据值。
	 * @param selector
	 * @param doc
	 * @return
	 */
	private String getValue(org.dom4j.Element selector, Elements doc){
		String value = null;
		String selectorExpress = selector.elementTextTrim("expression");
		String attrName = selector.elementTextTrim("attrname");
		String nodeNum = selector.attributeValue("nodenum");
		Elements valueEles = new Elements();
		if(StringUtils.isBlank(selectorExpress)){
			valueEles.addAll(doc);
		}else{
			String[] cronExpressArr = selectorExpress.split("\\|");
			for(String cssQuery : cronExpressArr){
				valueEles.addAll(doc.select(cssQuery));
			}
		}
		
		if(StringUtils.isBlank(attrName)){
			if(StringUtils.isNotBlank(nodeNum)){
				value = valueEles.size() > 0?valueEles.get(0).childNode(Integer.parseInt(nodeNum)).toString() : null;
			}else{
				value = valueEles.text();
			}
		}else{
			if(StringUtils.isNotBlank(nodeNum)){
				value = valueEles.size() > 0?valueEles.get(0).childNode(Integer.parseInt(nodeNum)).attr(attrName) : null;
			}else{
				value = valueEles.attr(attrName);
			}
		}
		
		return value;
	}
	
	/**
	 * html元素的定位选取
	 * @param doc
	 * @param selector
	 * @param valueEles
	 */
	private void addElements(Elements doc, org.dom4j.Element selector, Elements valueEles){
		String selectorExpress = selector.elementTextTrim("expression");
		String[] cronExpressArr = selectorExpress.split("\\|");
		for(String cssQuery : cronExpressArr){
			valueEles.addAll(doc.select(cssQuery));
			if(valueEles.size() > 0){
				break;
			}
		}
	}
	
	/**
	 * find，findall，replace，filter。任选其一。
	 * 清洗提取到的元素信息。
	 * @param col
	 * @param colValue
	 */
	private String cleanValue(org.dom4j.Element col, String colValue, String refererValue) {
		/*String find = col.attributeValue("find");
		String findAll = col.attributeValue("findall");
		String replace = col.attributeValue("replace");
		String filter = col.attributeValue("filter");
		if(StringUtils.isNotBlank(find)){
			colValue = MatchUtil.find(colValue, find, "");
		}
		
		if (StringUtils.isNotBlank(findAll)) {
			colValue = MatchUtil.findAll(colValue, findAll, "", " ");
		}

		if (StringUtils.isNotBlank(replace)) {
			String value = col.attributeValue("value");
			log.debug("replace--:" + replace + ",colValue--:" + colValue + ",value--:" + value);
			colValue = colValue.replaceAll(replace, value == null?refererValue : value);
		}

		if (StringUtils.isNotBlank(filter)) {
			if(MatchUtil.has(colValue, filter)){
				colValue = null;
			}
		}*/
		try{
			List<Attribute> attrs = col.attributes();
			boolean exitNow = false;
			for(Attribute attr : attrs){
				switch(attr.getName()){
					case "autoincrement":
						if(StringUtils.isNumeric(colValue)){
							colValue = String.valueOf(Integer.parseInt(colValue) + 1);
						}
						continue;
					case "find":
						colValue = MatchUtil.find(colValue, attr.getValue(), "");
						continue;
					case "prefix":
						colValue = attr.getValue() + colValue;
						continue;
					case "suffix":
						colValue = colValue + attr.getValue();
						continue;
					case "findall":
						String contactStr = col.attributeValue("contactstr");
						colValue = MatchUtil.findAll(colValue, attr.getValue(), "", contactStr);
						continue;
					case "replace":
						String replace = attr.getValue();
						String value = col.attributeValue("value");
						log.debug("replace--:" + replace + ",colValue--:" + colValue + ",value--:" + value);
						colValue = colValue.replaceAll(replace, value == null?refererValue : value);
						continue;
					case "filter":
						String retain = col.attributeValue("retain");
						boolean isRetain = StringUtils.isNotBlank(retain) && "1".equals(retain);
						if(MatchUtil.has(colValue, attr.getValue())){
							if(!isRetain){
								colValue = null;
								exitNow = true;
							}
						} else {
							if(isRetain){
								colValue = null;
								exitNow = true;
							}
						}
						continue;
					case "urlencode":
						String urlEncode = col.attributeValue("urlencode");
						if(StringUtils.isNotBlank(urlEncode) && "1".equals(urlEncode)){
							colValue = UrlEncoded.encodeString(colValue, StandardCharsets.UTF_8);
						}
					default:
				}
				if(exitNow){
					break;
				}
			}
		} catch(Exception e){
		}
		return colValue;
	}
	/**
	 * 产生目标http请求。
	 * @param urlContainer
	 * @param isEnd
	 * @param pattern
	 * @param page
	 * @param repeatKeyEle
	 * @return
	 */
	public List<Request> genTargetRequests(List<Map<String, String>> urlContainer, boolean isEnd,
			String pattern, Page page, org.dom4j.Element template) {
		List<Request> targetRequests = Lists.newArrayList();
		outer : for (Map<String, String> map : urlContainer) {
			String url = map.get(Enums.JsonColums.Url.toString());
            if (StringUtils.isBlank(url) || StringUtils.equals(url, "#") || url.startsWith("javascript:")) {
                continue;
            }
            
            //评论页时，不再翻页，过滤掉获取到下一页url地址。
            if(isEnd && MatchUtil.contain(url, pattern)){
            	continue;
            }
            
            JSONObject newInfoObj = JSONObject.fromObject(page.getRequest().getExtra(Request.DATAS));
			JSONObject initData = JSONObject.fromObject(
					newInfoObj.getString(Enums.JsonColums.InitData.toString()));
            for(Entry<String, String> en : map.entrySet()){
            	if(StringUtils.equals("url", en.getKey())){
            		//Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H, Constants.IS_REPEAT_KEY_NAME, en.getValue(), "1");
            		continue;
            	}
            	//重复
            	if(StringUtils.equals("repeatstore", en.getKey())){
            		String storeName = en.getValue();
            		org.dom4j.Element repeatKeyEle = template.element("repeatkey");
            		String key = cleanValue(repeatKeyEle, url, "");
            		if(StringUtils.isBlank(key)){
            			key = url;
            		}
            		//synchronized (storeName) {
            			if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, storeName, key)){
            				String crawlRepeat = map.get("crawlrepeat");
                			if(!StringUtils.equals("1", crawlRepeat)){
                				continue outer;
                			}
                		}else{
                			String pageInitNum = template.elementTextTrim("pageinitnum");
                			try {
                				Constants.dataRedisService.addInfo2Map(Constants.REDIS_INDEX_4_H, storeName, key, pageInitNum == null ? "0" : pageInitNum);
							} catch (Exception e) {
								log.error("{}", e.getMessage());
							}
                        }
					//}
            		continue;
            	}
            	if(HttpConstant.Header.REFERER.equals(en.getKey())){
            		newInfoObj.put(HttpConstant.Header.REFERER, en.getValue());//referer，为统一固定的字段
            		continue;
            	}
            	initData.put(en.getKey(), en.getValue());
            }
            newInfoObj.put(Enums.JsonColums.InitData.toString(), initData.toString());
            
            //s = UrlUtils.canonicalizeUrl(s, url.toString());
            targetRequests.add(new Request(url).putExtra(Request.DATAS, newInfoObj.toString()));
        }
		return targetRequests;
	}
}

