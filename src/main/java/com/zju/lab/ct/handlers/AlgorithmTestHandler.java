package com.zju.lab.ct.handlers;

import com.google.inject.Inject;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.FeatureDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.configuration.Strategy;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author wuhaitao
 * @date 2016/5/5 12:51
 */
@RouteHandler("/algorithm")
public class AlgorithmTestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmTestHandler.class);

    @Inject
    private FeatureDao featureDao;

    @RouteMapping(value = "/test", method = RouteMethod.GET)
    public Handler<RoutingContext> algorithmTest() {
        return ctx -> {
            int treeNum = Integer.parseInt(ctx.request().getParam("treeNum"));
            featureDao.fetchLiverGlobalFeatureSamples(samples -> {
                RandomForest randomForest = new RandomForest(treeNum, 2);
                long start = System.currentTimeMillis();
                try {
                    randomForest.createForest(samples);
                    long end = System.currentTimeMillis();
                    long time = end - start;
                    System.out.println("local algorithm run time:"+time);
                    JsonObject res = new JsonObject();
                    res.put("local", time);
                    SparkConf sparkConf = new SparkConf();
                    String appName = "AlgorithmTest";
                    if (AppUtil.configBoolean("spark.local.pattern")){
                        sparkConf.setMaster("local").setAppName(appName);
                    }
                    else{
                        sparkConf.setMaster(AppUtil.configStr("spark.master"))
                                .setAppName(appName)
                                .set("spark.driver.host", AppUtil.configStr("spark.driver.host"))
                                .set("spark.driver.port", AppUtil.configStr("spark.driver.port"));
                    }
                    JavaSparkContext jsc = new JavaSparkContext(sparkConf);
                    List<LabeledPoint> labeledPoints = new ArrayList<>(samples.size());
                    double[] d = new double[26];
                    samples.forEach(sample -> {
                        for (int i = 0; i < 26; i++) {
                            d[i] = sample[i];
                        }
                        Vector vector = Vectors.dense(d);
                        LabeledPoint labeledPoint = new LabeledPoint(sample[26]-1, vector);
                        labeledPoints.add(labeledPoint);
                    });
                    JavaRDD<LabeledPoint> rdd = jsc.parallelize(labeledPoints);
                    Strategy strategy = Strategy.defaultStrategy("Classification");
                    strategy.setNumClasses(2);
                    String featureSubsetStrategy = "auto";
                    int seed = 12345;
                    start = System.currentTimeMillis();
                    RandomForestModel model = org.apache.spark.mllib.tree.RandomForest.trainClassifier(rdd.rdd(),strategy,treeNum,featureSubsetStrategy,seed);
                    end = System.currentTimeMillis();
                    time = end - start;
                    System.out.println("spark algorithm run time:"+time);
                    res.put("spark", time);
                    ctx.response().end(res.encode());
                    jsc.stop();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(e.getMessage(), e);
                    ctx.response().setStatusCode(HttpCode.BAD_REQUEST.getCode()).end(e.getMessage());
                }
            });
        };
    }
}
