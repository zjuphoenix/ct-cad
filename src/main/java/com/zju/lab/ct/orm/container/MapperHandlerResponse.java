package com.zju.lab.ct.orm.container;

import com.zju.lab.ct.model.HttpCode;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by wuhaitao on 2016/3/26.
 */
public class MapperHandlerResponse {
    /*查询成功返回数据*/
    private List<JsonObject> result;
    /*数据库操作失败错误信息*/
    private HttpCode code;
    private String error;
    /*数据库插入和更新返回的成功记录的主键*/
    private JsonArray keys;

    public MapperHandlerResponse() {
    }

    public MapperHandlerResponse(List<JsonObject> result, HttpCode code, String error, JsonArray keys) {
        this.result = result;
        this.code = code;
        this.error = error;
        this.keys = keys;
    }

    public List<JsonObject> getResult() {
        return result;
    }

    public void setResult(List<JsonObject> result) {
        this.result = result;
    }

    public HttpCode getCode() {
        return code;
    }

    public void setCode(HttpCode code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public JsonArray getKeys() {
        return keys;
    }

    public void setKeys(JsonArray keys) {
        this.keys = keys;
    }
}
