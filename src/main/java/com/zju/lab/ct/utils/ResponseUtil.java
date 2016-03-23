package com.zju.lab.ct.utils;

import com.zju.lab.ct.exception.ContentTypeInvalidException;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by wuhaitao on 2016/3/23.
 */
public class ResponseUtil {

    public static void responseContent(HttpServerResponse response, ResponseMsg responseMsg) throws ContentTypeInvalidException{
        response.putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS")
                .putHeader("Access-Control-Max-Age", "60")
                .putHeader("Access-Control-Allow-Credentials", "false");
        response.setChunked(true);
        if (responseMsg.getCode().getCode() == HttpCode.OK.getCode()){
            Object content = responseMsg.getContent();
            if (content instanceof String){
                response.end((String) content);
            }
            else if (content instanceof JsonObject){
                JsonObject obj = (JsonObject) content;
                response.end(obj.encode());
            }
            else if (content instanceof JsonArray){
                JsonArray array = (JsonArray) content;
                response.end(array.encode());
            }
            else {
                throw new ContentTypeInvalidException(content.getClass().getName());
            }
        }
        else{
            response.setStatusCode(responseMsg.getCode().getCode()).end(responseMsg.getError());
        }
    }
}
