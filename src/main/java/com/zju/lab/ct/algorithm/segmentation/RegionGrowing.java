package com.zju.lab.ct.algorithm.segmentation;

import com.zju.lab.ct.utils.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

/**
 * Created by wuhaitao on 2016/3/29.
 */
public class RegionGrowing {
    private static Logger LOGGER = LoggerFactory.getLogger(RegionGrowing.class);

    public static String getSegmentationImage(String image, int seedX, int seedY, int threshold) throws IOException {
        File file = new File(image);
        BufferedImage bi = ImageIO.read(file);
        int height = bi.getHeight();
        int width = bi.getWidth();
        double[][] img = new double[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = bi.getRGB(i,j);
                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                //计算灰度值
                img[j][i] = r * 0.3 + g * 0.59 + b * 0.11;
            }
        }
        double seedAvg = img[seedY][seedX];
        double grayValueSum = seedAvg;//种子点灰度值加和
        int suit = 1;//种子点个数
        boolean[][] mask = new boolean[width][height];
        mask[seedX][seedY] = true;
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(seedX, seedY));
        while (stack.size() > 0) {
            Point point = stack.pop();
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (i != 0 || j != 0) {
                        int x = point.x + i;
                        int y = point.y + j;
                        if (!mask[y][x]) {
                            if (x >= 0 && x < width && y >= 0 && y < height && Math.abs(img[y][x] - seedAvg) < threshold) {
                                stack.push(new Point(x, y));
                                grayValueSum += img[y][x];
                                suit++;
                                mask[y][x] = true;
                            }
                        }
                    }
                }
            }
            seedAvg = grayValueSum / suit;
        }
        for(int i= 0 ; i < height ; i++){
            for(int j = 0 ; j < width; j++){
                if (!mask[i][j]){
                    if (i>=2&&j>=2&&i<height-2&&j<width-2){
                        int count = 0;
                        for (int ii = -2; ii <= 2; ii++) {
                            for (int jj = -2; jj <= 2 ; jj++) {
                                if (mask[i+ii][j+jj]){
                                    count++;
                                }
                            }
                        }
                        if (count<12){
                            bi.setRGB(j, i, 0);
                        }
                    }
                    else {
                        bi.setRGB(j, i, 0);
                    }
                }
            }
        }
        File newFile = new File(AppUtil.getSegmentationDir()+File.separator+"segmentation.jpg");
        if (newFile.exists()){
            newFile.delete();
        }
        newFile.createNewFile();
        ImageIO.write(bi, "jpg", newFile);
        System.out.println(newFile.exists());
        System.out.println(newFile.getAbsolutePath());
        return "segmentation.jpg";
    }
}
