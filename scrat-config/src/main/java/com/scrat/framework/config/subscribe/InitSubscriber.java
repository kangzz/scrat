package com.scrat.framework.config.subscribe;

/**
 * 描述：初始化订阅者
 * 作者 ：kangzz
 * 日期 ：2016-10-21 17:42:20
 */
public class InitSubscriber {

	private InitSubscriber() {
	}
	/**
	 * 描述：初始化
	 * 作者 ：kangzz
	 * 日期 ：2016-10-21 17:42:29
	 */
	public void init() {
		ConfigSubscriber.getInstance();
		AsuraSubAnnotationProcessor.getInstance(); // 必须在ConfigSubscriber.getInstance()之后
	}

}
