package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.verticle.EventBusMessage;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.json.JSONObject;
import java.io.File;

/**
 * Created by wuhaitao on 2016/3/30.
 */
@RouteHandler("/api/ct")
public class LesionDetectionHandler {

    @RouteMapping(value = "/segmentation", method = RouteMethod.POST)
    public Handler<RoutingContext> predictLesionType() {
        return ctx -> {
            /*
            * data:{
            * "image":String,
            * "seedX":int,
            * "seedY":int
            * }
            * */
            JsonObject data = ctx.getBodyAsJson();
            ctx.vertx().eventBus().send(EventBusMessage.LIVER_SEGMENTATION, data.encode(), ar -> {
                if (ar.succeeded()) {
                    JSONObject res = new JSONObject((String)ar.result().body());
                    int code = res.getInt("code");
                    if (code == HttpCode.OK.getCode()){
                        ctx.response().end(res.getString("image"));
                    }
                    else{
                        ctx.response().setStatusCode(code).end(res.getString("error"));
                    }
                }
                else{
                    ctx.response().setStatusCode(HttpCode.BAD_REQUEST.getCode()).end();
                }
            });
        };
    }

    @RouteMapping(method = RouteMethod.GET, value = "/segmentation/:image")
    public Handler<RoutingContext> getCTImage(){
        return  ctx -> {
            String image = ctx.request().getParam("image");
            HttpServerResponse response = ctx.response();
            response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            response.setChunked(true);
            response.sendFile(AppUtil.getSegmentationDir() + File.separator + image);
        };
    }
}
