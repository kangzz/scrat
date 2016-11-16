package com.scrat.framework.elasticsearch.utils;

import java.util.List;

public class PageIndexEntity<T> {
    private long total;

    private List<T> result;

    public PageIndexEntity(){

    }

    public PageIndexEntity(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
