package com.scrat.framework.redis.sharded;

import java.util.ResourceBundle;

/**
 * 描述：读取redis配置
 * 作者 ：kangzz
 * 日期 ：2016-11-14 00:04:12
 */
public class SharedRedisConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "shardedRedis";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);

    public static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
