package com.d5.service.business.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.common.Enums;
import com.d5.factory.GetTemplateScriptFactory;
import com.d5.service.business.IExecuteService;
import com.d5.thread.main.DataSerialThread;
import com.d5.thread.main.UrlSerialThread;
import com.d5.tool.Calculator;
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
public class XmlSelectorConfParserImpl implements IExecuteService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 解析传入的字符串，返回一个字符串。
     *
     * @param info
     * @return
     */
    private void parser(String info){
    	//准备
        log.debug("HtmlParserImpl 执行中");
        JSONObject dataObj = JSONObject.fromObject(info);
        String htmlSources = dataObj.getString(Enums.JsonColums.HtmlSources.toString());
        //log.debug("htmlSources is :" + htmlSources);
        int sourceLength = htmlSources.length();
        //清除网页源码，节省内存空间
        dataObj.remove(Enums.JsonColums.HtmlSources.toString());
        String script = GetTemplateScriptFactory.getTemplateScript(info);
        if(StringUtils.isBlank(script)){
            log.debug("获取解析脚本失败。 info content is:" + info.substring(0, 500));
            return;
        }
        /*if(script.length() > 1000){
        	log.debug("debug");
        }*/
        
        //解析
        org.dom4j.Element rootElement = Dom4jUtil.getRootElement(script);
        List<org.dom4j.Element> templates = rootElement.elements("template");
        for(org.dom4j.Element template : templates){
	        //org.dom4j.Element template = rootElement.element("template");
	        String collectName = template.elementTextTrim("collectname");
	        String queueName = template.elementTextTrim("queuename");
	        String sourceDataType = template.elementTextTrim("datatype").toLowerCase();
	        List<org.bson.Document> dataContainer = new ArrayList<org.bson.Document>(10);
	        List<Map<String, String>> urlContainer = new ArrayList<Map<String, String>>(10);
	        org.bson.Document data = InitData(dataObj, template);
	        
	        Map<String, String> tempMap = new HashMap<String, String>();
	        genTempData(dataObj, template, tempMap);
	        if("html".equals(sourceDataType)){
	        	Document doc = Jsoup.parse(htmlSources);
	        	parseGetData(doc.select("html"), dataContainer, urlContainer, template, data, tempMap);
	        }else{
	        	String dataString = (String) SerializationUtils.clone(htmlSources);
	            if("jsonp".equals(sourceDataType)){
	                String dataTypeReplace = template.elementTextTrim("datatypereplace").trim();
	                dataString = dataString.replaceAll(dataTypeReplace, "");
	            }
	            
	            JSONObject dataJson = JSONObject.fromObject(dataString);
	            dataString = null;
	            
	            parseGetData(dataJson, dataContainer, urlContainer, template, data, tempMap);
	        }
	        
	        getNextPage(urlContainer, template, dataObj, sourceLength, tempMap);
	        
	        //解析结果序列化
	        if(StringUtils.isNotBlank(collectName)){
	        	//线程
				new Thread(new DataSerialThread(
						(JSONObject) SerializationUtils.clone(dataObj),
						collectName, dataContainer)).start();
	        }
	        
	        if(StringUtils.isNotBlank(queueName)){
	        	//线程
				new Thread(new UrlSerialThread(
						(JSONObject) SerializationUtils.clone(dataObj), queueName,
						urlContainer)).start();
	        }
        }
        return ;
    }

    private void genTempData(JSONObject dataObj, org.dom4j.Element template,
			Map<String, String> tempMap) {
    	String tempColsStr = template.elementTextTrim("tempdata");
    	if(StringUtils.isBlank(tempColsStr)){
    		return;
    	}
    	String[] tempCols = tempColsStr.split("\\|");
    	for(String key : tempCols){
    		tempMap.put(key, dataObj.getString(key));
    	}
	}

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
        	String endPageStr = pageEle.elementTextTrim("endpage");
        	if(StringUtils.isNotBlank(endPageStr)){
        		endPageNum = Integer.parseInt(endPageStr);
        	}
        	if(nextPageNum > endPageNum){
        		log.debug("翻页结束。");
        		return;
        	}
    	}catch(Exception e){
    	}
    	Map<String, String> urlMap = new HashMap<String, String>();
    	String key = pageEle.elementTextTrim("key");
    	urlMap.put(Enums.JsonColums.Url.toString(), 
    			curUrl.replaceAll(StringUtils.join(key, "\\d+"), StringUtils.join(key, nextPageStartNum)));
    	urlContainer.add(urlMap);
	}

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

	private void parseGetData(JSONObject dataJson,
			List<org.bson.Document> dataContainer, List<Map<String, String>> urlContainer,
			org.dom4j.Element template, org.bson.Document basicData, Map<String, String> tempMap) {
    	org.bson.Document data = SerializationUtils.clone(basicData);
    	List<org.dom4j.Element> selectors = template.elements("selector");
		for(org.dom4j.Element selector : selectors){
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
							value = StringUtils.join(Constants.LOCAL_MAC, CommonUtil.generateShortUuid());
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
						case "date":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							value = CommonUtil.convertDate(value);
							break;
						case "integer":
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							break;
						default:
							value = getValue(col, obj);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
					}
					if(value != null){
						data.append(key, value);
					}
				}
				
				List<org.bson.Document> dataContainer_ = new ArrayList<org.bson.Document>();
				parseGetData(obj, dataContainer_, urlContainer, selector, data, tempMap);
				for(org.bson.Document d : dataContainer_){
					data.putAll(d);
				}
				if(data.size() > 0){
					dataContainer.add(SerializationUtils.clone(data));
				}
			}
		}
	}

	private String getValue(org.dom4j.Element selector, JSONObject obj) {
		String selectorExpress = selector.elementTextTrim("attrname");
		if(StringUtils.isBlank(selectorExpress)){
			return "";
		}
		return obj.getString(selectorExpress);
	}

	private void parseGetData(Elements doc,
			List<org.bson.Document> dataContainer, List<Map<String, String>> urlContainer,
			org.dom4j.Element template, org.bson.Document basicData, Map<String, String> tempMap) {
    	org.bson.Document data = SerializationUtils.clone(basicData);
    	List<org.dom4j.Element> selectors = template.elements("selector");
		for(org.dom4j.Element selector : selectors){
			String selectorExpress = selector.elementTextTrim("expression");
			
			Elements curElements = new Elements();
			if(StringUtils.isBlank(selectorExpress)){
				curElements.addAll(doc);
			}else{
				//curElements.addAll(doc.select(selectorExpress));
				addElements(doc, selector, curElements);
			}
			log.debug(selectorExpress + "--" + curElements.size());
			//selector取到想要的数据
			for(Element ele : curElements){
				Elements singleEles = new Elements();
				singleEles.add(ele);
				List<org.dom4j.Element> columns = selector.elements("column");
				for(org.dom4j.Element col : columns){
					String key = col.attributeValue("name");
					if("commentCount".equals(key)){
						log.debug("key is commentCount");
					}
					String value = null;
					String type = col.attributeValue("type");
					switch(type){
						case "ID":
							value = StringUtils.join(Constants.LOCAL_MAC, CommonUtil.generateShortUuid());
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
						case "date":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							value = CommonUtil.convertDate(value);
							break;
						case "integer":
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							break;
						default:
							value = getValue(col, singleEles);
							value = cleanValue(col, value, "");
							if(value != null){
								value = formatorValue(col, value, data, tempMap);
							}
					}
					if(value != null){
						data.append(key, value);
					}
				}
				
				List<org.bson.Document> dataContainer_ = new ArrayList<org.bson.Document>();
				parseGetData(singleEles, dataContainer_, urlContainer, selector, data, tempMap);
				for(org.bson.Document d : dataContainer_){
					data.putAll(d);
				}
				if(data.size() > 0){
					dataContainer.add(SerializationUtils.clone(data));
				}
			}
		}
	}
	
	private void addUrl2Container(List<Map<String, String>> urlContainer,
			org.bson.Document data, String value, org.dom4j.Element col,
			Map<String, String> tempMap) {
		Map<String, String> map = new HashMap<String, String>();
		String endPageStr = col.elementTextTrim("endpage");
		if(StringUtils.isNotBlank(endPageStr)){
			String curPageStr = cleanValue(col.element("endpage"), tempMap.get("url"), "");
			int curPageNum = Integer.parseInt(curPageStr);
			int nextPageNum = curPageNum + 1;
			int endPageNum = Integer.parseInt(endPageStr);
			if(nextPageNum > endPageNum){
				log.debug("翻页结束。");
        		return;
			}
		}
		map.put("url", value);
		String headerReferer = col.attributeValue("headerreferer");
		if(StringUtils.isNotBlank(headerReferer) && "true".equals(headerReferer)){
			map.put(HttpConstant.Header.REFERER, tempMap.get("url"));
		}
		
		String repeatStore = col.attributeValue("repeatstore");
		if(StringUtils.isNotBlank(repeatStore)){
			map.put("repeatstore", repeatStore);
		}
		String refererStr = col.attributeValue("referer");
		if(StringUtils.isNotBlank(refererStr)){
			String[] refererArr = refererStr.split("\\|");
			for(String referer : refererArr) {
				if(data.containsKey(referer)){
					map.put(referer, data.getString(referer));
				}
			}
		}
		urlContainer.add(map);
	}
	
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
	                }else{
	                	org.dom4j.Element ref = refArr.get(index);
	                	String refValue = null;
	                	String isTempData = ref.attributeValue("tempdata");
	                	String refColName = ref.getTextTrim();
	                	if(StringUtils.isNotBlank(isTempData) && "true".equals(isTempData)){
	                		refValue = tempMap.get(refColName);
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
		}
		return value;
	}

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
	
	private void addElements(Elements doc, org.dom4j.Element selector, Elements valueEles){
		String selectorExpress = selector.elementTextTrim("expression");
		String[] cronExpressArr = selectorExpress.split("\\|");
		for(String cssQuery : cronExpressArr){
			valueEles.addAll(doc.select(cssQuery));
		}
	}
	
	/**
	 * find，findall，replace，filter。任选其一。
	 * @param col
	 * @param colValue
	 */
	private String cleanValue(org.dom4j.Element col, String colValue, String refererValue) {
		String find = col.attributeValue("find");
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
		}
		return colValue;
	}

	/**
     * @param info
     */
    @Override
    public void execute(String info) {
        parser(info);
    }
}

