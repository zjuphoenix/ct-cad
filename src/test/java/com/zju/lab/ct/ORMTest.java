package com.zju.lab.ct;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.orm.ORMConfig;
import com.zju.lab.ct.orm.container.MapperScaner;
import com.zju.lab.ct.orm.dao.TestMapper;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by wuhaitao on 2016/3/25.
 */
public class ORMTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ORMTest.class);

    private Reflections daoReflections;
    private Set<Class<?>> daos;

    @Before
    public void setUp(){
        daoReflections = new Reflections(ORMConfig.mapperPath);
        daos = daoReflections.getTypesAnnotatedWith(HandlerDao.class);
    }
    //@Test
    public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Class<?> dao : daos) {
            LOGGER.info("Scan dao {}", dao.getSimpleName());
            Annotation[] annotations = dao.getAnnotations();
            LOGGER.info("annotation num:"+annotations.length);
            LOGGER.info(annotations[0].annotationType().getSimpleName());
            Method[] methods = dao.getDeclaredMethods();
            LOGGER.info("method num:"+methods.length);
            Annotation methodAnnotation= methods[0].getAnnotations()[0];
            Method m = methodAnnotation.getClass().getDeclaredMethod("value", null);
            String value = (String) m.invoke(methodAnnotation, null);
            LOGGER.info(value);
        }
    }

    public static void main(String[] args) {
        MapperScaner mapperScaner = new MapperScaner();
        mapperScaner.constructDaos();
        Object obj = mapperScaner.getMapper(TestMapper.class);
        TestMapper testMapper = (TestMapper)obj;
        testMapper.test(1, mapperHandlerResponse -> {
            List<JsonObject> result = mapperHandlerResponse.getResult();
            LOGGER.info(result.get(0).encode());
        });
    }
}
