package com.scrat.framework.log.logback;

import com.alibaba.fastjson.JSON;

/**
 * 描述：系统日志返回数据日志实体
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:08:55
 */
public class ReturnLogEntity extends DataLogEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 745354180459889134L;
	//返回值记录日志
	private String returnVal;

	public ReturnLogEntity(DataLogEntity dle) {
		super(dle.getClassName(), dle.getMethodName(), dle.getParams());
	}

	public ReturnLogEntity() {

	}

	public String getReturnVal() {
		return returnVal;
	}

	public void setReturnVal(Object returnVal) {
		this.returnVal = JSON.toJSONString(returnVal);
	}

}
