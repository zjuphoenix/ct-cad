package com.zju.lab.ct.algorithm.feature;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/21.
 */
public class GLCMFeature implements Feature{
    @Override
    public double[] getFeature(String image, int x1, int y1, int x2, int y2) throws IOException {
        //灰度共生矩阵为p2;
        //f1:二阶矩
        //f2:相关度
        //f3:墒
        //f4:对比度
        //f5:逆差矩
        //f6:和方差
        /*URL url = GLCMFeature.class.getClassLoader().getResource("webroot/" + image);
        File file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));*/
        File file = new File(image);
        BufferedImage bi = ImageIO.read(file);
        int C = x2-x1+1;
        int R = y2-y1+1;
        int size = R * C;
        double[][] p1= new double[256][256];
        int valid = 0;
        int[][] img = new int[R][C];
        for(int i=x1; i<=x2; i++) {
            for(int j=y1; j<=y2; j++) {
                int rgb = bi.getRGB(i, j);
                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int gray = (int)(r * 0.3 + g * 0.59 + b * 0.11);    //计算灰度值
                img[j-y1][i-x1] = gray;
            }
        }
        for (int M = 0; M < R; M++) {
            for (int N = 0; N < C-1; N++) {
                if (img[M][N]!=0 && img[M][N+1]!=0){
                    valid++;
                    p1[img[M][N]][img[M][N+1]] = p1[img[M][N]][img[M][N+1]] + 1;
                    p1[img[M][N+1]][img[M][N]] = p1[img[M][N+1]][img[M][N]] + 1;
                }
            }
        }
        //归一化
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                p1[i][j] = p1[i][j]*1.0/valid;
            }
        }

        //计算二阶矩 f1
        double f1 = 0.0;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                f1 += p1[i][j]*p1[i][j];
            }
        }

        //计算相关度 f2
        double m1=0,m2=0,v1=0,v2=0;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                m1 = m1 + i*p1[i][j];
                m2 = m2 + j*p1[i][j];
            }
        }
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                v1 = v1 + (i-m1)*(i-m1)*p1[i][j];
                v2 = v2 + (j-m2)*(j-m2)*p1[i][j];
            }
        }
        double f2 = 0;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                f2 = f2 + i*j*p1[i][j];
            }
        }
        f2 = f2/((v1+Double.MIN_VALUE)*(v2+Double.MIN_VALUE));

        //计算墒 f3
        double f3 = 0.0;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                if (p1[i][j] < Double.MIN_VALUE){
                    f3 -= p1[i][j]*Math.log10(Double.MIN_VALUE)/Math.log10(2);
                }
                else{
                    f3 -= p1[i][j]*Math.log10(p1[i][j])/Math.log10(2);
                }

            }
        }

        //计算对比度 f4
        double f4 = 0;
        double k_f4 = 0;
        for (int k = 0; k < 256; k++) {
            for (int i = 0; i < 256; i++) {
                if (i+k<=255){
                    k_f4 += p1[i][i+k];
                }
            }
            f4 += k_f4*k*k;
        }

        //计算逆差矩 f5
        double f5 = 0;
        for (int k = 0; k < 256; k++) {
            for (int j = 0; j < 256; j++) {
                f5 += p1[k][j]/(1+(j-k)*(j-k));
            }
        }

        //计算和方差 f6
        double f6 = 0;
        for (int k = 1; k < 512; k+=2) {
            for (int i = 0; i < k; i++) {
                int j = k - i;
                if (j<256&&i<256){
                    f6 += ((i-j)*(i-j)*p1[i][j]);
                }
            }
        }

        return new double[]{f1,f2,f3,f4,f5};
    }
}
