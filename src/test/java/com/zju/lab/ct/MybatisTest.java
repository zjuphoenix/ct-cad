package com.zju.lab.ct;

import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.UserMapper;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created by wuhaitao on 2016/4/21.
 */
public class MybatisTest {
    private static Logger LOGGER = LoggerFactory.getLogger(MybatisTest.class);
    private SqlSessionFactory sqlSessionFactory;
    @Before
    public void setUp() throws IOException {
        String resource = "mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
    }
    //@Test
    public void test(){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        int id = 178;
        try {
            ctMapper.deleteCTById(id);
            /*CTImage ctImage = ctMapper.queryCTById(id);
            if (ctImage != null){
                ctMapper.deleteCTById(id);
                String image = AppUtil.getUploadDir()+ File.separator+ctImage.getFile();
                File file = new File(image);
                if (file.exists()){
                    file.delete();
                }
                else{
                    LOGGER.error("ct file {} is not existing!", image);
                }
            }
            else{
                LOGGER.info("ct not found!");
            }*/
            //session.commit();
        } catch (Exception e) {
            LOGGER.error("delete ctimage by id {} failed!", id, e);
        }
    }

    //@Test
    public void test2(){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            CTImage ctImage = ctMapper.queryCTById(1);
            if (ctImage!=null){
                LOGGER.info(ctImage.toString());
            }
            CTImage ctImage1 = ctMapper.queryCTById(2);
            if (ctImage1!=null){
                LOGGER.info(ctImage1.toString());
            }
            //session.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    //@Test
    public void test3() throws Exception {
        SqlSession session= sqlSessionFactory.openSession();
        UserMapper userMapper = session.getMapper(UserMapper.class);
        List<String> perms = userMapper.queryUserPermission("admin");
        System.out.println(perms);
    }

    @Test
    public void test4() throws Exception {
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        CTImage ctImage = ctMapper.queryCTById(235);
        System.out.println(ctImage.getRecognition());
    }
}
