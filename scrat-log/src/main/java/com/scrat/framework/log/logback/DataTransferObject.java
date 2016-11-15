package com.scrat.framework.log.logback;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述：数据传输对象，包装数据传输内容
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:09:19
 */
public class DataTransferObject implements Serializable {

   private static final long serialVersionUID = -2443929344379556217L;
   
   public static final Logger LOGGER = LoggerFactory.getLogger(DataTransferObject.class);

	/** 成功 */
	public static final int SUCCESS = 0;

	/** 失败 */
	public static final int ERROR = 1;

	/**
	 * 编码
	 */
	private int code = 0;

	/**
	 * 消息（错误消息）
	 */
	private String msg = "";

	/**
	 * 成功消息数据
	 */
	private Map<String, Object> data = new HashMap<String, Object>();

	public DataTransferObject() {

	}

	public DataTransferObject(final int code, final String msg, final Map<String, Object> data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	public void setErrCode(final int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(final String msg) {
		this.msg = msg;
	}

	/**
	 * @return the data
	 */
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * 
	 * 向data中写入Entity
	 *
	 * @author zhangshaobin
	 * @created 2012-11-14 下午2:09:33
	 *
	 * @param key
	 * @param value
	 */
	public void putValue(final String key, final Object value) {
		data.put(key, value);
	}

	/**
	 * 
	 * 转换成json字符串
	 *
	 * @author zhangshaobin
	 * @created 2014年10月22日 下午2:39:45
	 *
	 * @return
	 */
	public String toJsonString() {
		return JSONObject.toJSONString(this);
	}

}
