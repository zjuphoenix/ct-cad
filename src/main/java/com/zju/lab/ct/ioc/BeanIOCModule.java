package com.zju.lab.ct.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.utils.Constants;
import com.zju.lab.ct.verticle.LesionRecognitionVerticle;
import com.zju.lab.ct.verticle.WebServer;
import io.vertx.core.Vertx;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

/**
 * Created by wuhaitao on 2016/4/17.
 */
public class BeanIOCModule extends AbstractModule{
    private static Logger LOGGER = LoggerFactory.getLogger(BeanIOCModule.class);
    private Vertx vertx;

    public BeanIOCModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        /*vertx*/
        bind(Vertx.class).toInstance(vertx);
        /*dao*/
        Reflections daoReflections = new Reflections(Constants.DAOPATH);
        Set<Class<?>> daos = daoReflections.getTypesAnnotatedWith(HandlerDao.class);
        daos.forEach(dao -> {
            bind(dao).in(Singleton.class);
        });
        /*handler*/
        Reflections handlerReflections = new Reflections(Constants.HANDLERPATH);
        Set<Class<?>> handlers = handlerReflections.getTypesAnnotatedWith(RouteHandler.class);
        handlers.forEach(handler -> {
            bind(handler).in(Singleton.class);
        });
        /*verticle*/
        bind(WebServer.class).in(Singleton.class);
        bind(LesionRecognitionVerticle.class).in(Singleton.class);
    }
}
