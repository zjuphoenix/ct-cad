package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.CTImageDao;
import com.zju.lab.ct.utils.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuhaitao on 2016/3/10.
 */
@RouteHandler("/api/ct")
public class CTImageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTImageHandler.class);

    private CTImageDao ctImageDao;

    public CTImageHandler(CTImageDao ctImageDao) {
        this.ctImageDao = ctImageDao;
    }

    /**
     * url: /api/ct/data/:recordId
     * GET
     * 根据recordId获取所有CT图像
     * 返回类型JsonObject key:{ct,count}
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/data/:recordId")
    public Handler<RoutingContext> getCTImages(){
        return  ctx -> {
            int recordId = Integer.parseInt(ctx.request().getParam("recordId"));
            ctImageDao.getCTImages(recordId, result -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, result);
            });
        };
    }

    /**
     * url: /api/ct/:id
     * DELETE
     * 根据id删除CT图像
     * @return
     */
    @RouteMapping(method = RouteMethod.DELETE, value = "/:id")
    public Handler<RoutingContext> deleteCTImageById(){
        return  ctx -> {
            int id = Integer.parseInt(ctx.request().getParam("id"));
            ctImageDao.deleteCTImageById(id, result -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, result);
            });
        };
    }

    /**
     * url: /api/ct/:id
     * GET
     * 根据id获取一条CT数据
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/:id")
    public Handler<RoutingContext> getCTImage(){
        return  ctx -> {
            int id = Integer.parseInt(ctx.request().getParam("id"));
            ctImageDao.getCTImageById(id, ctImage -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, ctImage);
            });
        };
    }

    /**
     * url: /api/ct
     * PUT
     * 更新CT图像诊断结果
     * @return
     */
    @RouteMapping(method = RouteMethod.PUT)
    public Handler<RoutingContext> updateDiagnosis(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = data.getInteger("id");
            String diagnosis = data.getString("diagnosis");
            ctImageDao.updateCTImage(id, diagnosis, responseMsg -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, responseMsg);
            });
        };
    }

    /**
     * url: /api/ct
     * POST
     * 根据recordId获取CT图像分页数据
     * 返回类型JsonObject key:{ct,count}
     * @return
     */
    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> getCTImagesByPage(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int recordId = data.getInteger("recordId");
            int pageIndex = data.getInteger("pageIndex");
            int pageSize = data.getInteger("pageSize");
            ctImageDao.getCTImagesByPage(recordId, pageIndex, pageSize, result -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, result);
            });
        };
    }
}

