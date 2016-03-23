package com.zju.lab.ct.algorithm.feature;

import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/27.
 */
public class ImageFeature implements Feature{
    private HistogramFeature histogramFeature;
    private GLCMFeature glcmFeature;
    private GGCMFeature ggcmFeature;

    public ImageFeature() {
        histogramFeature = new HistogramFeature();
        glcmFeature = new GLCMFeature();
        ggcmFeature = new GGCMFeature();
    }

    @Override
    public double[] getFeature(String image, int x1, int y1, int x2, int y2) throws IOException {

        double[] feature = new double[27];
        double[] f1 = histogramFeature.getFeature(image, x1, y1, x2, y2);
        double[] f2 = glcmFeature.getFeature(image, x1, y1, x2, y2);
        double[] f3 = ggcmFeature.getFeature(image, x1, y1, x2, y2);
        int n = 0;
        for (double f : f1){
            feature[n++] = f;
        }
        for (double f : f2){
            feature[n++] = f;
        }
        for (double f : f3){
            feature[n++] = f;
        }
        return feature;
    }
}
