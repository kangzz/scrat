package com.scrat.framework.elasticsearch.utils;


public class EsResultEntity<T> {
    private String id;
    private float score;
    private T source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }
}
