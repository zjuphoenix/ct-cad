package com.zju.lab.ct.algorithm.feature;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/2/21.
 */
public class GGCMFeature implements Feature{
    @Override
    public double[] getFeature(String image, int x1, int y1, int x2, int y2) throws IOException {
        File file = new File(image);
        BufferedImage bi = ImageIO.read(file);
        return getFeature(bi, x1, y1, x2, y2);
    }

    @Override
    public double[] getFeature(BufferedImage image, int x1, int y1, int x2, int y2) throws IOException {
        // 灰度梯度共生矩阵 H
        //归一化灰度梯度矩阵 H_basic

        //小梯度优势 T1
        // 大梯度优势 T2
        // 灰度分布的不均匀性 T3
        // 梯度分布的不均匀性 T4
        // 能量 T5
        // 灰度平均 T6
        // 梯度平均 T7
        // 灰度均方差 T8
        // 梯度均方差 T9
        // 相关 T10
        // 灰度熵 T11
        // 梯度熵 T12
        // 混合熵 T13
        // 惯性 T14
        // 逆差矩 T15

        int gray=256;
        int C = x2-x1+1;
        int R = y2-y1+1;
        int[][] img = new int[R][C];
        for(int i=x1; i<=x2; i++) {
            for(int j=y1; j<=y2; j++) {
                int rgb = image.getRGB(i, j);
                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                //计算灰度值
                img[j-y1][i-x1] = (int)(r * 0.3 + g * 0.59 + b * 0.11);
            }
        }

        //采用平方求和计算梯度矩阵
        double[][] GM=new double[R-1][C-1];
        for (int i = 0; i < R-1; i++) {
            for (int j = 0; j < C-1; j++) {
                if (img[i][j+1]!=0&&img[i][j]!=0&&img[i+1][j]!=0) {
                    GM[i][j] = Math.sqrt(Math.pow(img[i][j + 1] - img[i][j], 2) + Math.pow(img[i + 1][j] - img[i][j], 2));
                }
            }
        }

        //找出最大值最小值
        double n_min = Double.MAX_VALUE;
        double n_max = Double.MIN_VALUE;
        for (int i = 0; i < R-1; i++) {
            for (int j = 0; j < C-1; j++) {
                if (n_max < GM[i][j]){
                    n_max = GM[i][j];
                }
                if (n_min > GM[i][j]){
                    n_min = GM[i][j];
                }
            }
        }
        //把梯度图象灰度级离散化
        //设置新的灰度级为new_gray
        int new_gray=32;

        //新的梯度矩阵为new_GM
        int[][] new_GM=new int[R-1][C-1];
        double c = n_max-n_min;
        for (int i = 0; i < R-1; i++) {
            for (int j = 0; j < C-1; j++) {
                new_GM[i][j] = (int)((GM[i][j]-n_min)/c*(new_gray-1));
            }
        }

        int valid = 0;
        //计算灰度梯度共生矩阵
        //梯度矩阵比轨度矩阵维数少1，忽略灰度矩阵最外围
        int[][] H=new int[gray][new_gray];
        for (int i = 0; i < R-1; i++) {
            for (int j = 0; j < C-1; j++) {
                if(img[i][j] != 0) {
                    valid++;
                    H[img[i][j]][new_GM[i][j]] = H[img[i][j]][new_GM[i][j]] + 1;
                }
            }
        }

        //归一化灰度梯度矩阵 H_basic
        /*int total=(R-1)*(C-1);*/
        int total=valid;
        double[][] H_basic = new double[gray][new_gray];
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                H_basic[i][j] = H[i][j]*1.0/total;
            }
        }

        //小梯度优势 T1
        double[] TT = new double[new_gray];
        for (int i = 0; i < new_gray; i++) {
            for (int j = 0; j < gray; j++) {
                TT[i]+=H[j][i];
            }
        }
        double T1 = 0;
        for (int i = 1; i <= new_gray; i++) {
            T1=T1+TT[i-1]/(i*i);
        }
        T1=T1/total;


        //计算大梯度优势 T2
        double T2 = 0;
        for (int i = 1; i <= new_gray; i++) {
            T2=T2+TT[i-1]*i;
        }
        T2=T2/total;


        //计算灰度分布的不均匀性 T3
        double T3 = 0;
        double[] TT1 = new double[gray];
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                TT1[i]+=H[i][j];
            }
        }
        for (int j = 0; j < gray; j++) {
            T3=T3+TT1[j]*TT1[j];
        }
        T3=T3/total;


        //计算梯度分布的不均匀性 T4
        double T4 = 0;
        for (int j = 0; j < new_gray; j++) {
            T4=T4+TT[j]*TT[j];
        }
        T4=T4/total;


        //计算能量 T5
        double T5 = 0;
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                T5=T5+H_basic[i][j]*H_basic[i][j];
            }
        }


        //计算灰度平均 T6
        double[] TT2 = new double[gray];
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                TT2[i]+=H_basic[i][j];
            }
        }
        double T6 = 0;
        for (int j = 0; j < gray; j++) {
            T6=T6+j*TT2[j];
        }


        //计算梯度平均 T7
        double T7 = 0;
        double[] TT3 = new double[new_gray];
        for (int i = 0; i < new_gray; i++) {
            for (int j = 0; j < gray; j++) {
                TT3[i]+=H_basic[j][i];
            }
        }
        for (int j = 0; j < new_gray; j++) {
            T7=T7+j*TT3[j];
        }


        //计算灰度均方差 T8
        double T8 = 0;
        for (int j = 0; j < gray; j++) {
            T8=T8+(j-T6)*(j-T6)*TT2[j];
        }
        T8=Math.sqrt(T8);


        //计算梯度均方差 T9
        double T9 = 0;
        for (int j = 0; j < new_gray; j++) {
            T9=T9+(j-T7)*(j-T7)*TT3[j];
        }
        T9=Math.sqrt(T9);


        // 计算相关 T10
        double T10 = 0;
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                T10=T10+(i-T6)*(j-T7)*H_basic[i][j];
            }
        }


        //计算灰度熵 T11
        double T11 = 0;
        for (int j = 0; j < gray; j++) {
            T11=T11+TT2[j]*Math.log10(TT2[j]+Double.MIN_VALUE);
        }
        T11=T11*(-1);


        //计算梯度熵 T12
        double T12 = 0;
        for (int j = 0; j < new_gray; j++) {
            T12=T12+TT3[j]*Math.log10(TT3[j] + Double.MIN_VALUE);
        }
        T12=T12*(-1);


        //计算混合熵 T13
        double T13 = 0;
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                T13=T13+H_basic[i][j]*Math.log10(H_basic[i][j] + Double.MIN_VALUE);
            }
        }
        T13=T13*(-1);


        //计算惯性 T14
        double T14 = 0;
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                T14=T14+(i-j)*(i-j)*H_basic[i][j];
            }
        }


        //计算逆差矩 T15
        double T15 = 0;
        for (int i = 0; i < gray; i++) {
            for (int j = 0; j < new_gray; j++) {
                T15=T15+H_basic[i][j]/(1+(i-j)*(i-j));
            }
        }

        return new double[]{T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15};
    }
}
