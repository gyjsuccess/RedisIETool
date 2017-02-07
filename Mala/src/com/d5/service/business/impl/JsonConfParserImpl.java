package com.d5.service.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.d5.factory.DataDealImplClassFactory;
import com.d5.factory.GetTemplateScriptFactory;
import com.d5.service.business.IExecuteService;
import com.d5.util.CommonUtil;
import com.d5.util.MatchUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 字段在columns中
 * @author 01
 *
 */
public class JsonConfParserImpl implements IExecuteService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 解析传入的字符串，返回一个字符串。
     *
     * @param info
     * @return
     */
    private void parser(String info){
        log.debug("HtmlParserImpl 执行中");
        JSONObject dataObj = JSONObject.fromObject(info);
        String htmlSources = dataObj.getString(Enums.JsonColums.HtmlSources.toString());
        //清除网页源码，节省内存空间
        dataObj.remove(Enums.JsonColums.HtmlSources.toString());
        String script = GetTemplateScriptFactory.getTemplateScript(info);
        if(StringUtils.isBlank(script)){
            log.debug("获取解析脚本失败。 info content is:" + info.substring(0, 500));
            return;
        }
        JSONObject templateObj = JSONObject.fromObject(script);
        JSONArray templates = templateObj.getJSONArray("templates");
        for(int ix=0; ix<templates.size(); ix++){
            JSONObject template = templates.getJSONObject(ix);

            String forward = template.getString("forward").toLowerCase();
            String forwardName = template.getString(forward + "Name");

            String sourceDataType = template.getString("dataType").trim().toLowerCase();

            List<org.bson.Document> datas = new ArrayList<org.bson.Document>();
            org.bson.Document dataResult = new org.bson.Document();
            if(template.containsKey("cycleLabel")){
                //HTML标签，必须是cssselector。
                String cycleName = template.getString("cycleLabel");
                if("html".equals(sourceDataType)){
                    String dataString = htmlSources;
                    Document htmlDoc = Jsoup.parse(dataString);
                    JSONArray columnArray = template.getJSONArray("columns");
                    Elements cEle = new Elements();
                    cEle.addAll(htmlDoc.select(cycleName));
                    for(Element ele : cEle){
                        org.bson.Document data = new org.bson.Document();
                        JSONObject column = null;
                        for(int j=0; j<columnArray.size(); j++){
                            column = columnArray.getJSONObject(j);
                            Object value = getDataValue(data, ele, column);
                            if(value != null){
                                data.put(column.getString("name"), value);
                            }
                        }
                        datas.add(data);
                    }
                }else{
                    String dataString = htmlSources;
                    if("jsonp".equals(sourceDataType)){
                        String dataTypeReplace = template.getString("dataTypeReplace");
                        dataString = htmlSources.replaceAll(dataTypeReplace, "");
                    }
                    JSONObject dataJsonObj = JSONObject.fromObject(dataString);
                    JSONArray dataJsonArray = getCycleObj(dataJsonObj, cycleName);
                    if(dataJsonArray == null){
                        log.error("cycleLabel is error:" + cycleName);
                        return;
                    }
                    JSONArray columnArray = template.getJSONArray("columns");
                    for(int i=0; i<dataJsonArray.size(); i++){
                        org.bson.Document data = new org.bson.Document();
                        JSONObject data_ = dataJsonArray.getJSONObject(i);
                        JSONObject column = null;
                        for(int j=0; j<columnArray.size(); j++){
                            column = columnArray.getJSONObject(j);

                            Object value = getDataValue(data, data_, column);
                            if(value != null){
                                data.put(column.getString("name"), value);
                            }
                        }

                        if(template.containsKey("cycleColumn")){
                            List<org.bson.Document> valueList = new ArrayList<org.bson.Document>();
                            datas.addAll(getDataValues(data, template, data_));
                        }else{
                            datas.add(data);
                        }
                    }
                }
            }else{
                if("html".equals(sourceDataType)){
                    String dataString = htmlSources;
                    Document htmlDoc = Jsoup.parse(dataString);
                    JSONArray columnArray = template.getJSONArray("columns");
                    JSONObject column = null;
                    for(int j=0; j<columnArray.size(); j++){
                        column = columnArray.getJSONObject(j);
                        Object value = getDataValue(dataResult, htmlDoc.select("html").first(), column);
                        if(value != null){
                            dataResult.put(column.getString("name"), value);
                        }
                    }
                }else{
                    String dataString = htmlSources;
                    if("jsonp".equals(sourceDataType)){
                        String dataTypeReplace = template.getString("dataTypeReplace");
                        dataString = htmlSources.replaceAll(dataTypeReplace, "");
                    }
                    JSONObject dataJsonObj = JSONObject.fromObject(dataString);
                    JSONArray columnArray = template.getJSONArray("columns");
                    JSONObject column = null;
                    for(int j=0; j<columnArray.size(); j++){
                        column = columnArray.getJSONObject(j);
                        Object value = getDataValue(dataResult, dataJsonObj, column);
                        if(value != null){
                            dataResult.put(column.getString("name"), value);
                        }
                    }
                }
                datas.add(dataResult);
            }

            //数据序列化处理
            switch(forward){
                case "collect":
                    //数据写入Mongodb集合,datas,forwardName
                    dataObj.put(Enums.JsonColums.DataDealImplClassName.toString(), Constants.DATA_DEAL_4_MONGODB_IMPL_CLASS_NAME);
                    for(org.bson.Document doc : datas){
                        //重复
                        /*if(doc.containsKey("url")){
                            if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, Constants.IS_REPEAT_KEY_NAME, doc.getString("url"))){
                                continue;
                            }
                        }*/

                        //TODO getDate的开关放入配置文件
                        //doc.put("getDate", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                        DataDealImplClassFactory.getImplClass(dataObj.toString()).deal(doc, forwardName);
                    }
                    break;
                case "queue":
                    //数据写入Redis队列，datas,forwardName
                    dataObj.put(Enums.JsonColums.DataDealImplClassName.toString(), Constants.DATA_DEAL_4_REDIS_IMPL_CLASS_NAME);
                    for(org.bson.Document doc : datas){
                        String url = null;
                        try{
                            url = doc.getString("url");
                        }catch (Exception e){
                        }
                        if(StringUtils.isBlank(url)){
                            continue;
                        }
                        //重复
                        /*if(Constants.dataRedisService.existInHash(Constants.REDIS_INDEX_4_H, Constants.IS_REPEAT_KEY_NAME, url)){
                            continue;
                        }*/

                        JSONObject newInfoObj = new JSONObject();
                        newInfoObj.put(Enums.JsonColums.ExecuteImplClassName.toString(), Constants.DOWNLOADER_IMPL_CLASS_NAME);
                        newInfoObj.put(Enums.JsonColums.Url.toString(), url);//url，为统一固定的字段

                        DataDealImplClassFactory.getImplClass(dataObj.toString()).deal(newInfoObj.toString(), forwardName);
                    }
                    break;
                default:
            }
            dataObj.remove(Enums.JsonColums.DataDealImplClassName.toString());
        }

        return ;
    }

    private Collection<? extends org.bson.Document> getDataValues(org.bson.Document data, JSONObject template, JSONObject data_) {
        List<org.bson.Document> list = new ArrayList<org.bson.Document>();
        String cycleLabel = template.getJSONObject("cycleColumn").getString("cycleLabel");
        JSONArray dataJsonArray = getCycleObj(data_, cycleLabel);
        if(dataJsonArray == null){
            log.error("cycleLabel is error:" + cycleLabel);
            return null;
        }

        JSONArray columnArray = template.getJSONObject("cycleColumn").getJSONArray("columns");
        for(int i=0; i<dataJsonArray.size(); i++){
            JSONObject data__ = dataJsonArray.getJSONObject(i);
            JSONObject column = null;
            for(int j=0; j<columnArray.size(); j++){
                column = columnArray.getJSONObject(j);

                Object value = getDataValue(data, data__, column);
                if(value != null){
                    data.put(column.getString("name"), value);
                }
            }
            list.add(SerializationUtils.clone(data));
        }
        return list;
    }

    /**
     * json格式数据，获取循环标签数据
     * @param dataJsonObj
     * @param cycleName
     * @return
     */
    private JSONArray getCycleObj(JSONObject dataJsonObj, String cycleName) {
        String[] keys = cycleName.split("\\.");
        int count = keys.length;
        if(count == 1){
            return dataJsonObj.getJSONArray(cycleName);
        }

        if(count > 1){
            JSONObject jObj = dataJsonObj;
            for(int index=0; index<count-1; index++){
                jObj = jObj.getJSONObject(keys[index]);
            }
            return jObj.getJSONArray(keys[count-1]);
        }
        return null;
    }

    /**
     * json格式数据，解析每个column，获取字段的数据。
     * @param data
     * @param data_
     * @param column
     * @return
     */
    private Object getDataValue(org.bson.Document data, JSONObject data_, JSONObject column){
        String type = column.getString("type").toLowerCase();
        if("constant".equals(type)){
            return column.getString("value");
        }

        JSONObject labelObj = column.getJSONObject("label");
        String sData = null;
        try{
            sData = getValue(data_, labelObj.getString("expression"));
        } catch(Exception e){//label获取不到数据时，用副label获取数据。
            if(column.containsKey("label_")){
                labelObj = column.getJSONObject("label_");
                try{
                    sData = getValue(data_, labelObj.getString("expression"));
                } catch(Exception e1){//两个标签都获取不到数据
                }
            }
        }
        if(sData == null){
            return sData;
        }

        //去掉干扰信息
        if(column.containsKey("replace")){
            sData = regexDeal(sData, "replace", column.getString("replace"));
        }

        if(column.containsKey("find")){
            sData = regexDeal(sData, "find", column.getString("find"));
        }

        if(column.containsKey("findAll")){
            sData = regexDeal(sData, "findAll", column.getString("findAll"));
        }

        //格式化，替换{xxx}内容.{xxx}的位置必须与REF的顺序一致，否则会错位。
        if(labelObj.containsKey("formatter")){
            String template = labelObj.getJSONObject("formatter").getString("template");
            List<String> replaces = MatchUtil.findAll(template, "\\{([A-Za-z]+[0-9]*)+\\}");
            JSONArray refArr = labelObj.getJSONObject("formatter").getJSONArray("ref");
            String targetStr = template;
            int index = 0;
            for(String replace : replaces){
                if("{self}".equals(replace)){
                    targetStr = targetStr.replace(replace, sData);
                    continue;
                }else{
                    JSONObject ref = refArr.getJSONObject(index);
                    targetStr = targetStr.replace(replace, data.getString(ref.getString("name")));
                    if(ref.containsKey("replace")){
                        targetStr = regexDeal(targetStr, "replace", ref.getString("replace"));
                    }

                    if(ref.containsKey("find")){
                        targetStr = regexDeal(targetStr, "find", ref.getString("find"));
                    }

                    if(ref.containsKey("findAll")){
                        targetStr = regexDeal(targetStr, "findAll", ref.getString("findAll"));
                    }
                }
                index ++;
            }
            sData = targetStr;
        }

        //类型处理
        switch(type){
            case "integer":
                return Integer.parseInt(sData);
            case "date":
                return CommonUtil.convertDate(sData);
            default:
                return sData;
        }
    }

    private String getValue(JSONObject data, String expression) {
        String[] keys = expression.split("\\.");
        int count = keys.length;
        if(count == 1){
            return data.getString(expression);
        }

        if(count > 1){
            JSONObject jObj = data;
            for(int index=0; index<count-1; index++){
                jObj = jObj.getJSONObject(keys[index]);
            }
            return jObj.getString(keys[count-1]);
        }
        return null;
    }

    private String getArrayValue(JSONObject data, String expression) {
        String[] keys = expression.split("\\.");
        int count = keys.length;
        if(count == 2){
            return data.getString(expression);
        }

        if(count > 2){
            JSONObject jObj = data;
            for(int index=0; index<count-1; index++){
                jObj = jObj.getJSONObject(keys[index]);
            }
            return jObj.getString(keys[count-1]);
        }
        return null;
    }

    /**
     * json格式数据，解析每个column，获取字段的数据。
     * @param data
     * @param ele
     * @param column
     * @return
     */
    private Object getDataValue(org.bson.Document data, Element ele, JSONObject column){
        String type = column.getString("type").toLowerCase();
        if("constant".equals(type)){
            return column.getString("value");
        }
        if("htmlsource".equals(type)){
            return ele.outerHtml();
        }

        JSONObject labelObj = column.getJSONObject("label");
        String sData = "";
        String attrName = null;
        String expression = null;
        if(labelObj.containsKey("expression")) {
            expression = labelObj.getString("expression");
            if (labelObj.containsKey("attrName")) {
                attrName = labelObj.getString("attrName");
                if ("_self".equals(expression)) {
                    sData = ele.attr(attrName);
                } else {
                    sData = ele.select(expression).attr(attrName);
                }
            } else {
                if ("_self".equals(expression)) {
                    sData = ele.text();
                } else {
                    sData = ele.select(expression).text();
                }
            }
        }

        if(StringUtils.isBlank(sData) && column.containsKey("label_")){
            labelObj = column.getJSONObject("label_");
            if(labelObj.containsKey("expression")){
                expression = labelObj.getString("expression");
                if(labelObj.containsKey("attrName")){
                    sData = ele.select(expression).attr(attrName);
                } else {
                    sData = ele.select(expression).text();
                }
            }
        }

        if(column.containsKey("filter")){
            String filter = column.getString("filter");
            if(MatchUtil.contain(sData, filter)){
                sData = null;
            }
        }

        if(sData == null){
            return sData;
        }

        //去掉干扰信息
        if(column.containsKey("replace")){
            sData = regexDeal(sData, "replace", column.getString("replace"));
        }

        if(column.containsKey("find")){
            sData = regexDeal(sData, "find", column.getString("find"));
        }

        if(column.containsKey("findAll")){
            sData = regexDeal(sData, "findAll", column.getString("findAll"));
        }

        //格式化，替换{xxx}内容.{xxx}的位置必须与REF的顺序一致，否则会错位。
        if(labelObj.containsKey("formatter")){
            String template = labelObj.getJSONObject("formatter").getString("template");
            List<String> replaces = MatchUtil.findAll(template, "\\{([A-Za-z]+[0-9]*)+\\}");
            JSONArray refArr = labelObj.getJSONObject("formatter").getJSONArray("ref");
            String targetStr = template;
            int index = 0;
            for(String replace : replaces){
                if("{self}".equals(replace)){
                    targetStr = targetStr.replace(replace, sData);
                    continue;
                }else{
                    JSONObject ref = refArr.getJSONObject(index);
                    targetStr = targetStr.replace(replace, data.getString(ref.getString("name")));
                    if(ref.containsKey("replace")){
                        targetStr = regexDeal(targetStr, "replace", ref.getString("replace"));
                    }

                    if(ref.containsKey("find")){
                        targetStr = regexDeal(targetStr, "find", ref.getString("find"));
                    }

                    if(ref.containsKey("findAll")){
                        targetStr = regexDeal(targetStr, "findAll", ref.getString("findAll"));
                    }
                }
                index ++;
            }

            sData = targetStr;
        }


        //类型处理
        switch(type){
            case "integer":
                return Integer.parseInt(sData);
            case "date":
                return CommonUtil.convertDate(sData);
            default:
                return sData;
        }
    }

    private String regexDeal(String source, String dealType, String pattern){
        if("replace".equals(dealType)){
            return source.replaceAll(pattern, "");
        }

        if("find".equals(dealType)){
            return MatchUtil.find(source, pattern, "");
        }

        if("findAll".equals(dealType)){
            return MatchUtil.findAll(source, pattern, "", " ");
        }
        return "";
    }

    /**
     * @param info
     */
    @Override
    public void execute(String info) {
        parser(info);
    }
}
