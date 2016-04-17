package com.zju.lab.ct;

import com.google.inject.*;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.handlers.CTImageHandler;
import com.zju.lab.ct.ioc.BeanIOCModule;
import io.vertx.core.Vertx;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuhaitao on 2016/4/17.
 */
public class IOCTest {
    @Test
    public void test(){
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new BeanIOCModule(Vertx.vertx()));
        Map<Key<?>,Binding<?>> bindingMap = injector.getAllBindings();
        bindingMap.forEach((key, value) -> {
            System.out.println(key.toString());
        });
        Vertx vertx = injector.getInstance(Vertx.class);
        System.out.println(vertx.toString());
    }
}
