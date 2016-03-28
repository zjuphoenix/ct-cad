package com.zju.lab.ct.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.Constants;
import com.zju.lab.ct.utils.JDBCConnUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuhaitao on 2016/3/12.
 */
@HandlerDao
public class FeatureDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTImageDao.class);

    protected JDBCClient sqlite = null;
    private Map<String,Integer> lesionType;
    private Map<String,Integer> lungType;

    public FeatureDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "feature");
        try {
            URL url = getClass().getClassLoader().getResource(Constants.LESION);
            LOGGER.debug("Initialize lesion type from path : {}", url);
            ObjectMapper mapper = new ObjectMapper();
            JsonObject lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
            lesionType = new HashMap<>(5);
            for (int i = 1; i <= 5; i++) {
                lesionType.put(lesion.getString(String.valueOf(i)),i);
            }

            url = getClass().getClassLoader().getResource(Constants.LUNG);
            LOGGER.debug("Initialize lung type from path : {}", url);
            mapper = new ObjectMapper();
            lesion = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
            lungType = new HashMap<>(3);
            for (int i = 1; i <= 3; i++) {
                lungType.put(lesion.getString(String.valueOf(i)),i);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 添加肝脏局部病变特征数据到数据库
     * @param feature
     * @param label
     * @param handler
     */
    public void addLiverFeature(double[] feature, String label, Handler<String> handler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                handler.handle("connection sqlite failed!");
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray();
                for (int i=0;i<26;i++){
                    params.add(feature[i]);
                }
                params.add(lesionType.get(label));
                conn.updateWithParams("insert into feature(f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,label) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",params,result -> {
                    if (result.succeeded()){
                        handler.handle("success");
                    }
                    else{
                        handler.handle(result.cause().getMessage());
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });
    }

    /**
     * 添加肺部病变特征数据到数据库
     * @param feature
     * @param label
     * @param handler
     */
    public void addLungFeature(double[] feature, String label, Handler<String> handler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                handler.handle("connection sqlite failed!");
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray();
                for (int i=0;i<26;i++){
                    params.add(feature[i]);
                }
                params.add(lungType.get(label));
                conn.updateWithParams("insert into lungfeature(f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,label) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",params,result -> {
                    if (result.succeeded()){
                        handler.handle("success");
                    }
                    else{
                        handler.handle(result.cause().getMessage());
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });
    }

    /**
     * 从肝脏病变特征数据表feature中拉取所有数据，用于算法模型训练
     * @param samplesHandler
     */
    public void fetchLiverFeatureSamples(Handler<List<Double[]>> samplesHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                samplesHandler.handle(null);
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from feature", result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        List<Double[]> samples = new ArrayList<Double[]>(objs.size());
                        if (objs != null && !objs.isEmpty()) {
                            objs.forEach(obj -> {
                                Double[] d = new Double[27];
                                d[0] = obj.getDouble("f1");
                                d[1] = obj.getDouble("f2");
                                d[2] = obj.getDouble("f1");
                                d[3] = obj.getDouble("f1");
                                d[4] = obj.getDouble("f1");
                                d[5] = obj.getDouble("f1");
                                d[6] = obj.getDouble("f1");
                                d[7] = obj.getDouble("f1");
                                d[8] = obj.getDouble("f1");
                                d[9] = obj.getDouble("f1");
                                d[10] = obj.getDouble("f1");
                                d[11] = obj.getDouble("f1");
                                d[12] = obj.getDouble("f1");
                                d[13] = obj.getDouble("f1");
                                d[14] = obj.getDouble("f1");
                                d[15] = obj.getDouble("f1");
                                d[16] = obj.getDouble("f1");
                                d[17] = obj.getDouble("f1");
                                d[18] = obj.getDouble("f1");
                                d[19] = obj.getDouble("f1");
                                d[20] = obj.getDouble("f1");
                                d[21] = obj.getDouble("f1");
                                d[22] = obj.getDouble("f1");
                                d[23] = obj.getDouble("f1");
                                d[24] = obj.getDouble("f1");
                                d[25] = obj.getDouble("f1");
                                d[26] = (double)obj.getInteger("label");
                                samples.add(d);
                            });
                            samplesHandler.handle(samples);
                        }
                        else{
                            samplesHandler.handle(null);
                        }
                    }
                    else{
                        samplesHandler.handle(null);
                    }
                });
            }
        });
    }

    /**
     * 从肺部病变特征数据表feature中拉取所有数据，用于算法模型训练
     * @param samplesHandler
     */
    public void fetchLungFeatureSamples(Handler<List<Double[]>> samplesHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                samplesHandler.handle(null);
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from lungfeature", result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        List<Double[]> samples = new ArrayList<Double[]>(objs.size());
                        if (objs != null && !objs.isEmpty()) {
                            objs.forEach(obj -> {
                                Double[] d = new Double[27];
                                d[0] = obj.getDouble("f1");
                                d[1] = obj.getDouble("f2");
                                d[2] = obj.getDouble("f1");
                                d[3] = obj.getDouble("f1");
                                d[4] = obj.getDouble("f1");
                                d[5] = obj.getDouble("f1");
                                d[6] = obj.getDouble("f1");
                                d[7] = obj.getDouble("f1");
                                d[8] = obj.getDouble("f1");
                                d[9] = obj.getDouble("f1");
                                d[10] = obj.getDouble("f1");
                                d[11] = obj.getDouble("f1");
                                d[12] = obj.getDouble("f1");
                                d[13] = obj.getDouble("f1");
                                d[14] = obj.getDouble("f1");
                                d[15] = obj.getDouble("f1");
                                d[16] = obj.getDouble("f1");
                                d[17] = obj.getDouble("f1");
                                d[18] = obj.getDouble("f1");
                                d[19] = obj.getDouble("f1");
                                d[20] = obj.getDouble("f1");
                                d[21] = obj.getDouble("f1");
                                d[22] = obj.getDouble("f1");
                                d[23] = obj.getDouble("f1");
                                d[24] = obj.getDouble("f1");
                                d[25] = obj.getDouble("f1");
                                d[26] = (double)obj.getInteger("label");
                                samples.add(d);
                            });
                            samplesHandler.handle(samples);
                        }
                        else{
                            samplesHandler.handle(null);
                        }
                    }
                    else{
                        samplesHandler.handle(null);
                    }
                });
            }
        });
    }
}
