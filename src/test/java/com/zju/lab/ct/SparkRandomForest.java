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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhaitao
 * @date 2016/5/8 12:47
 */
public class SparkRandomForest {
    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("RandomForest");
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
        //rdd.rdd().sample(true,1.0,9).flatMap()
    }
}
