package com.scrat.framework.mongo.utils;


import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.scrat.framework.mongo.mongoConfig.MongoConfig;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：mongoDb
 * 作者 ：kangzz
 * 日期 ：2016-11-15 14:33:56
 */
public enum MongoDBUtil {
/**
 * 定义一个枚举的元素，它代表此类的一个实例
 */
    instance;
    //不适用fastJson 因为_id会有问题
    private Gson gson = new Gson();
    private MongoClient mongoClient;

    static {
        String ip = MongoConfig.getConfigProperty("host");
        String port = MongoConfig.getConfigProperty("port");
        instance.mongoClient = new MongoClient(ip, Integer.valueOf(port));

        // or, to connect to a replica set, with auto-discovery of the primary, supply a seed list of members
        // List<ServerAddress> listHost = Arrays.asList(new ServerAddress("localhost", 27017),new ServerAddress("localhost", 27018));
        // instance.mongoClient = new MongoClient(listHost);

        // 大部分用户使用mongodb都在安全内网下，但如果将mongodb设为安全验证模式，就需要在客户端提供用户名和密码：
        // boolean auth = db.authenticate(myUserName, myPassword);
        Builder options = new Builder();
        // options.autoConnectRetry(true);// 自动重连true
        // options.maxAutoConnectRetryTime(10); // the maximum auto connect retry time
        options.connectionsPerHost(300);// 连接池设置为300个连接,默认为100
        options.connectTimeout(15000);// 连接超时，推荐>3000毫秒
        options.maxWaitTime(5000); //
        options.socketTimeout(0);// 套接字超时时间，0无限制
        options.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数，如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误。
        options.writeConcern(WriteConcern.SAFE);//
        options.build();
    }

    // ------------------------------------共用方法---------------------------------------------------
    /**
     * 获取DB实例 - 指定DB
     *
     * @param dbName
     * @return
     */
    public MongoDatabase getDB(String dbName) {
        if (StringUtils.isNotBlank(dbName)) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            return database;
        }
        return null;
    }

    /**
     * 获取collection对象 - 指定Collection
     *
     * @param collName
     * @return
     */
    public MongoCollection<Document> getCollection(String dbName, String collName) {
        if (StringUtils.isBlank(collName)) {
            return null;
        }
        if (StringUtils.isBlank(dbName)) {
            return null;
        }
        MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collName);
        return collection;
    }

    /**
     * 查询DB下的所有表名
     */
    public List<String> getAllCollections(String dbName) {
        MongoIterable<String> colls = getDB(dbName).listCollectionNames();
        List<String> _list = new ArrayList<>();
        for (String s : colls) {
            _list.add(s);
        }
        return _list;
    }

    /**
     * 获取所有数据库名称列表
     *
     * @return
     */
    public MongoIterable<String> getAllDBNames() {
        MongoIterable<String> s = mongoClient.listDatabaseNames();
        return s;
    }

    /**
     * 删除一个数据库
     */
    public void dropDB(String dbName) {
        getDB(dbName).drop();
    }

    /**
     * 查找对象 - 根据主键_id
     *
     * @param coll
     * @param id
     * @return
     */
    public Document findById(MongoCollection<Document> coll, String id) {
        ObjectId _idobj = null;
        try {
            _idobj = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        Document myDoc = coll.find(Filters.eq("_id", _idobj)).first();
        return myDoc;
    }
    /** 条件查询 */
    public <T> T findById(MongoCollection<Document> coll, String id, Class<T> componentType) {
        Document document = this.findById(coll, id);
        return gson.fromJson(document.toJson(),componentType);
    }
    /** 统计数 */
    public int getCount(MongoCollection<Document> coll) {
        int count = (int) coll.count();
        return count;
    }
    /** 条件查询 */
    public MongoCursor<Document> find(MongoCollection<Document> coll,BasicDBObject basicDBObject) {
        return coll.find(basicDBObject)
                .sort(new BasicDBObject("_id",1))
                .iterator();
    }
    /** 条件查询 */
    public <T> List<T> find(MongoCollection<Document> coll,BasicDBObject basicDBObject,Class<T> componentType) {
        MongoCursor<Document> res = this.find(coll, basicDBObject);
        List<T> returnList = new ArrayList<>();
        if(res!=null){
            while (res.hasNext()){
                Document document = res.next();
                returnList.add(gson.fromJson(document.toJson(),componentType));
            }
        }
        return returnList;
    }
    /** 条件查询总行数 */
    public Long findCount(MongoCollection<Document> coll,BasicDBObject basicDBObject) {
        return coll.count(basicDBObject);
    }
    /** 分页查询 */
    public MongoCursor<Document> findByPage(MongoCollection<Document> coll, BasicDBObject basicDBObject, int pageNo, int pageSize) {
        return coll.find(basicDBObject)
                .skip((pageNo-1)*pageSize)
                .sort(new BasicDBObject("_id",1))
                .limit(pageSize)
                .iterator();
    }

    /** 分页查询 */
    public  <T> List<T> findByPage(MongoCollection<Document> coll, BasicDBObject basicDBObject, int pageNo, int pageSize ,Class<T> componentType) {
        MongoCursor<Document> res = this.findByPage(coll, basicDBObject, pageNo, pageSize);
        List<T> returnList = new ArrayList<>();
        if(res!=null){
            while (res.hasNext()){
                Document document = res.next();
                returnList.add(gson.fromJson(document.toJson(),componentType));
            }
        }
        return returnList;
    }
    /**
     * 通过ID删除
     *
     * @param coll
     * @param id
     * @return
     */
    public int deleteById(MongoCollection<Document> coll, String id) {
        int count = 0;
        ObjectId _id = null;
        try {
            _id = new ObjectId(id);
        } catch (Exception e) {
            return 0;
        }
        Bson filter = Filters.eq("_id", _id);
        DeleteResult deleteResult = coll.deleteOne(filter);
        count = (int) deleteResult.getDeletedCount();
        return count;
    }

    /**
     * @param coll
     * @param id
     * @param newdoc
     * @return
     */
    public Document updateById(MongoCollection<Document> coll, String id, Document newdoc) {
        ObjectId _idobj = null;
        try {
            _idobj = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        Bson filter = Filters.eq("_id", _idobj);
        // coll.replaceOne(filter, newdoc); // 完全替代
        coll.updateOne(filter, new Document("$set", newdoc));
        return newdoc;
    }

    public void dropCollection(String dbName, String collName) {
        getDB(dbName).getCollection(collName).drop();
    }

    /**
     * 关闭Mongodb
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }


}
