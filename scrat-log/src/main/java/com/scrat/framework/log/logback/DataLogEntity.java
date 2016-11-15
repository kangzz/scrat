package com.scrat.framework.log.logback;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * 描述：系统日志数据日志实体
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:09:30
 */
public class DataLogEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5138676334265605421L;
	//类名
	private String className;
	//方法名
	private String methodName;
	//参数
	private Object[] params;

	public DataLogEntity() {

	}

	public DataLogEntity(String className, String methodName, Object[] params) {
		this.className = className;
		this.methodName = methodName;
		this.params = params;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}


	/**
	 *
	 * toString方法，返回属性名称及值
	 *
	 * @author zhangshaobin
	 * @created 2012-12-19 上午10:16:37
	 *
	 * @return	属性名称及值，格式：[name]张三 [sex]男
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			Class<? extends Object> clazz = this.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if ("serialVersionUID".equals(fieldName)) {
					continue;
				}
				String methodName = "get" + fieldName.substring(0, 1).toUpperCase()
						+ fieldName.substring(1, fieldName.length());
				Method method = null;
				Object resultObj = null;
				method = clazz.getMethod(methodName);
				resultObj = method.invoke(this);
				if (resultObj != null && !"".equals(resultObj)) {
					result.append("[").append(fieldName).append("]").append(resultObj).append(" ");
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 *
	 * 根据字段转换json串
	 *
	 * @author zhangshaobin
	 * @created 2014年11月28日 下午3:33:23
	 *
	 * @param paramName
	 * @return
	 */
	public String toJsonStr(String... paramName) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		Map<String, Object> map = this.toMap();
		for (String pn : paramName) {
			if (map.get(pn) != null) {
				jsonMap.put(pn, map.get(pn));
			}
		}
		return JSONObject.toJSONString(jsonMap);
	}

	/**
	 *
	 * 根据字段转换json串
	 */
	public String toJsonStr() {
		return JSONObject.toJSONString(this);
	}

	/**
	 *
	 * 对象转换成map
	 *
	 * @author zhangshaobin
	 * @created 2014年11月28日 下午3:35:05
	 *
	 * @return
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Class<? extends Object> clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				String fieldName = field.getName();
				if ("serialVersionUID".equals(fieldName)) {
					continue;
				}
				String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
				map.put(fieldName, clazz.getMethod(methodName).invoke(this));

			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}
