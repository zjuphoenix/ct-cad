package com.zju.lab.ct;

import com.zju.lab.ct.framework.subject.VertxPublishSubject;
import com.zju.lab.ct.shutdown.ShutdownHookHub;
import com.zju.lab.ct.utils.AppUtil;
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
        options.setMaxWorkerExecuteTime(Long.MAX_VALUE);

        Vertx vertx = Vertx.vertx(options);
        VertxPublishSubject vertxPublishSubject = new VertxPublishSubject();
        vertxPublishSubject.initSubscription();
        vertxPublishSubject.publishSubject(vertx);

        /*添加钩子函数,保证vertx的正常关闭*/
        ShutdownHookHub.registerShutdownHook(new ShutdownHookHub.ShutdownHook() {
            @Override
            public String topic() {
                return "vertx-instance";
            }

            @Override
            public void call() {
                vertx.close();
                LOGGER.info("vertx-instance stop success!");
            }
        });
    }
}
