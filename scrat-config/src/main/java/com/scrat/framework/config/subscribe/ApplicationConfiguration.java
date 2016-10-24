package com.scrat.framework.config.subscribe;
/**
 * 描述：
 * 作者 ：kangzz
 * 日期 ：2016-10-21 17:39:45
 */
public interface ApplicationConfiguration {

    /**
     * 注册配置的type、code，以保证数据在发生变化时能及时得到通知
     *
     * @param type
     *         配置信息类型
     * @param code
     *         配置信息编码
     */
    void registConfig(final String type, final String code) throws Exception;

    /**
     * 注册配置的suffixPath，以保证数据在发生变化时能及时得到通知
     *
     */
    void registConfig(final String suffixPath) throws Exception;

    /**
     * 从zookeeper上获取配置信息
     *
     * @param type
     *         配置信息类型
     * @param code
     *         配置信息编码
     *
     * @return 配置信息值
     */
    String getConfigValue(final String type, final String code);
    /**
     * 从zookeeper上获取配置信息
     *
     * @param path
     *         配置信息路径
     * @return 配置信息值
     */
    String getConfigValue(final String path);

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    String getString(String key, String defaultValue);

    String getString(String key, String defaultValue, Runnable callback);

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    int getInt(String key, int defaultValue);

    int getInt(String key, int defaultValue, Runnable callback);

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    long getLong(String key, long defaultValue);

    long getLong(String key, long defaultValue, Runnable callback);

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    boolean getBoolean(String key, boolean defaultValue);

    boolean getBoolean(String key, boolean defaultValue, Runnable callback);

    /**
     * Sets an instance-level override. This will trump everything including
     * dynamic properties and system properties. Useful for tests.
     *
     * @param key
     * @param value
     */
    void setOverrideProperty(String key, Object value);

}
