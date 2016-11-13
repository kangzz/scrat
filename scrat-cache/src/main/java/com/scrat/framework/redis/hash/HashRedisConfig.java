package com.scrat.framework.redis.hash;

import java.util.ResourceBundle;
/**
 * 描述：读取redis配置
 * 作者 ：kangzz
 * 日期 ：2016-11-14 00:03:25
 */
public class HashRedisConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "hashRedis";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);

    public static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
