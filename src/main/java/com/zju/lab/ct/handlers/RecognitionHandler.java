package com.zju.lab.ct.handlers;

import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.FeatureDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.verticle.EventBusMessage;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/25.
 */
@RouteHandler("/api/ct")
public class RecognitionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecognitionHandler.class);
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private FeatureDao featureDao;

    public RecognitionHandler(FeatureDao featureDao) {
        this.featureDao = featureDao;
    }


    /**
     * /api/ct/predict
     * POST
     * 病变识别handler
     * @return
     */
    @RouteMapping(value = "/predict", method = RouteMethod.POST)
    public Handler<RoutingContext> predictLesionType() {
        return ctx -> {
            /*
            * data:{
            * "image":String,
            * "x1":int,
            * "y1":int,
            * "x2":int,
            * "y2":int,
            * "type":String
            * }
            * */
            JsonObject data = ctx.getBodyAsJson();
            ctx.vertx().eventBus().send(EventBusMessage.LESION_RECOGNITION, data.encode(), ar -> {
                if (ar.succeeded()) {
                    /*JsonObject result = new JsonObject();
                    result.put("lesion", ar.result().body());
                    ctx.response().end(result.encode());*/
                    ctx.response().end((String)ar.result().body());
                }
                else{
                    ctx.response().setStatusCode(HttpCode.BAD_REQUEST.getCode()).end();
                }
            });
        };
    }

    /**
     * /api/ct/addLiverfeature
     * 添加肝脏病变特征到数据表handler
     * @return
     */
    @RouteMapping(value = "/addLiverfeature", method = RouteMethod.POST)
    public Handler<RoutingContext> addLiverfeature() {
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            String image = data.getString("image");
            int x1 = data.getInteger("x1");
            int y1 = data.getInteger("y1");
            int x2 = data.getInteger("x2");
            int y2 = data.getInteger("y2");
            String label = data.getString("label");
            ImageFeature imageFeature = new ImageFeature();
            JsonObject result = new JsonObject();
            HttpServerResponse re = ctx.response();
            //re.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            try {
                double[] feature = imageFeature.getFeature(image, x1, y1, x2, y2);
                featureDao.addLiverFeature(feature, label, res -> {
                    if ("success".equals(res)){
                        result.put("result", "标注成功");
                        re.setChunked(true).setStatusCode(HttpCode.OK.getCode()).end(result.encode());
                    }
                    else{
                        result.put("result", res);
                        re.setChunked(true).setStatusCode(HttpCode.INTERNAL_SERVER_ERROR.getCode()).end(result.encode());
                    }
                });
            } catch (IOException e) {
                result.put("result", e.getMessage());
                re.setChunked(true).setStatusCode(HttpCode.NOT_FOUND.getCode()).end(result.encode());
            }
        };
    }

    /**
     * /api/ct/addLungfeature
     * 添加肺部病变特征到数据表handler
     * @return
     */
    @RouteMapping(value = "/addLungfeature", method = RouteMethod.POST)
    public Handler<RoutingContext> addLungfeature() {
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            String image = data.getString("image");
            int x1 = data.getInteger("x1");
            int y1 = data.getInteger("y1");
            int x2 = data.getInteger("x2");
            int y2 = data.getInteger("y2");
            String label = data.getString("label");
            ImageFeature imageFeature = new ImageFeature();
            JsonObject result = new JsonObject();
            HttpServerResponse re = ctx.response();
            //re.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            try {
                double[] feature = imageFeature.getFeature(image, x1, y1, x2, y2);
                featureDao.addLungFeature(feature, label, res -> {
                    if ("success".equals(res)){
                        result.put("result", "标注成功");
                        re.setChunked(true).setStatusCode(HttpCode.OK.getCode()).end(result.encode());
                    }
                    else{
                        result.put("result", res);
                        re.setChunked(true).setStatusCode(HttpCode.INTERNAL_SERVER_ERROR.getCode()).end(result.encode());
                    }
                });
            } catch (IOException e) {
                result.put("result", e.getMessage());
                re.setChunked(true).setStatusCode(HttpCode.NOT_FOUND.getCode()).end(result.encode());
            }
        };
    }

    /**
     * 生成算法识别模型
     * /api/ct/generateRecognitionModel
     * POST {
     *     "type":int,
     *     "treeNum":int
     * }
     * @return
     */
    @RouteMapping(value = "/generateRecognitionModel", method = RouteMethod.POST)
    public Handler<RoutingContext> generateRandomForestModel(){
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int type = data.getInteger("type");
            int treeNum = data.getInteger("treeNum");
            String msg = type==1?EventBusMessage.LIVER_ALGORITHM_MODEL_GENERATE:EventBusMessage.LUNG_ALGORITHM_MODEL_GENERATE;
            JsonObject json = new JsonObject();
            json.put("treeNum", treeNum);
            ctx.vertx().eventBus().send(msg, json.encode(), ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("generate recognition model success!");
                    ctx.response().end((String)ar.result().body());
                }
                else{
                    LOGGER.info("generate recognition model failed!");
                    ctx.response().setStatusCode(HttpCode.INTERNAL_SERVER_ERROR.getCode()).end();
                }
            });
        };
    }
}
