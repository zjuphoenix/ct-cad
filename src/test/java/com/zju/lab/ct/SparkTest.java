package com.zju.lab.ct;

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
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.configuration.Strategy;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhaitao
 * @date 2016/4/28 19:30
 */
public class SparkTest {
    private static Logger LOGGER = LoggerFactory.getLogger(SparkTest.class);
    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setMaster("spark://10.13.81.185:7077").setAppName("RandomForest").set("spark.driver.host", "10.110.89.56") .set("spark.driver.port", "50128")/*.setExecutorEnv("spark.executor.memory", "6000m")*/;
        //SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("RandomForest");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
        SqlSession session= sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        List<Feature> features = featureMapper.fetchAllLiverGlobalFeatures();
        List<LabeledPoint> labeledPoints = new ArrayList<>(features.size());
        features.forEach(feature -> {
            Vector vector = Vectors.dense(feature.featureSparkVector());
            LabeledPoint labeledPoint = new LabeledPoint(feature.getLabel()-1, vector);
            labeledPoints.add(labeledPoint);
        });
        JavaRDD<LabeledPoint> rdd = jsc.parallelize(labeledPoints);
        Strategy strategy = Strategy.defaultStrategy("Classification");
        strategy.setNumClasses(2);
        /*Strategy strategy = Strategy.defaultStrategy("Regression");*/
        int treeNum = 50;
        String featureSubsetStrategy = "auto";
        int seed = 12345;
        RandomForestModel model = RandomForest.trainClassifier(rdd.rdd(),strategy,treeNum,featureSubsetStrategy,seed);
        /*RandomForestModel model = RandomForest.trainRegressor(rdd.rdd(),strategy,treeNum,featureSubsetStrategy,seed);*/

        jsc.stop();
        /*try {
            //实例化ObjectOutputStream对象
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/GlobalFeatureRecognition"));
            //将对象写入文件
            oos.writeObject(model);
            oos.flush();
            oos.close();

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition"));

            try {
                //读取对象model,反序列化
                RandomForestModel p = (RandomForestModel)ois.readObject();
                DecisionTreeModel[] decisionTreeModels = p.trees();
                *//*Vector vector = Vectors.dense(127.50004806305871,340.2879698140891,0.22499862877466653,2.511056450123913,0.021876938629226055,3.99563685130568,0.004121294996333044,0.000030077291967146342,18.900421845412904,5904065.804309896,0.25887669996106333,0.044800402144906305,8.816274509803922,223.8958823529412,671.031568627451,0.0015895232602845064,127.23774509803921,7.816274509803923,18.434336217454703,4.46449718785113,2.6973072856593587,1.7345769619452298,1.2392216727684267,2.923431444893893,14615.849509803926,0.00007605560567581289,1);
                for (DecisionTreeModel decisionTreeModel:decisionTreeModels){
                    double tag = decisionTreeModel.predict(vector);
                }*//*
                LOGGER.info("RandomForest:"+p.toString());

                *//*LOGGER.info("RandomForest test:"+p.predict(vector)+1);*//*
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }*/

    }
}
