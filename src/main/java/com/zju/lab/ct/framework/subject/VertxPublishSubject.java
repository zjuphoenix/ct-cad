package com.zju.lab.ct.framework.subject;

import com.zju.lab.ct.framework.observer.IocObserver;
import com.zju.lab.ct.framework.observer.WebObserver;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.PublishSubject;

/**
 * @author wuhaitao
 * @date 2016/4/22 10:49
 */
public class VertxPublishSubject {
    private Logger LOGGER = LoggerFactory.getLogger(VertxPublishSubject.class);
    private PublishSubject<Vertx> publishSubject;
    public VertxPublishSubject() {
        publishSubject = PublishSubject.create();
    }

    public void initSubscription(){
        LOGGER.info("subscription is initializing...");
        publishSubject.subscribe(new IocObserver());
        LOGGER.info("ioc subscription completed initializing.");
        publishSubject.subscribe(new WebObserver());
        LOGGER.info("web subscription completed initializing.");
    }

    public void publishSubject(Vertx vertx){
        publishSubject.onNext(vertx);
        publishSubject.onCompleted();
        LOGGER.info("vertx subject completed publishing.");
    }
}
