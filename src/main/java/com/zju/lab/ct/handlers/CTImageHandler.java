package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.CTImageDao;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
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

    @RouteMapping(method = RouteMethod.GET, value = "/:id")
    public Handler<RoutingContext> getCTImages(){
        return  ctx -> {
            int id = Integer.parseInt(ctx.request().getParam("id"));
            ctImageDao.getCTImages(id, result -> {
                JsonArray cts = new JsonArray();
                if (result != null) {
                    for (CTImage ctImage : result) {
                        JsonObject obj = new JsonObject();
                        obj.put("id", ctImage.getId());
                        obj.put("type", ctImage.getType());
                        obj.put("file", ctImage.getFile());
                        obj.put("diagnosis", ctImage.getDiagnosis());
                        obj.put("consultationId", ctImage.getConsultationId());
                        cts.add(obj);
                    }
                }
                HttpServerResponse response = ctx.response();
                response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
                response.setChunked(true);
                response.end(cts.encode());
            });
        };
    }

    /**
     * /api/ct/:id
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

    @RouteMapping(method = RouteMethod.POST, value = "/record/updateDiagnosis")
    public Handler<RoutingContext> updateDiagnosis(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = data.getInteger("id");
            String diagnosis = data.getString("diagnosis");
            CTImage ctImage = new CTImage();
            ctImage.setId(id);
            ctImage.setDiagnosis(diagnosis);
            ctImageDao.updateCTImage(ctImage, responseMsg -> {
                HttpServerResponse response = ctx.response();
                response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
                response.setChunked(true);
                response.setStatusCode(responseMsg.getCode().getCode()).end(responseMsg.getContent());
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/ct/page")
    public Handler<RoutingContext> getCTImagesByPage(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = data.getInteger("id");
            int pageIndex = data.getInteger("pageIndex");
            int pageSize = data.getInteger("pageSize");
            ctImageDao.getCTImagesByPage(id, pageIndex, pageSize, result -> {
                HttpServerResponse response = ctx.response();
                response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
                response.setChunked(true);
                if (result != null) {
                    /*int count = result.getInteger("count");
                    JsonArray cts = new JsonArray();
                    List<JsonObject> array = result.getJsonArray("ct").getList();
                    for (JsonObject obj : array) {
                        cts.add(obj);
                    }*/
                    response.end(result.encode());
                }
                else{
                    response.setStatusCode(HttpCode.NULL_CONTENT.getCode()).end();
                }
            });
        };
    }
}

