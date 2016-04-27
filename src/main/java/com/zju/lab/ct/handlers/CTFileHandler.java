package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.io.File;

/**
 * @author wuhaitao
 * @date 2016/4/26 14:44
 */
@RouteHandler("")
public class CTFileHandler {
    /**
     * /upload/:image
     * GET
     * 获取上传的图片在前端显示，这里的图片不包含在静态资源里，而是通过web请求获取
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/upload/:image")
    public Handler<RoutingContext> getCTImage(){
        return  ctx -> {
            String image = ctx.request().getParam("image");
            HttpServerResponse response = ctx.response();
            response.setChunked(true);
            response.sendFile(AppUtil.getUploadDir() + File.separator + image);
        };
    }

    /**
     * /segmentation/:image
     * GET
     * 获取上传的图片在前端显示，这里的图片不包含在静态资源里，而是通过web请求获取
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/segmentation/:image")
    public Handler<RoutingContext> getSegmentationCTImage(){
        return  ctx -> {
            String image = ctx.request().getParam("image");
            HttpServerResponse response = ctx.response();
            response.setChunked(true);
            response.sendFile(AppUtil.getSegmentationDir() + File.separator + image);
        };
    }
}
