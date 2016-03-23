package com.zju.lab.ct.dao;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.JDBCConnUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
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

    protected JDBCClient sqlite = null;

    public CTImageDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "ct");
    }

    /**
     * 根据id获取一条CT数据
     * @param id
     * @param ctImageHandler
     */
    public void getCTImageById(int id, Handler<ResponseMsg<JsonObject>> ctImageHandler){
        sqlite.getConnection(connection -> {
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
        });
    }

    /**
     * 根据id删除CT图像，包括图像文件和数据表记录
     * @param id
     * @param responseMsgHandler
     */
    public void deleteCTImageById(int id, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
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
                                    responseMsgHandler.handle(new ResponseMsg(HttpCode.OK, "delete ct success!"));
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
        });
    }

    /**
     * 根据recordId获取所有CT图像
     * @param recordId
     * @param ctsHandler
     */
    public void getCTImages(int recordId, Handler<ResponseMsg<JsonObject>> ctsHandler){
        sqlite.getConnection(connection -> {
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
        });
    }

    /**
     * 上传CT数据
     * @param username
     * @param ctImages
     * @param responseMsgHandler
     */
    public void addCTImages(String username, List<CTImage> ctImages, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            LOGGER.info("start receive file");
            SQLConnection conn = connection.result();
            conn.updateWithParams("insert into record(username) values(?)", new JsonArray().add(username), updateResultAsyncResult -> {
                if (updateResultAsyncResult.succeeded()){
                    conn.query("select max(id) from record", resultSetAsyncResult -> {
                        if (resultSetAsyncResult.succeeded()){
                            int id = resultSetAsyncResult.result().getRows().get(0).getInteger("max(id)");
                            ctImages.forEach(ctImage -> {
                                JsonArray params = new JsonArray().add(ctImage.getType()).add(ctImage.getFile()).add(ctImage.getDiagnosis()).add(id);
                                String sql = "insert into ct(type,file,diagnosis,recordId) values(?,?,?,?)";
                                conn.updateWithParams(sql, params, insertResult -> {
                                    LOGGER.info("receive file");
                                    if (insertResult.failed()) {
                                        LOGGER.info("insert ct {} failed!", ctImage.getFile());
                                    }
                                });
                            });
                            responseMsgHandler.handle(new ResponseMsg<String>("insert ct success"));
                        }
                        else{
                            responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, resultSetAsyncResult.cause().getMessage()));
                        }
                    });
                }
                else{
                    responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                }
            });
            LOGGER.info("receive file finished");
        });
    }

    /**
     * 更新CT图像诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateCTImage(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
               /*System.out.println("connection sqlite failed!");*/
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
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.OK, "update ct diagnosis success!"));
                } else {
                    LOGGER.error("update ct failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, insertResult.cause().getMessage()));
                }
                JDBCConnUtil.close(conn);
            });
        });
    }

    /**
     * 根据recordId获取CT图像分页数据
     * @param recordId
     * @param pageIndex
     * @param pageSize
     * @param ctsHandler
     */
    public void getCTImagesByPage(int recordId, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> ctsHandler){
        sqlite.getConnection(connection -> {
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
        });
    }
}