package com.zju.lab.ct.verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.dao.FeatureDao;
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
    private Integer port = AppUtil.configInt("algorithm.server.port");

    @Override
    public void start() throws Exception {

        imageFeature = new ImageFeature();
        try {
            URL url = getClass().getClassLoader().getResource(Constants.LESION);
            LOGGER.debug("Initialize lesion type from path : {}", url);
            ObjectMapper mapper = new ObjectMapper();
            lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        FeatureDao featureDao = new FeatureDao(vertx);
        RandomForest randomforest = new RandomForest(50, 4);
        featureDao.fetchFeatureSamples(samples -> {
            if (samples == null){
                throw new RuntimeException("cannot get samples!");
            }
            randomforest.createForest(samples);
            LOGGER.info("RandomForest Algorithm finished!");
        });
        /*List<Double[]> data = new ArrayList<>(300);
        String filePath = "feature";
        try {
            String encoding="UTF-8";
            InputStreamReader read = new InputStreamReader(
                    LesionRecognitionServer.class.getClassLoader().getResourceAsStream(filePath),encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                String[] line = lineTxt.split(",");
                Double[] d = new Double[line.length];
                for (int i = 0; i < 27; i++) {
                    d[i] = Double.valueOf(line[i]);
                }
                data.add(d);
            }
            read.close();
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        RandomForest randomforest = new RandomForest(50, 5);
        randomforest.createForest(data);*/


        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/predict").handler(ctx -> {
            String image = ctx.request().getParam("image");
            int x1 = Integer.valueOf(ctx.request().getParam("x1"));
            int y1 = Integer.valueOf(ctx.request().getParam("y1"));
            int x2 = Integer.valueOf(ctx.request().getParam("x2"));
            int y2 = Integer.valueOf(ctx.request().getParam("y2"));
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");
            try {
                int type = randomforest.predictType(imageFeature.getFeature(image,x1,y1,x2,y2));
                response.end(lesion.getString(String.valueOf(type)));
            } catch (IOException e) {
                e.printStackTrace();
                response.setStatusCode(500).end();
            }
        });

        /*server.requestHandler(request -> {

            String image = request.getParam("image");
            HttpServerResponse response = request.response();
            response.putHeader("content-type", "text/plain");
            try {
                int type = randomforest.predictType(imageFeature.getFeature(image));
                response.end(lesion[type]);
            } catch (IOException e) {
                e.printStackTrace();
                response.setStatusCode(500).end();
            }
        });*/
        server.requestHandler(router::accept).listen(8081);
        LOGGER.info("LesionRecognitionServer started!");
    }
}
