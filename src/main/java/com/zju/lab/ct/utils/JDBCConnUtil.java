package com.zju.lab.ct.utils;

import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuhaitao on 2016/3/10.
 */
public class JDBCConnUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCConnUtil.class);

    /**
     * 关闭vertx jdbc链接
     *
     * @param conn
     */
    public static void close(SQLConnection...conn) {
        if(null == conn || conn.length == 0){
            return;
        }
        for(SQLConnection c : conn){
            c.close(closeResult -> {
                if(closeResult.failed()){
                    LOGGER.warn("关闭vertx jdbc连接失败!",closeResult.cause());
                }
            });
        }
    }
}
