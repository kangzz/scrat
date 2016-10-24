package com.scrat.framework.config.publish;
/**
 * 描述：初始化发布者
 * 作者 ：kangzz
 * 日期 ：2016-10-21 15:53:49
 */
public class InitPublisher {

    private InitPublisher() {
    }
    /**
     * 描述：初始化
     * 作者 ：kangzz
     * 日期 ：2016-10-21 15:54:02
     */
    public void init() {
        ConfigPublisher.getInstance();
    }

    /**
     * Stop the client
     */
   /* public void destroy() {
        ConfigPublisher.getInstance().destroy();
    }*/
}
