package com.scrat.framework.config.publish;

import com.netflix.config.DynamicPropertyFactory;
import com.scrat.framework.config.common.Check;
import com.scrat.framework.config.common.SerializableSerializer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 描述：配置信息发布
 * 作者 ：kangzz
 * 日期 ：2016-10-21 16:20:34
 */
public class ConfigPublisher {

    final static Logger logger = LoggerFactory.getLogger(ConfigPublisher.class);

    private static final String ZK_APPLICATION_NAME_KEY = "zookeeper.application.name";
    private static final String ZK_ENSEMBLE_KEY = "zookeeper.config.ensemble";
    private static final String ZK_ROOT_PATH_KEY = "zookeeper.config.root.path";
    private static final String ZK_SESSION_TIMEOUT_KEY = "zookeeper.session.timeout";
    private static final String ZK_CONN_TIMEOUT_KEY = "zookeeper.connection.timeout";

    private CuratorFramework client;

    private static ConfigPublisher pub;

    private String zkConfigRootPath;

    private ConfigPublisher() {
        init();
    }

   /**
    * 描述：获取ConfigPublisher实例
    * 作者 ：kangzz
    * 日期 ：2016-10-26 20:22:38
    */
    public static synchronized ConfigPublisher getInstance() {
        if (null != pub) return pub;
        pub = new ConfigPublisher();
        return pub;
    }

    /**
     * 描述：初始化
     * 作者 ：kangzz
     * 日期 ：2016-10-26 20:22:52
     */
    private void init() {
        zkConfigRootPath = DynamicPropertyFactory.getInstance().getStringProperty(ZK_ROOT_PATH_KEY, "/AsuraConfig").get();
        String zkApplicationName = DynamicPropertyFactory.getInstance().getStringProperty(ZK_APPLICATION_NAME_KEY, null).get();
        String zkConfigEnsemble = DynamicPropertyFactory.getInstance().getStringProperty(ZK_ENSEMBLE_KEY, null).get();
        Integer zkConfigSessionTimeout = DynamicPropertyFactory.getInstance().getIntProperty(ZK_SESSION_TIMEOUT_KEY, 15000).get();
        Integer zkConfigConnTimeout = DynamicPropertyFactory.getInstance().getIntProperty(ZK_CONN_TIMEOUT_KEY, 5000).get();

        if (Check.NuNStr(zkConfigEnsemble)) {
            logger.warn("ZooKeeper configuration running in file mode, zk is not enabled since not configured");
            return;
        }

        try {
            client = createAndStartZKClient(zkConfigEnsemble, zkConfigSessionTimeout, zkConfigConnTimeout);

            if (client.getState() != CuratorFrameworkState.STARTED) {
                throw new RuntimeException("ZooKeeper located at " + zkConfigEnsemble + " is not started.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("连接配置中心服务器超时，时间5000毫秒。", e.getCause());
            System.exit(1);
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

        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, zkSessionTimeout, zkConnTimeout, new ExponentialBackoffRetry(1000, 3));

        client.start();

        logger.info("Created, started, and cached zk client [{}] for connectString [{}]", client, connectString);

        return client;
    }

    /**
     * 描述：增加配置信息
     * 作者 ：kangzz
     * 日期 ：2016-10-26 20:23:14
     */
    public void setConfig(String type, String code, String data) {
        String path = zkConfigRootPath + "/" + type + "/" + code;
        try {
            if (!isExistsNode(path)) {
                client.create().creatingParentsIfNeeded().forPath(path);
            }

            client.setData().forPath(path, SerializableSerializer.serialize(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 描述：删除配置信息
     * 作者 ：kangzz
     * 日期 ：2016-10-26 20:23:26
     */
    public void deleteConfig(String type, String code) {
        String path = zkConfigRootPath + "/" + type + "/" + code;
        try {
            if (isExistsNode(path)) {
                client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 描述： 从zookeeper上获取配置信息
     *
     * @param type
     *         配置信息类型
     * @param code
     *         配置信息编码
     *
     * @return 配置信息值
     * 作者 ：kangzz
     * 日期 ：2016-10-26 20:23:43
     */
    public String getConfigValue(String type, String code) {
        String path = zkConfigRootPath + "/" + type + "/" + code;
        try {
            if (!isExistsNode(path)) {
                return "配置项不存在";
            }
            return (String) SerializableSerializer.deserialize(client.getData().forPath(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}
