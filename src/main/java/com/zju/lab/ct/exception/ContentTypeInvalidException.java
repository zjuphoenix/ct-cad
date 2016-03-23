package com.zju.lab.ct.exception;

/**
 * Created by wuhaitao on 2016/3/23.
 */
public class ContentTypeInvalidException extends RuntimeException{
    private static final long serialVersionUID = 508651458709970043L;

    /**
     *
     * @param message
     * 异常信息
     */
    public ContentTypeInvalidException(String message) {
        super(message);
    }
}
