package com.d5.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dom4jUtil {
	private static Logger log = LoggerFactory.getLogger(Dom4jUtil.class);

	private static Document getDocument(String xml) {
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			InputStreamReader strInStream = new InputStreamReader(in, "UTF-8");
			document = reader.read(strInStream);
		} catch (Exception e) {
			log.error("XML不合法! :", e);
		}
		return document;
	}
	
	public static Element getRootElement(String xml){
		return getDocument(xml).getRootElement();
	}
}