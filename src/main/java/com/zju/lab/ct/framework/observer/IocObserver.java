package com.zju.lab.ct.framework.observer;

import com.zju.lab.ct.ioc.IOCAppContext;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

/**
 * @author wuhaitao
 * @date 2016/4/22 10:09
 */
public class IocObserver implements Observer<Vertx>{
    private Logger LOGGER = LoggerFactory.getLogger(IocObserver.class);
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("ioc container failed initializing!", throwable);
    }

    @Override
    public void onNext(Vertx vertx) {
        /*初始化IOC容器，将vertx实例注入*/
        LOGGER.info("ioc container is initializing...");
        IOCAppContext iocAppContext = IOCAppContext.getInstance();
        iocAppContext.init(vertx);
        LOGGER.info("ioc container completed initializing!");
    }
}
