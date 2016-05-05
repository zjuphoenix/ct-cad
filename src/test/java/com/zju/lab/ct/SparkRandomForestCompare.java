package com.zju.lab.ct;

import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.mapper.FeatureMapper;
import com.zju.lab.ct.model.Feature;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.configuration.Strategy;
import org.apache.spark.mllib.tree.model.RandomForestModel;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhaitao
 * @date 2016/5/5 10:34
 */
public class SparkRandomForestCompare {
    public static void main(String[] args) throws Exception {
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
        SqlSession session= sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        List<Feature> features = featureMapper.fetchAllLiverGlobalFeatures();
        List<Double[]> samples = new ArrayList<>(features.size());
        features.forEach(feature -> {
            Double[] sample = feature.featureVector();
            samples.add(sample);
        });
        RandomForest randomForest = new RandomForest(5, 2);
        long start = System.currentTimeMillis();
        randomForest.createForest(samples);
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("local algorithm run time:"+time);

        SparkConf sparkConf = new SparkConf().setMaster("spark://10.13.81.185:7077").setAppName("RandomForest").set("spark.driver.host", "10.110.89.56") .set("spark.driver.port", "50128").setExecutorEnv("spark.executor.memory", "6000m");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        List<LabeledPoint> labeledPoints = new ArrayList<>(features.size());
        features.forEach(feature -> {
            Vector vector = Vectors.dense(feature.featureSparkVector());
            LabeledPoint labeledPoint = new LabeledPoint(feature.getLabel()-1, vector);
            labeledPoints.add(labeledPoint);
        });
        JavaRDD<LabeledPoint> rdd = jsc.parallelize(labeledPoints);
        Strategy strategy = Strategy.defaultStrategy("Classification");
        strategy.setNumClasses(2);
        int treeNum = 5;
        String featureSubsetStrategy = "auto";
        int seed = 12345;
        start = System.currentTimeMillis();
        RandomForestModel model = org.apache.spark.mllib.tree.RandomForest.trainClassifier(rdd.rdd(),strategy,treeNum,featureSubsetStrategy,seed);
        end = System.currentTimeMillis();
        time = end - start;
        System.out.println("spark algorithm run time:"+time);
        jsc.stop();
    }
}
