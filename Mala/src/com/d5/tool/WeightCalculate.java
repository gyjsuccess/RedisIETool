/******************************************************************
 *    ProjectName: Mala
 *    FileName: WeightCalculate.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月20日 上午10:18:39
 *    Revision:
 *    2017年1月20日 上午10:18:39
 *        - first revision
 *****************************************************************/
package com.d5.tool;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.data.transfer.DataDeal;

/**
 * PackageName: com.d5.tool
 * @ClassName WeightCalculate
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月20日 上午10:18:39
 */
public class WeightCalculate {
	private static Logger log = LoggerFactory.getLogger(WeightCalculate.class);
	public static Map<String, Double> calculateWeight(long heatValue, String celebrities, String movieType){
		return calPosTheta(celebrities, movieType, heatValue);
	}
	/**
	 * MethodName：calPosTheta
	 * @author: Administrator
	 * @Date: 2017年1月20日 上午10:26:04
	 * @Description double要乘以100转换为long
	 * @param celebrities
	 * @return
	 */
	private static Map<String, Double> calPosTheta(String celebrities, String movieType, long heatValue) {
		Map<String, Double> cTMap = new HashMap<>();
		String[] celebritiesArr = celebrities.split(" ?/ ?");
		int arrLen = celebritiesArr.length;
		if(movieType.matches("tv|movie")){
			if(arrLen == 1){
				fillMap(arrLen, celebritiesArr, 1.0 * heatValue, cTMap);
			}
			if(arrLen == 2){
				fillMap(arrLen, celebritiesArr, 0.5 * heatValue, cTMap);
			}
			if(arrLen >= 3){
				fillMap(arrLen, celebritiesArr, cTMap, heatValue);
			}
		} else {
			fillMap(arrLen, celebritiesArr, 1.0 * heatValue, cTMap);
		}		
		return cTMap;
	}
	/**
	 * MethodName：fillMap
	 * @author: Administrator
	 * @Date: 2017年1月20日 上午10:41:33
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param arrLen
	 * @param celebritiesArr
	 * @param cTMap
	 */
	private static void fillMap(int arrLen, String[] celebritiesArr, Map<String, Double> cTMap, long heatValue) {
		for(int i=0; i<arrLen; i++){
			double theta = calTheta(i, arrLen);
			cTMap.put(celebritiesArr[i], theta * heatValue);
		}
		
	}
	/**
	 * MethodName：calTheta
	 * @author: Administrator
	 * @Date: 2017年1月20日 上午10:42:36
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param i
	 * @param arrLen
	 * @return
	 */
	public static double calTheta(int i, int arrLen) {
		if(i >= arrLen){
			log.error("calTheta---参数错误！");
		} else if(arrLen == 3){
			if((i + 1) < 3){
				return 0.35;
			} else {
				return 0.3;
			}
		} else if(arrLen > 3){
			if((i + 1) < 3){
				return 0.3;
			} else {
				return DataDeal.rondDouble(0.4 * ((arrLen - i) * 1.0 / calSigama(arrLen - 2)), 2);
			}
		}
		return 0;
	}
	/**
	 * MethodName：calSigama
	 * @author: Administrator
	 * @Date: 2017年1月20日 上午11:12:50
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param i
	 * @return
	 */
	public static int calSigama(int n) {
		int sum = 0;
		for(int i=1; i<=n; i++){
			sum += i;
		}
		return sum;
	}
	/**
	 * MethodName：fillMap
	 * @author: Administrator
	 * @Date: 2017年1月20日 上午10:37:39
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param arrLen
	 * @param celebritiesArr
	 * @param d
	 */
	private static void fillMap(int arrLen, String[] celebritiesArr, double theta,
			Map<String, Double> cTMap) {
		for(int i=0; i<arrLen; i++){
			cTMap.put(celebritiesArr[i], theta);
		}
	}
}
