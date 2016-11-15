package com.scrat.framework.elasticsearch.client;

import java.util.ResourceBundle;

/**
 * 描述：elasticsearch 配置文件
 * 作者 ：kangzz
 * 日期 ：2016-11-15 21:05:45
 */
public class ElasticsearchConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "elasticsearch";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);

    public static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
