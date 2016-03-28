package com.zju.lab.ct.verticle;

import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.dao.FeatureDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.ConfigUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

/**
 * Created by wuhaitao on 2016/3/27.
 */
public class LesionRecognitionVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(LesionRecognitionVerticle.class);

    @Override
    public void start() throws Exception {

        /*lesion recognition*/
        LOGGER.info("lesion recognition process handler......");
        ImageFeature imageFeature = new ImageFeature();
        RandomForest randomforest_Liver = AppUtil.getRandomForestModel();
        JsonObject liverLesion = ConfigUtil.getLiverLesion();
        RandomForest randomforest_Lung = AppUtil.getLungRandomForestModel();
        JsonObject lungLesion = ConfigUtil.getLungLesion();
        EventBus eventBus = vertx.eventBus();
        /*病变识别消息订阅*/
        MessageConsumer<String> consumer = eventBus.consumer(EventBusMessage.LESION_RECOGNITION);
        consumer.handler(message -> {
            LOGGER.debug("receive recognition message:"+message.body());
            JSONObject params = new JSONObject(message.body());
            String image = params.getString("image");
            int x1 = params.getInt("x1");
            int y1 = params.getInt("y1");
            int x2 = params.getInt("x2");
            int y2 = params.getInt("y2");
            String cttype = params.getString("type");
            int type = 0;
            try {
                if ("肝脏".equals(cttype)){
                    type = randomforest_Liver.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    message.reply(liverLesion.getString(String.valueOf(type)));
                }
                else if("肺部".equals(cttype)){
                    type = randomforest_Lung.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    message.reply(lungLesion.getString(String.valueOf(type)));
                }
                else{
                    message.fail(HttpCode.BAD_REQUEST.getCode(),"ct type invalid!");
                }
            } catch (IOException e) {
                message.reply(e.getMessage());
                LOGGER.error(e.getMessage(), e);
            }
        });
        /*肝脏病变识别算法训练消息订阅*/
        eventBus.consumer(EventBusMessage.LIVER_ALGORITHM_MODEL_GENERATE).handler(message -> {
            LOGGER.info("liver algorithm generate message");
            FeatureDao featureDao = null;
            try {
                featureDao = new FeatureDao(vertx);
                RandomForest randomforest = new RandomForest(50, 4);
                featureDao.fetchLiverFeatureSamples(samples -> {
                    if (samples == null){
                        LOGGER.error("cannot get liver samples!");
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), "cannot get liver samples!");
                    }
                    else {
                        randomforest.createForest(samples);
                        LOGGER.info("Liver RandomForest Algorithm finished!");
                        //实例化ObjectOutputStream对象
                        ObjectOutputStream oos = null;
                        try {
                            File file = new File("conf/RandomForest");
                            file.deleteOnExit();
                            oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest"));
                            //将对象写入文件
                            oos.writeObject(randomforest);
                            oos.flush();
                            oos.close();
                            message.reply("liver lesion recognition model create success!");
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                        }
                    }
                });
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
                message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
            }
        });
        /*肺部病变识别算法训练消息订阅*/
        eventBus.consumer(EventBusMessage.LUNG_ALGORITHM_MODEL_GENERATE).handler(message -> {
            LOGGER.info("lung algorithm generate message");
            FeatureDao featureDao = null;
            try {
                featureDao = new FeatureDao(vertx);
                RandomForest randomforest = new RandomForest(50, 4);
                featureDao.fetchLungFeatureSamples(samples -> {
                    if (samples == null){
                        LOGGER.error("cannot get lung samples!");
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), "cannot get lung samples!");
                    }
                    else {
                        randomforest.createForest(samples);
                        LOGGER.info("Lung RandomForest Algorithm finished!");
                        //实例化ObjectOutputStream对象
                        ObjectOutputStream oos = null;
                        try {
                            File file = new File("conf/RandomForest_Lung");
                            file.deleteOnExit();
                            oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest_Lung"));
                            //将对象写入文件
                            oos.writeObject(randomforest);
                            oos.flush();
                            oos.close();
                            message.reply("lung lesion recognition model create success!");
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                        }
                    }
                });
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
                message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
            }
        });
    }
}
