package com.d5.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import net.sf.json.JSONObject;

public class KafkaUtil {
	private static Logger log = Logger.getLogger(KafkaUtil.class);
	private static Producer<String, String> producer = getProducer();
	/*private static final TelnetUtil telnetClient = new TelnetUtil("10.9.201.198", 9092);*/

	private static Producer<String, String> getProducer() {
		Properties properties = new Properties();
		try {
			properties.load(ClassLoader
					.getSystemResourceAsStream("producer.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ProducerConfig config = new ProducerConfig(properties);
		producer = new Producer<String, String>(config);
		return producer;
	}

	private static void send(String topicName, String message) {
		if (topicName == null || message == null) {
			return;
		}
		KeyedMessage<String, String> km = new KeyedMessage<String, String>(
				topicName, message);
		producer.send(km);
	}

	public void send(String topicName, Collection<String> messages) {
		if (topicName == null || messages == null) {
			return;
		}
		if (messages.isEmpty()) {
			return;
		}
		List<KeyedMessage<String, String>> kms = new ArrayList<KeyedMessage<String, String>>();
		for (String entry : messages) {
			KeyedMessage<String, String> km = new KeyedMessage<String, String>(
					topicName, entry);
			kms.add(km);
		}
		producer.send(kms);
	}

	private static void close() {
		producer.close();
	}

	public static void sendDataToKafka(String topicName,
			JSONObject object) {
		log.debug("JSON 数据为：" + object);
		/*synchronized (object) {
			if(telnetClient.isReachable(0)){
				writeErrorData(topicName);
				if(object.size() > 0){
					send(topicName, object.toString());
				}
			}else{
				Constants.dataRedisService.addEntityString(object.toString(), Utils.KAFKA_ERROR_LIST);
			}
		}*/
		send(topicName, object.toString());
	}
	
	public static void sendDataToKafka(String topicName,
			String object) {
		log.debug("JSON 数据为：" + object);
		send(topicName, object);
	}
	
	/*private static void writeErrorData(String topicName){
		String data = null;
		while((data = Constants.dataRedisService.getEntityString(Utils.KAFKA_ERROR_LIST)) != null){
			send(topicName, data);
		}
	}*/

	public static void main(String[] args) {
		test();
	}
	
	public static void test() {
		String data = "{\"id\":\"b8ca3aa97986691083282669098467\",\"sourceId\":\"5\",\"isNew\":0,\"categoryId\":\"030005002006001\",\"comments\":[],\"isAllComment\":1}";
		sendDataToKafka("test", JSONObject.fromObject(data));
		/*String filePath = "C:\\Users\\01\\Desktop\\0922.txt";
		Pattern regex = Pattern.compile("\\{.*\\}");
		try {
			List<String> datas = FileUtils.readLines(new File(filePath));
			for(String data : datas){
				String ResultString = null;
				try {
					Matcher regexMatcher = regex.matcher(data);
					if (regexMatcher.find()) {
						ResultString = regexMatcher.group();
					} 
				} catch (PatternSyntaxException ex) {
				}
				if(StringUtils.isNotBlank(ResultString)){
					sendDataToKafka("test", JSONObject.fromObject(ResultString));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
//		testForum();
//		testWeibo();
//		testAuthority();
//		testElecBusiness();
		close();
	}
	
}
