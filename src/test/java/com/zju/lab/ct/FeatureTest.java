package com.zju.lab.ct;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.zju.lab.ct.algorithm.feature.Feature;
import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.algorithm.segmentation.Point;
import com.zju.lab.ct.mapper.FeatureMapper;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.DataStructureUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import segmentation.Segmentation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
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

    //@Test
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

    //@Test
    public void test3() throws MWException, IOException {
        Segmentation segmentation = new Segmentation();
        File rootDir = new File("E:/graduation/data");
        File[] dirs = rootDir.listFiles();
        List<Point> seeds = new ArrayList<>(2);
        seeds.add(new Point(150,250));
        seeds.add(new Point(100,250));
        Object[] objects = null;
        int[][] matrix = null;
        File newFile=null;
        for (File dir:dirs){
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return !name.endsWith("seg.jpg");
                }
            });
            for (File file:files){
                String fileName = file.getAbsolutePath();
                for (Point seed : seeds){
                    objects = segmentation.getLiverMask(1,fileName,seed.x,seed.y);
                    MWNumericArray res = (MWNumericArray)objects[0];
                    matrix = (int[][])res.toIntArray();
                    if (!DataStructureUtil.checkAllZero(matrix)){
                        break;
                    }
                }
                int height = matrix.length;
                int width = matrix[0].length;
                BufferedImage bi = ImageIO.read(file);
                for(int i= 0 ; i < height ; i++){
                    for(int j = 0 ; j < width; j++){
                        int gray = matrix[j][i];
                        if (gray==0) {
                            bi.setRGB(i, j, 0);
                        }
                    }
                }
                fileName = fileName.replace(".jpg", "javaseg.jpg");
                newFile = new File(fileName);
                if (newFile.exists()){
                    newFile.delete();
                }
                newFile.createNewFile();
                ImageIO.write(bi, "bmp", newFile);
            }
        }
    }

    public void forTransfer(String file1, String file2) throws Exception{
        int length=2097152;
        File f1 = new File(file1);
        File f2 = new File(file2);
        f2.createNewFile();
        FileInputStream in=new FileInputStream(f1);
        FileOutputStream out=new FileOutputStream(f2);
        FileChannel inC=in.getChannel();
        FileChannel outC=out.getChannel();
        while(true){
            if(inC.position()==inC.size()){
                inC.close();
                outC.close();
                break;
            }
            if((inC.size()-inC.position())<20971520)
                length=(int)(inC.size()-inC.position());
            else
                length=20971520;
            inC.transferTo(inC.position(),length,outC);
            inC.position(inC.position()+length);
        }
    }
    //@Test
    public void test4() throws Exception {
        String[] normal = new String[]{
                "E:/graduation/data/Zhang_Lian_Mu",
                "E:/graduation/data/Chen_Xiao_Bo",
                "E:/graduation/data/Zhou_Guo_Ling",
                "E:/graduation/data/Zhao_Mei"
        };
        String[] cancer = new String[]{
                "E:/graduation/data/Hu_Yao_Zhen",
                "E:/graduation/data/Lu_Sheng_Gao",
                "E:/graduation/data/Shi_Quan_Ping",
                "E:/graduation/data/Wu_Yong_Hang",
                "E:/graduation/data/Xiong_Shi_Wen",
                /*"E:/graduation/data/Xu_Song_Yin",*/
                /*"E:/graduation/data/Yu_A_Xian",*/
                /*"E:/graduation/data/Yu_Miao_Fu",*/
                "E:/graduation/data/Zhou_Jun",
                "E:/graduation/data/Zhu_Jian_Guo",
                "E:/graduation/data/Yuan_Chao_Ming",
                "E:/graduation/data/Pan_Fu_Tang"
        };
        int i = 0;
        String normalDir = "E:/graduation/javatrain/normal/";
        String cancerDir = "E:/graduation/javatrain/cancer/";
        for (String f : normal){
            File dir = new File(f);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("javaseg.jpg");
                }
            });
            for (File file : files){
                forTransfer(file.getAbsolutePath(), normalDir+i+".jpg");
                i++;
            }
        }

        i = 0;
        for (String f : cancer){
            File dir = new File(f);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("javaseg.jpg");
                }
            });
            for (File file : files){
                forTransfer(file.getAbsolutePath(), cancerDir+i+".jpg");
                i++;
            }
        }
    }

    @Test
    public void test5() throws Exception {
        File normal = new File("E:/graduation/javatrain/normal");
        File cancer = new File("E:/graduation/javatrain/cancer");
        File[] normalFiles = normal.listFiles();
        File[] cancerFiles = cancer.listFiles();
        ImageFeature imageFeature = new ImageFeature();
        SqlSessionFactory sqlSessionFactory;
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
        SqlSession session= sqlSessionFactory.openSession(false);
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        com.zju.lab.ct.model.Feature f = null;
        for (File file:normalFiles){
            double[] feature = imageFeature.getFeature(file.getAbsolutePath(),0,0,511,511);
            feature[26] = 1;
            f = new com.zju.lab.ct.model.Feature(feature, 1);
            featureMapper.addLiverGlobalFeature(f);
        }
        for (File file:cancerFiles){
            double[] feature = imageFeature.getFeature(file.getAbsolutePath(),0,0,511,511);
            feature[26] = 2;
            f = new com.zju.lab.ct.model.Feature(feature, 2);
            featureMapper.addLiverGlobalFeature(f);
        }
        session.commit();

    }
}
