package com.zju.lab.ct;

import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/24.
 */
public class RandomForestTest {
    private static Logger LOGGER = LoggerFactory.getLogger(RandomForestTest.class);

    //@Test
    public void test(){
        Connection c = null;
        Statement stmt = null;
        RandomForest randomforest = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from feature");
            List<Double[]> samples = new ArrayList<Double[]>(rs.getRow());
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
                samples.add(d);
            }
            rs.close();
            stmt.close();
            c.close();
            randomforest = new RandomForest(50, 4);
            randomforest.createForest(samples);
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        try {
            //实例化ObjectOutputStream对象
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest"));
            //将对象写入文件
            oos.writeObject(randomforest);
            oos.flush();
            oos.close();

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest"));

            try {
                //读取对象people,反序列化
                RandomForest p = (RandomForest) ois.readObject();
                LOGGER.info("RandomForest:"+p.toString());
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test2() {
        Connection c = null;
        Statement stmt = null;
        List<Double[]> samples = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from feature");
            samples = new ArrayList<Double[]>(rs.getRow());
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
                samples.add(d);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        try {

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest"));

            try {
                //读取对象people,反序列化
                RandomForest randomForest = (RandomForest) ois.readObject();
                double[] data = new double[samples.get(0).length];
                int sum = samples.size();
                int right = 0;
                for (Double[] sample : samples){
                    for (int i=0;i<sample.length;i++){
                        data[i] = sample[i];
                    }
                    if (randomForest.predictType(data) == (int)data[26]){
                        right++;
                    }
                }
                double accurancy = right*1.0/sum;
                LOGGER.info("RandomForest Accurancy:"+accurancy);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
