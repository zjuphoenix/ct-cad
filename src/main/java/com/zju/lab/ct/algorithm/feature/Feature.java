package com.zju.lab.ct.algorithm.feature;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/21.
 */
public interface Feature {
    double[] getFeature(String image, int x1, int y1, int x2, int y2) throws IOException;
    double[] getFeature(BufferedImage image, int x1, int y1, int x2, int y2) throws IOException;
}
