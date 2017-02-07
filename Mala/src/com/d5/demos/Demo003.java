package com.d5.demos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

import com.d5.util.Dom4jUtil;
import com.d5.util.MatchUtil;

public class Demo003 {
	public static void main(String[] args) {
		
		Element col = Dom4jUtil.getRootElement("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><column name=\"\" autoincrement=\"\" type=\"ID\" find=\"\" findall=\"\" filter=\"\" value=\"\" headerreferer=\"\"></column></root>").element("column");
		String colValue = "";
		
		@SuppressWarnings("unchecked")
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
				case "findall":
					colValue = MatchUtil.findAll(colValue, attr.getValue(), "", " ");
					continue;
				case "replace":
					String replace = attr.getValue();
					String value = col.attributeValue("value");
					colValue = colValue.replaceAll(replace, value == null?"" : value);
					continue;
				case "filter":
					if(MatchUtil.has(colValue, attr.getValue())){
						colValue = null;
					}
					exitNow = true;
				default:
			}
			if(exitNow){
				break;
			}
		}
	}
}
