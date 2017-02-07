/******************************************************************
 *    ProjectName: DataCrawl
 *    FileName: HeatCalculate.java
 *    Description: TODO(用一句话描述该文件做什么)
 *    Company: Digital Telemedia Co.,Ltd
 *    @author: Administrator
 *    @version: 1.0.0
 *    Create at: 2017年1月18日 下午5:46:44
 *    Revision:
 *    2017年1月18日 下午5:46:44
 *        - first revision
 *****************************************************************/
package com.d5.tool;

import java.math.BigDecimal;

import com.d5.data.transfer.DataDeal;

/**
 * PackageName: com.d5.tool
 * @ClassName HeatCalculate
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author Administrator
 * @Date 2017年1月18日 下午5:46:44
 */
public class HeatCalculate {
	/**
	 * 
	 * MethodName：calHeat
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午5:50:13
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param numberOld
	 * @param numberNew
	 * @param days
	 * @return
	 */
	public static double calHeat(long numberOld, long numberNew, int days, int dSapn){
		double weight = calWeight(days, dSapn);
		return DataDeal.rondDouble(weight * (Math.abs(numberNew - numberOld)) + 1 / ((days + 1) * 10000000), 2);
	}

	/**
	 * MethodName：calWeight
	 * @author: Administrator
	 * @Date: 2017年1月18日 下午5:54:26
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 * @param days
	 * @param dSapn
	 * @return
	 */
	private static double calWeight(int days, int dSapn) {
		int daysSpan = days / dSapn;
		if(daysSpan > 1){
			return 0.5;
		}
		return 0.85;
	}

	/**
	 * 
	 * MethodName：calHeatNew
	 * @author: Administrator
	 * @Date: 2017年1月19日 上午10:02:49
	 * @Description 新电影热度计算
	 * @param ratingPeopleNum
	 * @param wishesNum
	 * @param days
	 * @param dSapn
	 */
	public static double calHeatNew(long ratingPeopleNum, long wishesNum, int days, int dSapn) {
		double weight = calWeight(days, dSapn);
		return DataDeal.rondDouble(weight * ratingPeopleNum + wishesNum + 1 / ((days + 1) * 10000000), 2);
	}
}
