package com.zju.lab.ct;

import com.zju.lab.ct.shutdown.ShutdownHookHub;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.ioc.IOCAppContext;
import com.zju.lab.ct.verticle.LesionRecognitionVerticle;
import com.zju.lab.ct.verticle.WebServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuhaitao on 2016/2/25.
 */
public class App {
    private static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        /*设置工作线程*/
        options.setWorkerPoolSize(AppUtil.configInt("work.pool.size"));
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        /*初始化IOC容器，将vertx实例注入*/
        IOCAppContext iocAppContext = IOCAppContext.getInstance();
        iocAppContext.init(Vertx.vertx(options));
        Vertx vertx = (Vertx)iocAppContext.getBean(Vertx.class);
        /*设置verticle工作方式参数*/
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        //deploymentOptions.setHa(true);
        deploymentOptions.setInstances(AppUtil.configInt("instance.num"));
        deploymentOptions.setWorker(AppUtil.configBoolean("work.model"));
        /*deploymentOptions.setMultiThreaded(AppUtil.configBoolean("multi-threaded.model"));*/

        //Cannot use HttpServer in a multi-threaded worker verticle
        vertx.deployVerticle((WebServer)iocAppContext.getBean(WebServer.class), deploymentOptions);
        vertx.deployVerticle((LesionRecognitionVerticle)iocAppContext.getBean(LesionRecognitionVerticle.class));

        /*添加钩子函数,保证vertx的正常关闭*/
        ShutdownHookHub.registerShutdownHook(new ShutdownHookHub.ShutdownHook() {
            @Override
            public String topic() {
                return "vertx-instance";
            }

            @Override
            public void call() {
                vertx.close();
                LOGGER.info("vertx stop success!");
            }
        });
    }
}
