package com.zju.lab.ct;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.zju.lab.ct.utils.AppUtil;
import org.junit.Test;
import segmentation.Segmentation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/4/14.
 */
public class OpneCVTest {
    //@Test
    public void test() throws MWException, IOException {
        Segmentation segmentation = new Segmentation();
        String fileName = "E:/graduation/data/Chen_Xiao_Bo/IMG-0001-00009.jpg";
        Object[] objects = segmentation.getLiverRegion(1,fileName,150,250);
        MWNumericArray res = (MWNumericArray)objects[0];
        int[][] matrix = (int[][])res.toIntArray();
        int height = 512;
        int width = 512;
        File file = new File(fileName);
        BufferedImage bi = ImageIO.read(file);
        for(int i= 0 ; i < height ; i++){
            for(int j = 0 ; j < width; j++){
                int gray = matrix[j][i];
                if (gray==0) {
                    bi.setRGB(i, j, 0);
                }
            }
        }
        File newFile = new File(AppUtil.getSegmentationDir()+File.separator+"segmentation.jpg");
        if (newFile.exists()){
            newFile.delete();
        }
        newFile.createNewFile();
        ImageIO.write(bi, "jpg", newFile);
        System.out.println();
    }
}
