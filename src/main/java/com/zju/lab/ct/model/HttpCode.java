package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/3/9.
 */
public enum HttpCode {
    OK(200),

    /**资源为空*/
    NULL_CONTENT(204),

    /**资源未更改*/
    NOT_MODIFIED(304),

    /**指代坏请求（如，参数错误）*/
    BAD_REQUEST(400),

    /**未授权*/
    UNAUTHORIZED(401),

    NOT_FOUND(404),

    /**服务器内部错误*/
    INTERNAL_SERVER_ERROR(500),

    /**服务不可达*/
    SERVICE_UNAVAILABLE(503),

    ;

    private int code;

    private HttpCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
