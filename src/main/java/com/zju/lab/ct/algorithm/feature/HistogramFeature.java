package com.zju.lab.ct.algorithm.feature;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/21.
 */
public class HistogramFeature implements Feature{

    @Override
    public double[] getFeature(String image, int x1, int y1, int x2, int y2) throws IOException {
        File file = new File(image);
        BufferedImage bi = ImageIO.read(file);
        return getFeature(bi, x1, y1, x2, y2);
    }

    @Override
    public double[] getFeature(BufferedImage image, int x1, int y1, int x2, int y2) throws IOException {
        double sgray[] = new double[256];
        for(int i=0; i<256; i++) {
            sgray[i] = 0;
        }
        int width = x2-x1+1;
        int height = y2-y1+1;
        int size = width * height;
        int valid = 0;

        for(int i=x1; i<=x2; i++) {
            for(int j=y1; j<=y2; j++) {
                int rgb = image.getRGB(i, j);

                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int gray = (int)(r * 0.3 + g * 0.59 + b * 0.11);    //计算灰度值
                if (gray != 0) {
                    sgray[gray]++;
                    valid++;
                }
            }
        }

        for(int gray=0; gray<256; gray++) {
            sgray[gray] /= valid;
            if (sgray[gray] < Double.MIN_VALUE){
                sgray[gray] = Double.MIN_VALUE;
            }
        }
        double mean = 0,variance = 0,skewness = 0,kurtosis = 0,energy = 0,entropy = 0;
        for(int gray=0; gray<256; gray++) {
            mean += sgray[gray] * gray;
        }
        double term1 = 0, term3 = 0;
        for(int gray=0; gray<256; gray++) {
            variance += sgray[gray] * Math.pow(gray + 1 - mean, 2);
            energy += sgray[gray] * sgray[gray];
            entropy -= sgray[gray] * Math.log(sgray[gray]);
            term1 += sgray[gray] * Math.pow(gray + 1 - mean, 3);
            term3 += sgray[gray] * Math.pow(gray + 1 - mean, 4);
        }
        double term2 = Math.sqrt(variance);
        skewness = Math.pow(term2, -3) * term1;
        kurtosis = Math.pow(term2, -4) * term3;
        return new double[]{mean,variance,skewness,kurtosis,energy,entropy};
    }
}
