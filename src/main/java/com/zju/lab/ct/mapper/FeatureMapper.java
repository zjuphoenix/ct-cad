package com.zju.lab.ct.mapper;

import com.zju.lab.ct.model.Feature;

import java.util.List;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public interface FeatureMapper {
    List<Feature> fetchAllLiverFeatures() throws Exception;
    List<Feature> fetchAllLungFeatures() throws Exception;
    void addLiverFeature(Feature feature) throws Exception;
    void addLiverGlobalFeature(Feature feature) throws Exception;
    void addLungFeature(Feature feature) throws Exception;
    List<Feature> fetchAllLiverGlobalFeatures() throws Exception;
}
