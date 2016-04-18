package com.zju.lab.ct.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/4/18.
 */
public class ShutdownHookHub {
    public static interface ShutdownHook{
        String topic();
        void call();
    }

    private static Logger logger = LoggerFactory.getLogger(ShutdownHookHub.class);
    private final static List<ShutdownHook> hooks = new ArrayList<>();

    static {
        Thread thread = new Thread(() -> {
            callback();
        });
        thread.setName("shutdown-hook-thread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    private static void callback(){
        logger.info("shutdown hook list executed, size: {}.", hooks.size());
        hooks.forEach(hook -> {
            logger.info("shutdown hook:{} started to call.", hook.topic());
            hook.call();
        });
    }

    public static void registerShutdownHook(ShutdownHook shutdownHook){
        if (shutdownHook == null){
            logger.warn("shutdown hook is null.");
            return;
        }
        hooks.add(shutdownHook);
        logger.info("shutdown hook: {} has been registered.", shutdownHook.topic());
    }
}
