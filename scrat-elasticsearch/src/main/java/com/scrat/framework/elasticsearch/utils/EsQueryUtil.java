package com.scrat.framework.elasticsearch.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scrat.framework.elasticsearch.client.EshClientFactory;
import com.scrat.framework.elasticsearch.exception.EsException;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.threadpool.ThreadPoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：Query
 * 作者 ：kangzz
 * 日期 ：2016-11-15 22:44:26
 */
public class EsQueryUtil {
    private static Logger logger = LoggerFactory.getLogger(EsQueryUtil.class);
    private EsQueryUtil(){

    }
   public static SearchResponse query(SearchSourceBuilder searchSourceBuilder, String indexName, String... typeNames) throws EsException {
        validationEsQuery(indexName,typeNames);
        SearchResponse response= EshClientFactory.getClient().prepareSearch(EshClientFactory.getIndexs(indexName)).setTypes(typeNames).setSource(searchSourceBuilder.toString()).execute().actionGet();
       return response;
    }
    public static EsResponse queryByEsQueryDo(EsQueryDo esQueryObj) throws EsException {
        validationEsQuery(esQueryObj.getIndexName(),esQueryObj.getTypeName());
        //创建ES查询Request对象
        SearchRequestBuilder esSearch=buildSearchRequest (esQueryObj);
        //执行查询
        SearchResponse response =esSearch.execute().actionGet();
        JSONObject resObj = new JSONObject();
        //获取facet结果
        if(esQueryObj.aggregationFields() !=null && esQueryObj.aggregationFields().length>0){
            parseAggregationResult(response, esQueryObj.aggregationFields(), resObj);
        }
        //1、获取搜索的文档结果
        SearchHits searchHits = response.getHits();
        if (searchHits == null || searchHits.getTotalHits() == 0) {
            return EsResponse.responseOK(null);
        }
        SearchHit[] hits = searchHits.getHits();
        resObj.put("total", searchHits.getTotalHits());
        //1.1、获取搜索结果
        parseSearchResult(hits, esQueryObj.isHighLigth(), esQueryObj, resObj);
        return EsResponse.responseOK(resObj);
    }
    private static SearchRequestBuilder buildSearchRequest (EsQueryDo esQueryObj) throws  EsException {
        if (StringUtils.isBlank(EshClientFactory.getIndexs(esQueryObj.getIndexName()))) {
            throw new EsException("没有指定要搜索的索引名称(indexName)");
        }
        for (ThreadPoolStats.Stats stats : EshClientFactory.getClient().threadPool().stats()) {
            logger.info(JSON.toJSONString(stats));
        }
        //加载要搜索索引
        SearchRequestBuilder searchRequestBuilder = EshClientFactory.getClient().prepareSearch(EshClientFactory.getIndexs(esQueryObj.getIndexName()));
        //由spring从配置加载要搜索的index的类型
        searchRequestBuilder.setTypes(esQueryObj.getTypeName());
        //由spring从配置加载要搜索的类型
        searchRequestBuilder.setSearchType(SearchType.fromId(esQueryObj.getSearchType()));
        //查询可以为null
        searchRequestBuilder.setQuery(esQueryObj.getQueryBuilder());

        if (esQueryObj.getSortBuilders() !=null && !esQueryObj.getSortBuilders().isEmpty()) {
            for (SortBuilder sortBuilder : esQueryObj.getSortBuilders()) {
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        if (esQueryObj.getAggregationBuilders() !=null && !esQueryObj.getAggregationBuilders().isEmpty()) {
            for (AbstractAggregationBuilder aggregationBuilder : esQueryObj.getAggregationBuilders()) {
                searchRequestBuilder.addAggregation(aggregationBuilder);
            }

        }
        //设置高亮域
       if (esQueryObj.isHighLigth()) {
            if (esQueryObj.highLigthFields()!=null && esQueryObj.highLigthFields().length>0) {
                for (String hlFieldName : esQueryObj.highLigthFields()) {
                    searchRequestBuilder.addHighlightedField(hlFieldName).setHighlighterPreTags(esQueryObj.getHighLigthPreTag())
                            .setHighlighterPostTags(esQueryObj.getHighLigthPostTag());
                }
            }
        }
        //分页
        searchRequestBuilder.setFrom(esQueryObj.getFromIndex()).setSize(esQueryObj.getSize());
        searchRequestBuilder.setExplain(esQueryObj.isExplain());
        return searchRequestBuilder;
    }

    private static void parseSearchResult (SearchHit[] hits, boolean isHighLigth, EsQueryDo esQueryObj, JSONObject resObj) {
        List<JSONObject> searchRes = new ArrayList<JSONObject>();
        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            //将文档中的每一个对象转换json串值
            JSONObject hitJson=new  JSONObject();
            hitJson.put("id",hit.getId());
            hitJson.put("score",hit.getScore());
            JSONObject json = JSON.parseObject(hit.getSourceAsString());
            //如果高亮设置为true
            if (isHighLigth) {
                if (esQueryObj.highLigthFields()!=null && esQueryObj.highLigthFields().length>0) {
                    for (String hlFieldName : esQueryObj.highLigthFields()) {
                        highLightResult(json, hit, hlFieldName);
                    }
                }

            }
            hitJson.put("source",json);
            searchRes.add(hitJson);
        }
        resObj.put("result", searchRes);

    }

    private static void parseAggregationResult (SearchResponse response, String[] aggreFields, JSONObject resObj) {
        List<Map<String, Object>> aggrs = new ArrayList<Map<String, Object>>();
        for (String aggreField : aggreFields) {
            Terms terms = response.getAggregations().get(aggreField);
            if (terms != null) {
                Map<String, Object> maps = new HashMap<String, Object>();
                for (Terms.Bucket bucket : terms.getBuckets()) {
                    maps.put((String)bucket.getKey(), bucket.getDocCount());
                }
                JSONObject jo = new JSONObject();
                jo.put(aggreField, maps);
                aggrs.add(jo);
            }
        }
        resObj.put("aggregations", aggrs);
    }

    /**
     * 对搜索命中结果做高亮
     *
     * @param json
     * @param hit
     * @param highLigthFieldName
     */
    private static void highLightResult (JSONObject json, SearchHit hit, String highLigthFieldName) {
        //获取对应的高亮域
        Map<String, HighlightField> result = hit.highlightFields();
        //从设定的高亮域中取得指定域
        HighlightField hlField = result.get(highLigthFieldName);
        if (null == hlField) {
            return;
        }
        //取得定义的高亮标签
        Text[] hlTexts = hlField.fragments();
        if (null == hlTexts || hlTexts.length == 0) {
            return;
        }
        //为title串值增加自定义的高亮标签
        StringBuffer hlTextsFiled = new StringBuffer();
        for (Text text : hlTexts) {
            hlTextsFiled.append(text);
        }
        //如果高亮域内有fragments 反回的数据不为空字符串
        if (StringUtils.isNotBlank(hlTextsFiled.toString())) {
            json.put(highLigthFieldName, hlTextsFiled);
        }
    }
   /**
     * 验证ES查询对象
     * @param
     * @throws Exception
     */
    private static void validationEsQuery(String  indexName,String... typeNames) throws EsException{
        boolean flag=true;
        for (String typeName : typeNames) {
            if(StringUtils.isBlank(typeName)){
                flag=false;
                break;
            }
        }
        if(StringUtils.isBlank(EshClientFactory.getIndexs(indexName)) ||!flag){
            throw  new EsException("please check indexName and typeName");
        }
        if (!EshClientFactory.getClient().admin().indices().prepareExists(EshClientFactory.getIndexs(indexName)).get().isExists()) {
            throw  new EsException("indexName is not exist in es server");
        }
        if (!EshClientFactory.getClient().admin().indices().prepareTypesExists(EshClientFactory.getIndexs(indexName)).setTypes(typeNames).get().isExists()) {
            throw  new EsException("typeName is not exist in es server");
        }
    }
}
