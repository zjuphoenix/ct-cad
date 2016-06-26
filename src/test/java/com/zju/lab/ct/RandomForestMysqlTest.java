package com.zju.lab.ct;

import com.zju.lab.ct.algorithm.feature.ImageFeature;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import com.zju.lab.ct.algorithm.randomforest.RandomForestDecorator;
import com.zju.lab.ct.mapper.FeatureMapper;
import com.zju.lab.ct.model.Feature;
import com.zju.lab.ct.utils.AppUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wuhaitao
 * @date 2016/5/24 16:15
 */
public class RandomForestMysqlTest {
    //@Test
    public void test() throws Exception {
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
        SqlSession session= sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        List<Feature> features = featureMapper.fetchAllLiverGlobalFeatures();
        List<Double[]> samples = new ArrayList<Double[]>(features.size());
        features.forEach(feature -> {
            samples.add(feature.featureVector());
        });
        RandomForest randomforest = new RandomForest(50, 2);
        randomforest.createForest(samples);
        //实例化ObjectOutputStream对象
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/GlobalFeatureRecognition2"));
        //将对象写入文件
        oos.writeObject(randomforest);
        oos.flush();
        oos.close();

        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition2"));

        //读取对象people,反序列化
        RandomForest p = (RandomForest) ois.readObject();
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
        System.out.println("RandomForest Accurancy:"+accurancy);
    }

    //@Test
    public void test2() throws IOException, ClassNotFoundException {
        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/GlobalFeatureRecognition2"));
        //读取对象people,反序列化
        RandomForest randomforest_Global = (RandomForest) ois.readObject();
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
        System.out.println("global feature recognition:"+type);
    }

    @Test
    public void test3() throws Exception {
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
        SqlSession session= sqlSessionFactory.openSession();
        FeatureMapper featureMapper = session.getMapper(FeatureMapper.class);
        List<Feature> features = featureMapper.fetchAllLiverFeatures();
        List<Double[]> samples = new ArrayList<Double[]>(features.size());
        features.forEach(feature -> {
            samples.add(feature.featureVector());
        });
        RandomForest randomforest = new RandomForest(50, 4);
        randomforest.createForest(samples);
        //实例化ObjectOutputStream对象
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("conf/RandomForest2"));
        //将对象写入文件
        oos.writeObject(randomforest);
        oos.flush();
        oos.close();

        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("conf/RandomForest2"));

        //读取对象people,反序列化
        RandomForest p = (RandomForest) ois.readObject();
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
        System.out.println("RandomForest Accurancy:"+accurancy);
    }
}
