package com.scrat.framework.mongo.mongoConfig;

import java.util.ResourceBundle;

/**
 * 描述：配置mongo
 * 作者 ：kangzz
 * 日期 ：2016-11-15 15:56:04
 */
public class MongoConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "mongoDB";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);

    public static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
