package com.zju.lab.ct;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.CARTTree;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.algorithm.randomforest.RandomForestDecorator;
import com.zju.lab.ct.algorithm.randomforest.TreeNode;
import com.zju.lab.ct.utils.AppUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import segmentation.Segmentation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            List<CARTTree> trees = randomforest.getTrees();
            /*trees.forEach(tree -> {
                System.out.println("-----------------------");
                TreeNode decisionTree = tree.getDecisionTree();
                print(decisionTree);
                System.out.println("-----------------------");
            });*/
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        try {
            //实例化ObjectOutputStream对象
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest1"));
            //将对象写入文件
            oos.writeObject(randomforest);
            oos.flush();
            oos.close();

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest1"));

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

    //@Test
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
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest1"));

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

    /*肺部特征训练*/
    //@Test
    public void testLung(){
        Connection c = null;
        Statement stmt = null;
        RandomForest randomforest = null;
        List<Double[]> samples = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from lungfeature");
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
            randomforest = new RandomForest(50, 2);
            randomforest.createForest(samples);
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        try {
            //实例化ObjectOutputStream对象
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest_Lung"));
            //将对象写入文件
            oos.writeObject(randomforest);
            oos.flush();
            oos.close();

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest_Lung"));

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
                LOGGER.info("Lung RandomForest Accurancy:"+accurancy);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void print(TreeNode node){
        System.out.println(node.toString());
        if (node.left!=null) {
            System.out.println("left:");
            print(node.left);
        }
        if (node.right!=null) {
            System.out.println("right:");
            print(node.right);
        }
    }
    //@Test
    public void printTree() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest"));
        RandomForest randomForest = (RandomForest) ois.readObject();
        List<CARTTree> trees = randomForest.getTrees();
        trees.forEach(tree -> {
            System.out.println("-----------------------");
            TreeNode decisionTree = tree.getDecisionTree();
            print(decisionTree);
            System.out.println("-----------------------");
        });
    }

    //@Test
    public void testRecognition(){
        try {
            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition"));

            try {
                //读取对象people,反序列化
                RandomForest p = (RandomForest) ois.readObject();
                ImageFeature imageFeature = new ImageFeature();
                String fileName = "E:/graduation/data/Hu_Yao_Zhen/IMG-0002-00004.jpg";
                Segmentation segmentation = new Segmentation();
                Object[] objects = segmentation.getLiverMask(1,fileName,150,250);
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
                ImageIO.write(bi, "bmp", newFile);
                double[] feature = imageFeature.getFeature(bi,0,0,511,511);
                int type = p.predictType(feature);
                LOGGER.info("global feature recognition:{}",type);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (MWException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    //@Test
    public void testCompare() throws IOException {
        File file = new File(AppUtil.getSegmentationDir()+File.separator+"segmentation.jpg");
        BufferedImage bi = ImageIO.read(file);
        String fileName = "E:/graduation/data/Chen_Xiao_Bo/IMG-0001-00008seg.jpg";
        file = new File(fileName);
        BufferedImage bi2 = ImageIO.read(file);
        int[][] matrix = new int[512][512];
        System.out.println("bi:");
        for(int i= 0 ; i < 512 ; i++){
            for(int j = 0 ; j < 512; j++){
                int rgb = bi.getRGB(i, j);
                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int gray = (int)(r * 0.3 + g * 0.59 + b * 0.11);    //计算灰度值
                matrix[i][j]=gray;
            }
        }
        System.out.println();
        System.out.println("---------------------------------------------");
        int[][] matrix2 = new int[512][512];
        System.out.println("bi2:");
        for(int i= 0 ; i < 512 ; i++){
            for(int j = 0 ; j < 512; j++){
                int rgb = bi2.getRGB(i, j);
                /*应为使用getRGB(i,j)获取的该点的颜色值是ARGB，
                而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，
                即bufImg.getRGB(i, j) & 0xFFFFFF。*/
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int gray = (int)(r * 0.3 + g * 0.59 + b * 0.11);    //计算灰度值
                matrix2[i][j]=gray;
            }
        }
        int error = 0;
        for(int i= 0 ; i < 512 ; i++){
            for(int j = 0 ; j < 512; j++){
                error+=Math.abs(matrix[i][j]-matrix2[i][j]);
            }
        }
        System.out.println(error);
    }

    //@Test
    public void testAccurancy() {
        Connection c = null;
        Statement stmt = null;
        RandomForest randomforest = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from globalfeature");
            List<Double[]> samples = new ArrayList<Double[]>(rs.getRow());
            while (rs.next()) {
                Double[] d = new Double[27];
                d[0] = rs.getDouble("f1");
                d[1] = rs.getDouble("f2");
                d[2] = rs.getDouble("f3");
                d[3] = rs.getDouble("f4");
                d[4] = rs.getDouble("f5");
                d[5] = rs.getDouble("f6");
                d[6] = rs.getDouble("f7");
                d[7] = rs.getDouble("f8");
                d[8] = rs.getDouble("f9");
                d[9] = rs.getDouble("f10");
                d[10] = rs.getDouble("f11");
                d[11] = rs.getDouble("f12");
                d[12] = rs.getDouble("f13");
                d[13] = rs.getDouble("f14");
                d[14] = rs.getDouble("f15");
                d[15] = rs.getDouble("f16");
                d[16] = rs.getDouble("f17");
                d[17] = rs.getDouble("f18");
                d[18] = rs.getDouble("f19");
                d[19] = rs.getDouble("f20");
                d[20] = rs.getDouble("f21");
                d[21] = rs.getDouble("f22");
                d[22] = rs.getDouble("f23");
                d[23] = rs.getDouble("f24");
                d[24] = rs.getDouble("f25");
                d[25] = rs.getDouble("f26");
                d[26] = (double) rs.getInt("label");
                samples.add(d);
                break;
            }
            rs.close();
            stmt.close();
            c.close();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition"));
            randomforest = (RandomForest) ois.readObject();
            double[] data = new double[samples.get(0).length];
            int sum = samples.size();
            int right = 0;
            for (Double[] sample : samples){
                for (int i=0;i<sample.length;i++){
                    data[i] = sample[i];
                }
                int type = randomforest.predictType(data);
                if (type == (int)data[26]){
                    right++;
                }
            }
            double accurancy = right*1.0/sum;
            LOGGER.info("RandomForest Accurancy:"+accurancy);
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    //@Test
    public void testSegmentedRecognition(){
        try {
            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition"));

            try {
                //读取对象people,反序列化
                RandomForest p = (RandomForest) ois.readObject();
                ImageFeature imageFeature = new ImageFeature();
                String fileName = "E:/graduation/data/Zhang_Lian_Mu/IMG-0003-00023javaseg.jpg";
                File file = new File(fileName);
                BufferedImage bi = ImageIO.read(file);
                double[] feature = imageFeature.getFeature(bi,0,0,511,511);
                for (int i = 0; i < feature.length; i++) {
                    System.out.print(feature[i]);
                    System.out.print(",");
                }
                System.out.println();
                int type = p.predictType(feature);
                Map<Integer,Integer> map = p.predictResult(feature);
                System.out.println(map.toString());
                LOGGER.info("global feature recognition:{}",type);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void main() {
        Connection c = null;
        Statement stmt = null;
        RandomForest randomforest = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:db/cad");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from globalfeature");
            List<Double[]> samples = new ArrayList<Double[]>(rs.getRow());
            while (rs.next()) {
                Double[] d = new Double[27];
                d[0] = rs.getDouble("f1");
                d[1] = rs.getDouble("f2");
                d[2] = rs.getDouble("f3");
                d[3] = rs.getDouble("f4");
                d[4] = rs.getDouble("f5");
                d[5] = rs.getDouble("f6");
                d[6] = rs.getDouble("f7");
                d[7] = rs.getDouble("f8");
                d[8] = rs.getDouble("f9");
                d[9] = rs.getDouble("f10");
                d[10] = rs.getDouble("f11");
                d[11] = rs.getDouble("f12");
                d[12] = rs.getDouble("f13");
                d[13] = rs.getDouble("f14");
                d[14] = rs.getDouble("f15");
                d[15] = rs.getDouble("f16");
                d[16] = rs.getDouble("f17");
                d[17] = rs.getDouble("f18");
                d[18] = rs.getDouble("f19");
                d[19] = rs.getDouble("f20");
                d[20] = rs.getDouble("f21");
                d[21] = rs.getDouble("f22");
                d[22] = rs.getDouble("f23");
                d[23] = rs.getDouble("f24");
                d[24] = rs.getDouble("f25");
                d[25] = rs.getDouble("f26");
                d[26] = (double) rs.getInt("label");
                samples.add(d);
            }
            rs.close();
            stmt.close();
            c.close();
            randomforest = new RandomForest(50, 2);
            randomforest.createForest(samples);
        } catch (Exception e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOGGER.info("-----------------");
        try {
            //实例化ObjectOutputStream对象
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/GlobalFeatureRecognition"));
            //将对象写入文件
            oos.writeObject(randomforest);
            oos.flush();
            oos.close();

            //实例化ObjectInputStream对象
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition"));

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
    public void testSparkGlobalRandomForest() throws IOException, ClassNotFoundException {
        RandomForestDecorator randomforest_Global = AppUtil.getGlobalFeatureRecognitionModel();
        ImageFeature imageFeature = new ImageFeature();
        //String fileName = "E:/graduation/data/Zhang_Lian_Mu/IMG-0003-00028javaseg.jpg";
        String fileName = "E:/graduation/javatrain/normal/1.jpg";
        File file = new File(fileName);
        BufferedImage bi = ImageIO.read(file);
        double[] feature = imageFeature.getFeature(bi,0,0,511,511);
        for (int i = 0; i < feature.length; i++) {
            System.out.print(feature[i]);
            System.out.print(",");
        }
        System.out.println();
        int type = randomforest_Global.predictType(feature);
        Map<Integer,Integer> map = randomforest_Global.predictResult(feature);
        System.out.println(map.toString());
        LOGGER.info("global feature recognition:{}",type);
    }

}
