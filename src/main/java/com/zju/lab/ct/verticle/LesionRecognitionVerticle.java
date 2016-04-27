package com.zju.lab.ct.verticle;

import com.google.inject.Inject;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.algorithm.segmentation.Point;
import com.zju.lab.ct.algorithm.segmentation.RegionGrowing;
import com.zju.lab.ct.dao.CTImageDao;
import com.zju.lab.ct.dao.FeatureDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.ConfigUtil;
import com.zju.lab.ct.utils.DataStructureUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import segmentation.Segmentation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by wuhaitao on 2016/3/27.
 */
public class LesionRecognitionVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(LesionRecognitionVerticle.class);

    private List<Point> seeds = null;

    @Inject
    private CTImageDao ctImageDao;
    @Inject
    private FeatureDao featureDao;

    @Override
    public void start() throws Exception {
        /*lesion recognition*/
        LOGGER.info("lesion recognition process handler......");
        ImageFeature imageFeature = new ImageFeature();
        RandomForest randomforest_Liver = AppUtil.getRandomForestModel();
        JsonObject liverLesion = ConfigUtil.getLiverLesion();
        RandomForest randomforest_Lung = AppUtil.getLungRandomForestModel();
        JsonObject lungLesion = ConfigUtil.getLungLesion();

        RandomForest randomforest_Global = AppUtil.getGlobalFeatureRecognitionModel();
        seeds = new ArrayList<>(2);
        seeds.add(new Point(150,250));
        seeds.add(new Point(100,250));

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
            /*System.out.println(params.toString());*/
            Map<Integer, Integer> type = null;
            int number = 40;
            try {
                JsonObject lesion = null;
                if ("肝脏".equals(cttype)){
                    /*type = randomforest_Liver.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    message.reply(liverLesion.getString(String.valueOf(type)));*/
                    type = randomforest_Liver.predictResult(imageFeature.getFeature(image, x1, y1, x2, y2));
                    number = randomforest_Liver.getTreeNum();
                    lesion = liverLesion;
                }
                else if("肺部".equals(cttype)){
                    /*type = randomforest_Lung.predictType(imageFeature.getFeature(image, x1, y1, x2, y2));
                    message.reply(lungLesion.getString(String.valueOf(type)));*/
                    type = randomforest_Lung.predictResult(imageFeature.getFeature(image, x1, y1, x2, y2));
                    number = randomforest_Lung.getTreeNum();
                    lesion = lungLesion;
                }
                else{
                    message.fail(HttpCode.BAD_REQUEST.getCode(),"ct type invalid!");
                    return;
                }
                int treeNum = number;
                JsonObject lesionType = lesion;
                JsonArray res = new JsonArray();
                type.forEach((key, value) -> {
                    /*res.add(new JsonObject().put(lesionType.getString(String.valueOf(key.intValue())),value.doubleValue()/treeNum*100+"%"));*/
                    res.add(new JsonObject().put("type",lesionType.getString(String.valueOf(key.intValue()))).put("probability",value.doubleValue()/treeNum*100+"%"));
                });
                message.reply(res.encode());
            } catch (IOException e) {
                message.reply(e.getMessage());
                LOGGER.error(e.getMessage(), e);
            }
        });
        /*肝脏病变识别算法训练消息订阅*/
        eventBus.consumer(EventBusMessage.LIVER_ALGORITHM_MODEL_GENERATE).handler(message -> {
            LOGGER.info("liver algorithm generate message");
            RandomForest randomforest = new RandomForest(50, 4);
            featureDao.fetchLiverFeatureSamples(samples -> {
                if (samples == null){
                    LOGGER.error("cannot get liver samples!");
                    message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), "cannot get liver samples!");
                }
                else {
                    try {
                        randomforest.createForest(samples);
                        LOGGER.info("Liver RandomForest Algorithm finished!");
                        //实例化ObjectOutputStream对象
                        ObjectOutputStream oos = null;
                        try {
                            File file = new File("conf/RandomForest");
                            if (file.exists()){
                                file.delete();
                                file.createNewFile();
                            }
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
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    } catch (ExecutionException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    }

                }
            });
        });
        /*肺部病变识别算法训练消息订阅*/
        eventBus.consumer(EventBusMessage.LUNG_ALGORITHM_MODEL_GENERATE).handler(message -> {
            LOGGER.info("lung algorithm generate message");
            RandomForest randomforest = new RandomForest(50, 4);
            featureDao.fetchLungFeatureSamples(samples -> {
                if (samples == null){
                    LOGGER.error("cannot get lung samples!");
                    message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), "cannot get lung samples!");
                }
                else {
                    try {
                        randomforest.createForest(samples);
                        LOGGER.info("Lung RandomForest Algorithm finished!");
                        //实例化ObjectOutputStream对象
                        ObjectOutputStream oos = null;
                        try {
                            File file = new File("conf/RandomForest_Lung");
                            if (file.exists()){
                                file.delete();
                                file.createNewFile();
                            }
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
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    } catch (ExecutionException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    }

                }
            });
        });
        /*肝脏区域分割消息订阅*/
        eventBus.consumer(EventBusMessage.LIVER_SEGMENTATION).handler(message -> {
            LOGGER.info("receive segmentation message:"+message.body());
            JSONObject params = new JSONObject((String)message.body());
            String image = params.getString("image");
            int seedX = params.getInt("seedX");
            int seedY = params.getInt("seedY");
            JsonObject obj = new JsonObject();
            try {
                String segImg = RegionGrowing.getSegmentationImage(image,seedX,seedY,20);
                obj.put("code", HttpCode.OK.getCode());
                obj.put("image", segImg);
                message.reply(obj.encode());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                obj.put("code", HttpCode.INTERNAL_SERVER_ERROR.getCode());
                obj.put("error", e.getMessage());
                message.reply(obj.encode());
            }
        });

        /*肝脏区域全局特征识别*/
        eventBus.consumer(EventBusMessage.GLOBAL_FEATURE_RECOGNITION).handler(message -> {
            LOGGER.info("receive global feature recognition message:"+message.body());
            JSONObject params = new JSONObject((String)message.body());
            String image = AppUtil.getUploadDir() + File.separator + params.getString("file");
            File ctfile = new File(image);
            String fileName = ctfile.getAbsolutePath();
            int id = params.getInt("id");
            Segmentation segmentation = null;
            try {
                segmentation = new Segmentation();
                Object[] objects = null;
                int[][] matrix = null;
                /*遍历所有种子点，直到找到能通过该种子点分割出肝脏区域为止，当图像掩模矩阵matrix不是全零时说明已经分割出肝脏*/
                for (Point seed : seeds){
                    objects = segmentation.getLiverMask(1,fileName,seed.x,seed.y);
                    MWNumericArray res = (MWNumericArray)objects[0];
                    matrix = (int[][])res.toIntArray();
                    if (!DataStructureUtil.checkAllZero(matrix)){
                        break;
                    }
                }
                int height = matrix.length;
                int width = matrix[0].length;
                BufferedImage bi = ImageIO.read(ctfile);
                boolean flag = false;
                for(int i= 0 ; i < height ; i++){
                    for(int j = 0 ; j < width; j++){
                        int gray = matrix[j][i];
                        if (gray==0) {
                            bi.setRGB(i, j, 0);
                        }
                        else{
                            flag = true;
                        }
                    }
                }
                int type = 1;
                if (flag){
                    double[] feature = imageFeature.getFeature(bi,0,0,bi.getWidth()-1,bi.getHeight()-1);
                    type = randomforest_Global.predictType(feature);
                }
                LOGGER.info("global feature recognition:{}",type);
                ctImageDao.updateRecognition(id, type, stringResponseMsg -> {

                });
                File newFile = new File(AppUtil.getSegmentationDir()+File.separator+params.getString("file"));
                if (newFile.exists()){
                    newFile.delete();
                }
                newFile.createNewFile();
                ImageIO.write(bi, "bmp", newFile);
            } catch (MWException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        });

        /*肝脏全局特征识别算法训练消息订阅*/
        eventBus.consumer(EventBusMessage.GLOBAL_ALGORITHM_MODEL_GENERATE).handler(message -> {
            LOGGER.info("liver global algorithm generate message");
            RandomForest randomforest = new RandomForest(50, 2);
            featureDao.fetchLiverGlobalFeatureSamples(samples -> {
                if (samples == null){
                    LOGGER.error("cannot get liver global feature samples!");
                    message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), "cannot get liver global feature samples!");
                }
                else {
                    try {
                        randomforest.createForest(samples);
                        LOGGER.info("Liver Global Feature RandomForest Algorithm finished!");
                        //实例化ObjectOutputStream对象
                        ObjectOutputStream oos = null;
                        try {
                            File file = new File("conf/GlobalFeatureRecognition");
                            if (file.exists()){
                                file.delete();
                                file.createNewFile();
                            }
                            oos = new ObjectOutputStream(new FileOutputStream("conf/GlobalFeatureRecognition"));
                            //将对象写入文件
                            oos.writeObject(randomforest);
                            oos.flush();
                            oos.close();
                            message.reply("liver global feature recognition model create success!");
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    } catch (ExecutionException e) {
                        LOGGER.error(e.getMessage(), e);
                        message.fail(HttpCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                    }

                }
            });
        });
    }
}
