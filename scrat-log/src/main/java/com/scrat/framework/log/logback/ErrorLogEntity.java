package com.scrat.framework.log.logback;

/**
 * 描述：系统日志错误日志实体
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:09:08
 */
public class ErrorLogEntity extends DataLogEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7175924822251701372L;
	//抛出异常参数
	private String throwMessage;

	public ErrorLogEntity(DataLogEntity dle) {
		super(dle.getClassName(), dle.getMethodName(), dle.getParams());
	}

	public String getThrowMessage() {
		return throwMessage;
	}

	public void setThrowMessage(String throwMessage) {
		this.throwMessage = throwMessage;
	}

}
