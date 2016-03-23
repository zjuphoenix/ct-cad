package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/3/9.
 */
public class ResponseMsg<T> {

    private T content;
    private HttpCode code;
    private String error;

    public ResponseMsg(T content) {
        this.code = HttpCode.OK;
        this.content = content;
    }
    public ResponseMsg(HttpCode code, String error) {
        this.code = code;
        this.error = error;
    }
    public HttpCode getCode() {
        return code;
    }
    public void setCode(HttpCode code) {
        this.code = code;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}