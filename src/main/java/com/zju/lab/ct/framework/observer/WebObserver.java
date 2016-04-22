package com.zju.lab.ct.framework.observer;

import com.zju.lab.ct.ioc.IOCAppContext;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.verticle.LesionRecognitionVerticle;
import com.zju.lab.ct.verticle.WebServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

/**
 * @author wuhaitao
 * @date 2016/4/22 10:09
 */
public class WebObserver implements Observer<Vertx>{
    private Logger LOGGER = LoggerFactory.getLogger(WebObserver.class);
    @Override
    public void onCompleted() {
        LOGGER.info("web module completed initializing!");
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("web module failed initializing!");
    }

    @Override
    public void onNext(Vertx vertx) {
        IOCAppContext iocAppContext = IOCAppContext.getInstance();
        /*设置verticle工作方式参数*/
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        //deploymentOptions.setHa(true);
        deploymentOptions.setInstances(AppUtil.configInt("instance.num"));
        deploymentOptions.setWorker(AppUtil.configBoolean("work.model"));
        /*deploymentOptions.setMultiThreaded(AppUtil.configBoolean("multi-threaded.model"));*/

        //Cannot use HttpServer in a multi-threaded worker verticle
        vertx.deployVerticle((WebServer)iocAppContext.getBean(WebServer.class), deploymentOptions);
        vertx.deployVerticle((LesionRecognitionVerticle)iocAppContext.getBean(LesionRecognitionVerticle.class), deploymentOptions);
    }
}
