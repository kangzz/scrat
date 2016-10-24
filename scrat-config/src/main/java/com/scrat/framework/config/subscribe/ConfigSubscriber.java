package com.scrat.framework.config.subscribe;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.netflix.config.*;
import com.scrat.framework.config.common.Check;
import com.scrat.framework.config.common.SerializableSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述：配置信息订阅
 * 作者 ：kangzz
 * 日期 ：2016-10-21 17:40:49
 */
public class ConfigSubscriber implements ApplicationConfiguration {

    final static Logger logger = LoggerFactory.getLogger(ConfigSubscriber.class);

    private static final String ZK_APPLICATION_NAME_KEY = "zookeeper.application.name";
    private static final String ZK_ENSEMABLE_KEY = "zookeeper.config.ensemble";
    private static final String ZK_ROOT_PATH_KEY = "zookeeper.config.root.path";
    private static final String ZK_SESSION_TIMEOUT_KEY = "zookeeper.session.timeout";
    private static final String ZK_CONN_TIMEOUT_KEY = "zookeeper.connection.timeout";
    private static final String PROPERTIES_RESOURCES_NAME_KEY = "properties.resources.name";

    private CuratorFramework client;

    private String zkConfigRootPath;

    private static ApplicationConfiguration sub;

    /**
     * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
     */
    private ExecutorService pool = Executors.newFixedThreadPool(5);

    private ConfigSubscriber() {
        init();
    }

    /**
     * 描述：获取ConfigSubscriber对象
     * 作者 ：kangzz
     * 日期 ：2016-10-21 17:41:00
     */
    public static synchronized ApplicationConfiguration getInstance() {
        if (null != sub) return sub;
        sub = new ConfigSubscriber();
        return sub;
    }

   /**
    * 描述：初始化zk连接
    * 作者 ：kangzz
    * 日期 ：2016-10-21 17:41:07
    */
    private void init() {
        zkConfigRootPath = DynamicPropertyFactory.getInstance().getStringProperty(ZK_ROOT_PATH_KEY, "/AsuraConfig").get();
        String zkApplicationName = DynamicPropertyFactory.getInstance().getStringProperty(ZK_APPLICATION_NAME_KEY, null).get();
        String zkConfigEnsemble = DynamicPropertyFactory.getInstance().getStringProperty(ZK_ENSEMABLE_KEY, null).get();
        Integer zkConfigSessionTimeout = DynamicPropertyFactory.getInstance().getIntProperty(ZK_SESSION_TIMEOUT_KEY, 15000).get();
        Integer zkConfigConnTimeout = DynamicPropertyFactory.getInstance().getIntProperty(ZK_CONN_TIMEOUT_KEY, 5000).get();

        Transaction tran = Cat.newTransaction("Asura Configuration init", zkApplicationName + "_" + zkConfigEnsemble);

        try {
            String propertiesNames = System.getProperty(PROPERTIES_RESOURCES_NAME_KEY);
            if (Check.NuNStr(propertiesNames)) {
                propertiesNames = DynamicPropertyFactory.getInstance().getStringProperty(PROPERTIES_RESOURCES_NAME_KEY, null).get();
            }

            if (!Check.NuNStr(propertiesNames)) {
                String[] propertiesNamesArr = propertiesNames.split(",");
                for (String propertiesName : propertiesNamesArr) {
                    // configuration: initial classpath properties file
                    try {
                        ConfigurationManager.loadCascadedPropertiesFromResources(propertiesName.trim());
                    } catch (IOException ex) {
                        logger.warn("error initializing asura-config's local file ", ex);
                        Cat.logError("error initializing asura-config's local file ", ex);
                    }
                }
            }

            if (Check.NuNStr(zkConfigEnsemble)) {
                logger.warn("ZooKeeper configuration running in file mode, zk is not enabled since not configured");
                Cat.logError("zk is not enabled since not configured", new RuntimeException("ZooKeeper located at " + zkConfigEnsemble + " is not started."));
                return;
            }

            client = createAndStartZKClient(zkConfigEnsemble, zkConfigSessionTimeout, zkConfigConnTimeout);

            if (client.getState() != CuratorFrameworkState.STARTED) {
                throw new RuntimeException("ZooKeeper located at " + zkConfigEnsemble + " is not started.");
            }

            // 将zk纳入netflix archaius监控，只会监控zkConfigRootPath下的一级子节点变化，目前用到的是根据系统不同，只将系统需要的放入到内存中
//            ZooKeeperConfigurationSource zookeeperConfigSource = new ZooKeeperConfigurationSource(client, zkConfigRootPath);
//            zookeeperConfigSource.start();
//
//            DynamicWatchedConfiguration zookeeperDynamicConfig = new DynamicWatchedConfiguration(zookeeperConfigSource);
//
//            // insert ZK DynamicConfig into the 2nd spot
//            ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).addConfigurationAtIndex(zookeeperDynamicConfig, "zk dynamic override", 1);

            //ConfigurationManager.install(zookeeperDynamicConfig);
            tran.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            logger.error("连接配置中心服务器超时，时间5000毫秒。", e);
            Cat.logError("asura configuration init exception", e);
            tran.setStatus(e);
            System.exit(1);
        } finally {
            tran.complete();
        }
        System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + zkApplicationName + " connected to cofnig server(" + zkConfigEnsemble + ").");
        logger.info(zkApplicationName + " connected to cofnig server(" + zkConfigEnsemble + ").");
    }

    /**
     * 创建连接
     *
     * @param connectString
     *
     * @return
     */
    private synchronized static CuratorFramework createAndStartZKClient(String connectString, Integer zkSessionTimeout, Integer zkConnTimeout) {

        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, zkSessionTimeout, zkConnTimeout, new RetryNTimes(Integer.MAX_VALUE, 1000));

        client.start();

        logger.info("Created, started, and cached zk client [{}] for connectString [{}]", client, connectString);

        return client;
    }

    /**
     * 描述：注册配置的type、code，以保证数据在发生变化时能及时得到通知
     * @param type 配置信息类型
     * @param code 配置信息编码
     * @return
     * 作者 ：kangzz
     * 日期 ：2016-10-21 17:41:23
     */
    @Override
    public void registConfig(final String type, final String code) throws Exception {
        final String suffixPath = "/" + type + "/" + code;
        registConfig(suffixPath);
    }

    /**
     * 注册配置的path，以保证数据在发生变化时能及时得到通知
     * @param suffixPath
     * @throws Exception
     */
    public void registConfig(final String suffixPath) throws Exception {
        final String path = pasePath(suffixPath);
        if (isExistsNode(path)) {
            final String data = (String) SerializableSerializer.deserialize(client.getData().forPath(path));
            if (!Check.NuNStr(data)) {
                ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).setOverrideProperty(paseKey(path), data);
            }
        }

        /**
         * 监听数据节点的变化情况
         */
        final NodeCache nodeCache = new NodeCache(client, path, false);

        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                ChildData childData = nodeCache.getCurrentData();
                if (Check.NuNObj(childData)) {
                    ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).clearOverrideProperty(path);
                } else {
                    String data = (String) SerializableSerializer.deserialize(childData.getData());
                    ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).setOverrideProperty(paseKey(path), data);
                }
            }
        }, pool);
    }
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
    @Override
    public String getConfigValue(final String type, final String code) {
        final String suffixPath =  "/" + type + "/" + code;
        return getConfigValue(suffixPath);
    }

    /**
     * 从zookeeper上获取配置信息
     * @param suffixPath 配置路径
     * @return 配置信息值
     */
    public String getConfigValue(final String suffixPath) {
        final String path = pasePath(suffixPath);
        String data = DynamicPropertyFactory.getInstance().getStringProperty(paseKey(path), null).get();
        if (Check.NuNStr(data)) {
            try {
                if (isExistsNode(path)) {
                    data = (String) SerializableSerializer.deserialize(client.getData().forPath(path));
                } else {
                    throw new RuntimeException("zk configuration can not find. key:" + path);
                }
                if (!Check.NuNStr(data)) {
                    ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).setOverrideProperty(paseKey(path), data);
                } else {
                    throw new RuntimeException("zk configuration value is null. key:" + path);
                }
            } catch (Exception e) {
                logger.error("can't get asura configuration by " + path, e);
                Transaction tran = Cat.newTransaction("get Asura Configuration", path);
                Cat.logError("can't get asura configuration by " + path, e);
                tran.setStatus(e);
                tran.complete();
            }
        }
        return DynamicPropertyFactory.getInstance().getStringProperty(paseKey(path), null).get();
    }
    /**
     * 判断一个节点是否存在
     *
     * @param path
     *         节点路径
     *
     * @throws Exception
     */
    private boolean isExistsNode(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return null != stat;
    }

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    @Override
    public String getString(String key, String defaultValue) {
        final DynamicStringProperty property = DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue);
        return property.get();
    }

    @Override
    public String getString(String key, String defaultValue, Runnable callback) {
        final DynamicStringProperty property = DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue, callback);
        return property.get();
    }

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    @Override
    public int getInt(String key, int defaultValue) {
        final DynamicIntProperty property = DynamicPropertyFactory.getInstance().getIntProperty(key, defaultValue);
        return property.get();
    }

    @Override
    public int getInt(String key, int defaultValue, Runnable callback) {
        final DynamicIntProperty property = DynamicPropertyFactory.getInstance().getIntProperty(key, defaultValue, callback);
        return property.get();
    }

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    @Override
    public long getLong(String key, long defaultValue) {
        final DynamicLongProperty property = DynamicPropertyFactory.getInstance().getLongProperty(key, defaultValue);
        return property.get();
    }

    @Override
    public long getLong(String key, long defaultValue, Runnable callback) {
        final DynamicLongProperty property = DynamicPropertyFactory.getInstance().getLongProperty(key, defaultValue, callback);
        return property.get();
    }

    /**
     * @param key
     *         配置项的key
     * @param defaultValue
     *         如果取回的配置项值为空, 应该返回的默认值
     *
     * @return 配置项的值
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        final DynamicBooleanProperty property = DynamicPropertyFactory.getInstance().getBooleanProperty(key, defaultValue);
        return property.get();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue, Runnable callback) {
        final DynamicBooleanProperty property = DynamicPropertyFactory.getInstance().getBooleanProperty(key, defaultValue, callback);
        return property.get();
    }

    /**
     * Sets an instance-level override. This will trump everything including
     * dynamic properties and system properties. Useful for tests.
     *
     * @param key
     * @param value
     */
    @Override
    public void setOverrideProperty(String key, Object value) {
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).setOverrideProperty(key, value);
    }

    private String pasePath(String key){
        String path = StringUtils.replace(key, ".", "/");
        if (path.startsWith("/")) {
            return zkConfigRootPath + path;
        }
        return zkConfigRootPath + "/" + path;
    }

    private String paseKey(String path){
        String toKey = path.substring(zkConfigRootPath.length() + 1);
        return StringUtils.replace(toKey, "/", ".");
    }
}
