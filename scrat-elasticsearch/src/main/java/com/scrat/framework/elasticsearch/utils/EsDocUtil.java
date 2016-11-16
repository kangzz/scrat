package com.scrat.framework.elasticsearch.utils;

import com.alibaba.fastjson.JSON;
import com.scrat.framework.elasticsearch.client.EshClientFactory;
import com.scrat.framework.elasticsearch.exception.EsDocException;
import com.scrat.framework.elasticsearch.po.DocPo;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述：Doc操作
 * 作者 ：kangzz
 * 日期 ：2016-11-15 22:25:00
 */
public class EsDocUtil{
    private static Logger logger = LoggerFactory.getLogger(EsDocUtil.class);
    private static final int len = 1000;//批量
    private EsDocUtil(){

    }
    /**
     * 修改文档
     * @param indexName
     * @param indexType
     * @param id
     * @param obj
     * @return
     */
    public static boolean updateDocument(String indexName, String indexType,String id,Object obj) {

        TransportClient client = EshClientFactory.getClient();
        client.prepareUpdate(EshClientFactory.getIndexs(indexName),indexType,id).setDoc(JSON.toJSONString(obj)).execute().actionGet();
        /*IndexResponse indexResponse = client.prepareIndex(indexName,indexType).setId(id).
                setSource(JSON.toJSONString(obj)).execute().actionGet();*/
        return true;


    }

    /**
     * 修改文档
     *
     * @param indexName
     * @param indexType
     * @param id
     * @param
     *
     * @return
     */
    public static boolean updateFileds(String indexName, String indexType, String id, Map<String, Object> fieldValues) {
        TransportClient client = EshClientFactory.getClient();
        client.prepareUpdate(EshClientFactory.getIndexs(indexName),indexType,id).setDoc(JSON.toJSONString(fieldValues)).execute().actionGet();
        return true;
    }


    /**
     * 新增文档 es自动生成id
     * @param indexName
     * @param indexType
     * @param obj
     * @return
     */
    public static boolean  createDocument(String indexName, String indexType,Object obj) {
        TransportClient client = EshClientFactory.getClient();
        IndexResponse indexResponse = client.prepareIndex(EshClientFactory.getIndexs(indexName),indexType).
                setSource(JSON.toJSONString(obj)).execute().actionGet();
        return indexResponse.isCreated();
    }
    /**
     * 保存文档
     * @param indexName
     * @param indexType
     * @param obj
     * @return
     */
    public static boolean  saveOrUpdateDocument(String indexName, String indexType,String id,Object obj) {
        TransportClient client = EshClientFactory.getClient();
        if(StringUtils.isBlank(id)){
            logger.error("保存文档失败","id为空，请传id");
            return false;
        }
        IndexResponse indexResponse = client.prepareIndex(EshClientFactory.getIndexs(indexName),indexType).setId(id).
                    setSource(JSON.toJSONString(obj)).execute().actionGet();
        return true;
    }
    /**
     * 批量save
     * @param indexName
     * @param indexType
     * @param list
     * @return
     */
    private  static BulkResponse bulkSaveOrUpdate(String indexName, String indexType, List<DocPo> list){
        TransportClient client = EshClientFactory.getClient();
        BulkResponse bulkResponse=null;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for(int i=0;i<list.size();i++){
            bulkRequest.add(new IndexRequest(EshClientFactory.getIndexs(indexName), indexType,list.get(i).getId()).source(JSON.toJSONString(list.get(i).getObj())));
        }
        bulkResponse = bulkRequest.get();
        // 处理错误信息
        handBulkResponseException(bulkResponse);
        return bulkResponse;
    }
    //处理错误西悉尼
    private static void handBulkResponseException(BulkResponse bulkResponse){
        if (bulkResponse.hasFailures()) {
            long count = 0L;
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                logger.error("发生错误的 索引id为 : "+bulkItemResponse.getId()+" ，错误信息为："+ bulkItemResponse.getFailureMessage());
                count++;
            }
            logger.error("发生错误的 总个数为 : "+count);
        }
    }
    /**
     * 批量del
     * @param indexName
     * @param indexType
     * @param
     * @return
     */
    private  static BulkResponse bulkDelete(String indexName, String indexType, List<String> ids){
        TransportClient client = EshClientFactory.getClient();
        BulkResponse bulkResponse=null;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for(int i=0;i<ids.size();i++){
            bulkRequest.add(new DeleteRequest(EshClientFactory.getIndexs(indexName), indexType,ids.get(i) ));
        }
        bulkResponse = bulkRequest.get();
        // 处理错误信息
        handBulkResponseException(bulkResponse);
        return bulkResponse;
    }
    /**
     * 批量saves
     * @param indexName
     * @param indexType
     * @param list
     * @return
     */
    public  static List<BulkResponse> bulkSaveOrUpdates(String indexName, String indexType, List<DocPo> list,int len)  throws  EsDocException{
        List<BulkResponse> bulkResponses=null;
        if(list == null || list.isEmpty() || 0==len){
            throw  new EsDocException("集合为空 后者 批处理量为0");
        }
        boolean flag=true;
        for(int i=0;i<list.size();i++){
            if(StringUtils.isBlank(list.get(i).getId())){
                flag=false;
                break;
            }
        }
        if(!flag){
            throw  new EsDocException("id 不能为空");
        }
        bulkResponses=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            BulkResponse bulkResponse=null;
            int end = len + i -1;
            if(end>=list.size()){
                bulkResponse=bulkSaveOrUpdate(indexName,indexType,list.subList(i,list.size()));
            }else{
                bulkResponse= bulkSaveOrUpdate(indexName,indexType,list.subList(i,end+1));
            }
            i = end;
            bulkResponses.add(bulkResponse);
        }
        return bulkResponses;
    }
    /**
     * 批量saves
     * @param indexName
     * @param indexType
     * @param list
     * @return
     */
    public  static List<BulkResponse> bulkSaveOrUpdates(String indexName, String indexType, List<DocPo> list) throws EsDocException{
        return bulkSaveOrUpdates(indexName,  indexType, list,len);
    }
    /**
     * delete
     * @param indexName
     * @param indexType
     * @param list
     * @return
     */
    public  static List<BulkResponse> bulkDeletes(String indexName, String indexType, List<String> list,int len)  throws EsDocException{
        List<BulkResponse> bulkResponses=null;
        if(list == null || list.isEmpty() || 0==len){
            throw  new EsDocException("集合为空 后者 批处理量为0");
        }
        boolean flag=true;
        for(int i=0;i<list.size();i++){
            if(StringUtils.isBlank(list.get(i))){
                flag=false;
                break;
            }
        }
        if(!flag){
            throw  new EsDocException("id 不能为空");
        }
        bulkResponses=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            BulkResponse bulkResponse=null;
            int end = len + i -1;
            if(end>=list.size()){
                bulkResponse=bulkDelete(indexName,indexType,list.subList(i,list.size()));
            }else{
                bulkResponse= bulkDelete(indexName,indexType,list.subList(i,end+1));
            }
            i = end;
            bulkResponses.add(bulkResponse);
        }
        return bulkResponses;
    }
    /**
     * 批量delete
     * @param indexName
     * @param indexType
     * @param list
     * @return
     */
    public static  List<BulkResponse> bulkDeletes(String indexName, String indexType, List<String> list) throws EsDocException {
        return bulkDeletes(indexName,  indexType, list,len);
    }
    /**
     * 删除文档
     * @param indexName
     * @param indexType
     * @param id
     * @return
     */
    public  static boolean deleteDocument(String indexName, String indexType, String id) {
        TransportClient client = EshClientFactory.getClient();
        DeleteResponse indexResponse=client.prepareDelete(EshClientFactory.getIndexs(indexName), indexType, id).get();
        return indexResponse.isFound();
    }
}
