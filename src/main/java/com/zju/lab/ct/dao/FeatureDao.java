package com.zju.lab.ct.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.mapper.FeatureMapper;
import com.zju.lab.ct.model.Feature;
import com.zju.lab.ct.utils.Constants;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuhaitao on 2016/3/12.
 */
@HandlerDao
public class FeatureDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTImageDao.class);

    private Map<String,Integer> lesionType;
    private Map<String,Integer> lungType;
    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public FeatureDao(SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
        try {
            URL url = getClass().getClassLoader().getResource(Constants.LESION);
            LOGGER.debug("Initialize lesion type from path : {}", url);
            ObjectMapper mapper = new ObjectMapper();
            JsonObject lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
            lesionType = new HashMap<>(5);
            for (int i = 1; i <= 5; i++) {
                lesionType.put(lesion.getString(String.valueOf(i)),i);
            }

            url = getClass().getClassLoader().getResource(Constants.LUNG);
            LOGGER.debug("Initialize lung type from path : {}", url);
            mapper = new ObjectMapper();
            lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
            lungType = new HashMap<>(3);
            for (int i = 1; i <= 3; i++) {
                lungType.put(lesion.getString(String.valueOf(i)),i);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 添加肝脏局部病变特征数据到数据库
     * @param feature
     * @param label
     * @param handler
     */
    public void addLiverFeature(double[] feature, String label, Handler<String> handler){
        SqlSession session = sqlSessionFactory.openSession(false);
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        Feature feature1 = new Feature(feature, lesionType.get(label));
        try {
            featureMapper.addLiverFeature(feature1);
            session.commit();
            handler.handle("success");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            handler.handle(e.getMessage());
        }
    }

    /**
     * 添加肺部病变特征数据到数据库
     * @param feature
     * @param label
     * @param handler
     */
    public void addLungFeature(double[] feature, String label, Handler<String> handler){
        SqlSession session = sqlSessionFactory.openSession(false);
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        Feature feature1 = new Feature(feature, lungType.get(label));
        try {
            featureMapper.addLungFeature(feature1);
            session.commit();
            handler.handle("success");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            handler.handle(e.getMessage());
        }
    }

    /**
     * 从肝脏病变特征数据表feature中拉取所有数据，用于算法模型训练
     * @param samplesHandler
     */
    public void fetchLiverFeatureSamples(Handler<List<Double[]>> samplesHandler){
        SqlSession session = sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        try {
            List<Feature> features = featureMapper.fetchAllLiverFeatures();
            List<Double[]> samples = new ArrayList<>(features.size());
            features.forEach(feature -> {
                Double[] sample = feature.featureVector();
                samples.add(sample);
            });
            /*List<Double[]> samples = features.stream().flatMap(feature -> {
                Double[] sample = feature.featureVector();
                return Stream.of(sample);
            }).collect(Collectors.toList());*/
            samplesHandler.handle(samples);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            samplesHandler.handle(null);
        }
    }

    /**
     * 从肺部病变特征数据表feature中拉取所有数据，用于算法模型训练
     * @param samplesHandler
     */
    public void fetchLungFeatureSamples(Handler<List<Double[]>> samplesHandler){
        SqlSession session = sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        try {
            List<Feature> features = featureMapper.fetchAllLungFeatures();
            List<Double[]> samples = new ArrayList<>(features.size());
            features.forEach(feature -> {
                Double[] sample = feature.featureVector();
                samples.add(sample);
            });
            /*List<Double[]> samples = features.stream().flatMap(feature -> {
                Double[] sample = feature.featureVector();
                return Stream.of(sample);
            }).collect(Collectors.toList());*/
            samplesHandler.handle(samples);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            samplesHandler.handle(null);
        }
    }

    /**
     * 从肝脏全局特征数据表feature中拉取所有数据，用于算法模型训练
     * @param samplesHandler
     */
    public void fetchLiverGlobalFeatureSamples(Handler<List<Double[]>> samplesHandler){
        SqlSession session = sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        try {
            List<Feature> features = featureMapper.fetchAllLiverGlobalFeatures();
            List<Double[]> samples = new ArrayList<>(features.size());
            features.forEach(feature -> {
                Double[] sample = feature.featureVector();
                samples.add(sample);
            });
            samplesHandler.handle(samples);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            samplesHandler.handle(null);
        }
    }
}
