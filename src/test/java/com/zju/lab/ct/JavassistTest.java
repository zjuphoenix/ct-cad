package com.zju.lab.ct;

import com.zju.lab.ct.orm.container.MapperHandlerResponse;
import com.zju.lab.ct.orm.dao.TestMapper;
import io.vertx.core.Handler;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;

/**
 * Created by wuhaitao on 2016/3/27.
 */
public class JavassistTest {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        ProxyFactory factory=new ProxyFactory();
        //设置父类，ProxyFactory将会动态生成一个类，继承该父类
        factory.setInterfaces(new Class[]{TestMapper.class});
        Class<?> c = factory.createClass();
        TestMapper testMapper = (TestMapper) c.newInstance();
        ((Proxy) testMapper).setHandler(new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                return proceed.invoke(self, args);
            }
        });
        testMapper.test(1, new Handler<MapperHandlerResponse>() {
            @Override
            public void handle(MapperHandlerResponse mapperHandlerResponse) {
                mapperHandlerResponse.setError("1234");
            }
        });
    }
}
