package com.zju.lab.ct.dao;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.RecordMapper;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.Record;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.JDBCConnUtil;
import com.zju.lab.ct.verticle.EventBusMessage;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/10.
 */
@HandlerDao
public class CTImageDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTImageDao.class);

    /*protected JDBCClient sqlite = null;
    private JsonObject sqliteConfig = null;*/
    private Vertx vertx;
    private SqlSessionFactory sqlSessionFactory;
    @Inject
    public CTImageDao(Vertx vertx, SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
        this.vertx = vertx;
        /*this.sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        this.sqlite = JDBCClient.createShared(vertx, sqliteConfig, "ct");*/
    }

    private JsonObject ct2Json(CTImage ctImage){
        JsonObject obj = new JsonObject();
        obj.put("id", obj.getInteger("id"));
        obj.put("type", obj.getString("type"));
        obj.put("file", obj.getString("file"));
        obj.put("diagnosis", obj.getString("diagnosis"));
        obj.put("recordId", obj.getInteger("recordId"));
        obj.put("recognition", obj.getInteger("recognition"));
        return obj;
    }

    /**
     * 根据id获取一条CT数据
     * @param id
     * @param ctImageHandler
     */
    public void getCTImageById(int id, Handler<ResponseMsg<JsonObject>> ctImageHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            CTImage ctImage = ctMapper.queryCTById(id);
            if (ctImage != null){
                ctImageHandler.handle(new ResponseMsg<JsonObject>(ct2Json(ctImage)));
            }
            else{
                LOGGER.error("ctimage not found!");
                ctImageHandler.handle(new ResponseMsg<JsonObject>(HttpCode.NOT_FOUND, "ctimage not found!"));
            }
        } catch (Exception e) {
            LOGGER.error("get ctimage by id failed!");
            ctImageHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                ctImageHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from ct where id = "+id, result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        CTImage ctImage = null;
                        if (objs != null && !objs.isEmpty()) {
                            ctImageHandler.handle(new ResponseMsg<JsonObject>(objs.get(0)));
                        }
                        else{
                            ctImageHandler.handle(new ResponseMsg<JsonObject>(HttpCode.NULL_CONTENT, "没有符合要求的CT数据"));
                        }
                    }
                    else{
                        LOGGER.error("get ctimage by id failed!");
                        ctImageHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });*/
    }

    /**
     * 根据id删除CT图像，包括图像文件和数据表记录
     * @param id
     * @param responseMsgHandler
     */
    public void deleteCTImageById(int id, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            CTImage ctImage = ctMapper.queryCTById(id);
            if (ctImage != null){
                ctMapper.deleteCTById(id);
                responseMsgHandler.handle(new ResponseMsg("delete ct success!"));
                String image = AppUtil.getUploadDir()+File.separator+ctImage.getFile();
                File file = new File(image);
                if (file.exists()){
                    file.delete();
                }
                else{
                    LOGGER.error("ct file {} is not existing!", image);
                }
            }
            else{
                responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.NOT_FOUND, "ct not found"));
            }
            session.commit();
        } catch (Exception e) {
            LOGGER.error("delete ctimage by id {} failed!", id, e);
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from ct where id="+id, result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        if (objs != null && !objs.isEmpty()) {
                            String image = AppUtil.getUploadDir()+File.separator+objs.get(0).getString("file");
                            File file = new File(image);
                            if (file.exists()){
                                file.delete();
                            }
                            else{
                                LOGGER.error("ct file {} is not existing!", image);
                            }
                            conn.update("delete from ct where id = "+id, updateResultAsyncResult -> {
                                if (updateResultAsyncResult.succeeded()){
                                    responseMsgHandler.handle(new ResponseMsg("delete ct success!"));
                                }
                                else{
                                    LOGGER.error("delete ctimage by id {} failed!", id);
                                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                                }
                                JDBCConnUtil.close(conn);
                            });
                        }
                        else{
                            responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.NOT_FOUND, "ct not found"));
                            JDBCConnUtil.close(conn);
                        }
                    }
                    else{
                        LOGGER.error("delete ctimage by id {} failed!", id);
                        responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                        JDBCConnUtil.close(conn);
                    }
                });
            }
        });*/
    }

    /**
     * 根据recordId获取所有CT图像
     * @param recordId
     * @param ctsHandler
     */
    public void getCTImages(int recordId, Handler<ResponseMsg<JsonObject>> ctsHandler){
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from ct where recordId = " + recordId, result -> {
                    if (result.succeeded()) {
                        List<JsonObject> objs = result.result().getRows();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("ct",objs);
                        jsonObject.put("count", objs==null?0:objs.size());
                        ctsHandler.handle(new ResponseMsg<JsonObject>(jsonObject));
                    } else {
                        LOGGER.error("insert data failed!");
                        ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });*/
    }

    /**
     * 插入一张CT图像记录,在单张CT图像上传后调用
     * @param ctImage
     * @param responseMsgHandler
     */
    public void addCTImage(CTImage ctImage, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.addCT(ctImage);
            responseMsgHandler.handle(new ResponseMsg<String>("upload ct success"));
        } catch (Exception e) {
            LOGGER.info("insert ct {} failed!", ctImage.getFile());
            responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            LOGGER.info("start receive file");
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(ctImage.getType()).add(ctImage.getFile()).add(ctImage.getDiagnosis()).add(ctImage.getRecordId());
            String sql = "insert into ct(type,file,diagnosis,recordId) values(?,?,?,?)";
            conn.updateWithParams(sql, params, insertResult -> {
                LOGGER.info("receive file");
                if (insertResult.failed()) {
                    LOGGER.info("insert ct {} failed!", ctImage.getFile());
                    responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, insertResult.cause().getMessage()));
                }
                else{
                    responseMsgHandler.handle(new ResponseMsg<String>("upload ct success"));
                }
            });
        });*/
    }

    /**
     * 上传多张CT数据，在多文件上传后调用
     * @param username
     * @param ctImages
     * @param responseMsgHandler
     */
    public void addCTImages(String username, List<CTImage> ctImages, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            Record record = new Record();
            record.setUsername(username);
            recordMapper.addRecord(record);
            int recordId = record.getId();
            for (CTImage ctImage : ctImages){
                ctImage.setRecordId(recordId);
                ctMapper.addCT(ctImage);
                int id = ctImage.getId();
                LOGGER.info("ctId:{}", id);
                JsonObject data = new JsonObject().put("file", ctImage.getFile()).put("id", id);
                vertx.eventBus().send(EventBusMessage.GLOBAL_FEATURE_RECOGNITION, data.encode());
            }
            session.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.succeeded()) {
                LOGGER.info("start receive file");
                SQLConnection conn = connection.result();
                conn.updateWithParams("insert into record(username) values(?)", new JsonArray().add(username), updateResultAsyncResult -> {
                    if (updateResultAsyncResult.succeeded()) {
                        JsonArray updateKeys = updateResultAsyncResult.result().getKeys();
                        LOGGER.info("updateKeys:" + updateKeys.encode());
                        int id = updateKeys.getInteger(0);
                        ctImages.forEach(ctImage -> {
                            JsonArray params = new JsonArray().add(ctImage.getType()).add(ctImage.getFile()).add(ctImage.getDiagnosis()).add(id);
                            String sql = "insert into ct(type,file,diagnosis,recordId) values(?,?,?,?)";
                            conn.updateWithParams(sql, params, insertResult -> {
                                LOGGER.info("receive file");
                                if (insertResult.failed()) {
                                    LOGGER.info("insert ct {} failed!", ctImage.getFile());
                                }
                                else{
                                    JsonArray insertKeys = insertResult.result().getKeys();
                                    int ctId = insertKeys.getInteger(0);
                                    LOGGER.info("ctId:{}", ctId);
                                    JsonObject data = new JsonObject().put("file", ctImage.getFile()).put("id", ctId);
                                    vertx.eventBus().send(EventBusMessage.GLOBAL_FEATURE_RECOGNITION, data.encode());
                                }
                            });
                        });
                        responseMsgHandler.handle(new ResponseMsg<String>("insert ct success"));
                    } else {
                        responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                    }
                });
                LOGGER.info("receive file finished");
            }
            else{
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
        });*/
    }

    /**
     * 更新CT图像诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateCTImage(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.updateCTDiagnosis(id, diagnosis);
            LOGGER.info("update ct success!");
            responseMsgHandler.handle(new ResponseMsg("update ct diagnosis success!"));
        } catch (Exception e) {
            LOGGER.error("update ct diagnosis failed!");
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
               *//*System.out.println("connection sqlite failed!");*//*
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(diagnosis).add(id);
            String sql = "update ct set diagnosis = ? where id = ?";
            conn.updateWithParams(sql, params, insertResult -> {
                if (insertResult.succeeded()) {
                    LOGGER.info("update ct success!");
                    responseMsgHandler.handle(new ResponseMsg("update ct diagnosis success!"));
                } else {
                    LOGGER.error("update ct failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, insertResult.cause().getMessage()));
                }
                JDBCConnUtil.close(conn);
            });
        });*/
    }

    /**
     * 根据recordId获取CT图像分页数据
     * @param recordId
     * @param pageIndex
     * @param pageSize
     * @param ctsHandler
     */
    public void getCTImagesByPage(int recordId, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> ctsHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            List<CTImage> ctImages = ctMapper.queryCTs(recordId, (pageIndex-1)*pageSize, pageSize);
            int sum = ctMapper.queryCTCountByRecordId(recordId);
            session.commit();
            JsonObject res = new JsonObject();
            res.put("ct", ctImages);
            res.put("count", sum);
            ctsHandler.handle(new ResponseMsg<JsonObject>(res));
        } catch (Exception e) {
            ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray().add(recordId).add(pageSize).add((pageIndex-1)*pageSize);
                conn.queryWithParams("select * from ct where recordId = ? limit ? offset ?", params, result -> {
                    if (result.succeeded()) {
                        List<JsonObject> objs = result.result().getRows();
                        JsonObject res = new JsonObject();
                        res.put("ct", objs);
                        conn.query("select count(*) from ct where recordId = "+recordId, count -> {
                            if (count.succeeded()) {
                                int sum = count.result().getRows().get(0).getInteger("count(*)");
                                res.put("count", sum);
                                ctsHandler.handle(new ResponseMsg<JsonObject>(res));
                            }
                            else{
                                ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, count.cause().getMessage()));
                            }
                            JDBCConnUtil.close(conn);
                        });

                    } else {
                        LOGGER.error("get ct data by page failed!");
                        ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                        JDBCConnUtil.close(conn);
                    }
                });
            }
        });*/
    }

    /**
     * 更新CT图像全局特征识别结果
     * @param id
     * @param recognition
     * @param responseMsgHandler
     */
    public void updateRecognition(int id, int recognition, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.updateCTRecognition(id, recognition);
            LOGGER.info("update ct recognition success!");
            responseMsgHandler.handle(new ResponseMsg("update ct recognition success!"));
        } catch (Exception e) {
            LOGGER.error("update ct recognition failed!");
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(recognition).add(id);
            String sql = "update ct set recognition = ? where id = ?";
            conn.updateWithParams(sql, params, insertResult -> {
                if (insertResult.succeeded()) {
                    LOGGER.info("update ct recognition success!");
                    responseMsgHandler.handle(new ResponseMsg("update ct recognition success!"));
                } else {
                    LOGGER.error("update ct recognition failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, insertResult.cause().getMessage()));
                }
                JDBCConnUtil.close(conn);
            });
        });*/
    }

    /**
     * 根据recordId获取全局特征智能识别为肝癌CT图像
     * @param recordId
     * @param ctsHandler
     */
    public void getCancerImages(int recordId, Handler<ResponseMsg<JsonObject>> ctsHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            List<CTImage> ctImages = ctMapper.queryCancerCT(recordId);
            JsonObject jsonObject = new JsonObject();
            if (ctImages != null && !ctImages.isEmpty()){
                jsonObject.put("status", 1);
                jsonObject.put("cancer",ctImages);
                ctsHandler.handle(new ResponseMsg<JsonObject>(jsonObject));
            }
            else{
                jsonObject.put("status", -1);
                ctsHandler.handle(new ResponseMsg<JsonObject>(jsonObject));
            }
        } catch (Exception e) {
            LOGGER.error("query ct data failed!");
            ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray().add(recordId);
                conn.queryWithParams("select * from ct where recordId = ?", params, result -> {
                    if (result.succeeded()) {
                        List<JsonObject> objs = result.result().getRows();
                        List<JsonObject> cancer = new ArrayList<>();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("status", 1);
                        for (JsonObject obj : objs){
                            if (obj.getInteger("recognition") == null){
                                jsonObject.put("status", -1);
                            }
                            else if(obj.getInteger("recognition").intValue() == 2){
                                cancer.add(obj);
                            }
                        }
                        jsonObject.put("cancer",cancer);
                        ctsHandler.handle(new ResponseMsg<JsonObject>(jsonObject));
                    } else {
                        LOGGER.error("query ct data failed!");
                        ctsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });*/
    }
}
