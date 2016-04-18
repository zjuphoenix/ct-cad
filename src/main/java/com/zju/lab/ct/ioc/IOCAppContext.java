package com.zju.lab.ct.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import io.vertx.core.Vertx;

/**
 * Created by wuhaitao on 2016/4/17.
 */
public class IOCAppContext {
    private static IOCAppContext iocAppContext = new IOCAppContext();
    private Injector injector;

    private IOCAppContext(){

    }

    /*必须执行过init方法后IOC容器才算初始化完成，上下文才能使用*/
    public void init(Vertx vertx){
        injector = Guice.createInjector(Stage.PRODUCTION, new BeanIOCModule(vertx));
    }

    public static IOCAppContext getInstance(){
        return iocAppContext;
    }

    public Object getBean(Class<?> clazz){
        return injector.getInstance(clazz);
    }
}
