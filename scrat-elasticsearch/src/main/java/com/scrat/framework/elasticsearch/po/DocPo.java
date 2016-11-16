package com.scrat.framework.elasticsearch.po;


public class DocPo {
    /**
     * esId，类似主键
     */
    private String id;
    /**
     * 文档对象
     */
    private Object  obj;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
