package com.zju.lab.ct;

import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/4/13.
 */
public class FeatureTest {
    private static Logger LOGGER = LoggerFactory.getLogger(FeatureTest.class);
    //@Test
    public void test() throws IOException {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            PreparedStatement stat = c.prepareStatement("insert into globalfeature(f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,label) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ImageFeature imageFeature = new ImageFeature();
            String normalDir = "E:/graduation/train/normal";
            String cancerDir = "E:/graduation/train/cancer";
            File dir = new File(normalDir);
            File[] files = dir.listFiles();
            for (File file : files){
                double[] feature = imageFeature.getFeature(file.getAbsolutePath(), 0, 0, 511 ,511);
                feature[26] = 1;
                for (int i = 0; i <26; i++){
                    stat.setDouble(i+1, feature[i]);
                }
                stat.setInt(27, 1);
                stat.executeUpdate();
            }
            dir = new File(cancerDir);
            files = dir.listFiles();
            for (File file : files){
                double[] feature = imageFeature.getFeature(file.getAbsolutePath(), 0, 0, 511, 511);
                feature[26] = 2;
                for (int i = 0; i <26; i++){
                    stat.setDouble(i+1, feature[i]);
                }
                stat.setInt(27, 2);
                stat.executeUpdate();
            }
            stat.close();
            c.close();
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    @Test
    public void testAlgorithm(){
        RandomForest randomforest = null;
        Connection c = null;
        Statement stmt = null;
        List<Double[]> trainSamples = null;
        List<Double[]> testSamples = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from globalfeature");
            int size = rs.getRow();
            trainSamples = new ArrayList<Double[]>(rs.getRow()/2+10);
            testSamples = new ArrayList<Double[]>(rs.getRow()/2+10);
            int flag = 0;
            while (rs.next()) {
                Double[] d = new Double[27];
                d[0] = rs.getDouble("f1");
                d[1] = rs.getDouble("f2");
                d[2] = rs.getDouble("f1");
                d[3] = rs.getDouble("f1");
                d[4] = rs.getDouble("f1");
                d[5] = rs.getDouble("f1");
                d[6] = rs.getDouble("f1");
                d[7] = rs.getDouble("f1");
                d[8] = rs.getDouble("f1");
                d[9] = rs.getDouble("f1");
                d[10] = rs.getDouble("f1");
                d[11] = rs.getDouble("f1");
                d[12] = rs.getDouble("f1");
                d[13] = rs.getDouble("f1");
                d[14] = rs.getDouble("f1");
                d[15] = rs.getDouble("f1");
                d[16] = rs.getDouble("f1");
                d[17] = rs.getDouble("f1");
                d[18] = rs.getDouble("f1");
                d[19] = rs.getDouble("f1");
                d[20] = rs.getDouble("f1");
                d[21] = rs.getDouble("f1");
                d[22] = rs.getDouble("f1");
                d[23] = rs.getDouble("f1");
                d[24] = rs.getDouble("f1");
                d[25] = rs.getDouble("f1");
                d[26] = (double) rs.getInt("label");
                if (flag%2==0) {
                    trainSamples.add(d);
                }
                else{
                    testSamples.add(d);
                }
                flag++;
            }
            rs.close();
            stmt.close();
            c.close();
            randomforest = new RandomForest(50, 2);
            randomforest.createForest(trainSamples);
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        int sum = testSamples.size();
        int right = 0;
        double[] data = new double[testSamples.get(0).length];
        for (Double[] sample : testSamples){
            for (int i=0;i<sample.length;i++){
                data[i] = sample[i];
            }
            if (randomforest.predictType(data) == (int)data[26]){
                right++;
            }
        }
        double accurancy = right*1.0/sum;
        LOGGER.info("RandomForest Accurancy:"+accurancy);
    }
}
