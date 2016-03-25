package com.zju.lab.ct.verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.dao.FeatureDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Created by wuhaitao on 2016/2/27.
 */
public class LesionRecognitionServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(LesionRecognitionServer.class);
    private ImageFeature imageFeature;
    private JsonObject lesion;
    private JsonObject lung;
    private Integer port = AppUtil.configInt("algorithm.server.port");

    @Override
    public void start() throws Exception {

        imageFeature = new ImageFeature();
        try {
            URL url = getClass().getClassLoader().getResource(Constants.LESION);
            LOGGER.debug("Initialize liver lesion type from path : {}", url);
            ObjectMapper mapper = new ObjectMapper();
            lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));

            url = getClass().getClassLoader().getResource(Constants.LUNG);
            LOGGER.debug("Initialize lung lesion type from path : {}", url);
            mapper = new ObjectMapper();
            lung = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        /*FeatureDao featureDao = new FeatureDao(vertx);
        RandomForest randomforest = new RandomForest(50, 4);
        featureDao.fetchFeatureSamples(samples -> {
            if (samples == null){
                throw new RuntimeException("cannot get samples!");
            }
            randomforest.createForest(samples);
            LOGGER.info("RandomForest Algorithm finished!");
        });*/
        RandomForest randomforest = AppUtil.getRandomForestModel();
        RandomForest randomforest_Lung = AppUtil.getLungRandomForestModel();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/predict").handler(ctx -> {
            String image = ctx.request().getParam("image");
            int x1 = Integer.valueOf(ctx.request().getParam("x1"));
            int y1 = Integer.valueOf(ctx.request().getParam("y1"));
            int x2 = Integer.valueOf(ctx.request().getParam("x2"));
            int y2 = Integer.valueOf(ctx.request().getParam("y2"));
            String cttype = ctx.request().getParam("type");
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");
            try {
                if ("肝脏".equals(cttype)) {
                    int type = randomforest.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    response.end(lesion.getString(String.valueOf(type)));
                }
                else if("肺部".equals(cttype)){
                    int type = randomforest_Lung.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    response.end(lung.getString(String.valueOf(type)));
                }
                else{
                    response.setStatusCode(HttpCode.BAD_REQUEST.getCode()).end();
                }
            } catch (IOException e) {
                e.printStackTrace();
                response.setStatusCode(500).end();
            }
        });

        server.requestHandler(router::accept).listen(port);
        LOGGER.info("LesionRecognitionServer started!");
    }
}
