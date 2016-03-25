package com.zju.lab.ct.dao;

import com.zju.lab.ct.annotations.HandlerDao;
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
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@HandlerDao
public class RecordsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsDao.class);

    protected JDBCClient sqlite = null;

    public RecordsDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "records");
    }

    /**
     * 按页查询病历记录
     * @param pageIndex
     * @param pageSize
     * @param recordsHandler
     */
    public void getRecordsByPage(int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> recordsHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray().add(pageSize).add((pageIndex-1)*pageSize);
                conn.queryWithParams("select * from record limit ? offset ?", params, result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        JsonObject res = new JsonObject();
                        res.put("records", objs);
                        conn.query("select count(*) from record", count -> {
                            if (count.succeeded()) {
                                int sum = count.result().getRows().get(0).getInteger("count(*)");
                                res.put("count", sum);
                                recordsHandler.handle(new ResponseMsg<JsonObject>(res));
                            }
                            else{
                                recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, count.cause().getMessage()));
                            }
                            JDBCConnUtil.close(conn);
                        });
                    }
                    else{
                        LOGGER.error("query records failed!");
                        recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                        JDBCConnUtil.close(conn);
                    }
                });
            }
        });
    }

    /**
     * 按用户和页查询病历
     * @param username
     * @param pageIndex
     * @param pageSize
     * @param recordsHandler
     */
    public void getRecordsByUserPage(String username, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> recordsHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray().add(username).add(pageSize).add((pageIndex-1)*pageSize);
                conn.queryWithParams("select * from record where username = ? limit ? offset ?", params, result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        JsonObject res = new JsonObject();
                        res.put("records", objs);
                        conn.query("select count(*) from record where username = "+username, count -> {
                            if (count.succeeded()) {
                                int sum = count.result().getRows().get(0).getInteger("count(*)");
                                res.put("count", sum);
                                recordsHandler.handle(new ResponseMsg<JsonObject>(res));
                            }
                            else{
                                recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, count.cause().getMessage()));
                            }
                            JDBCConnUtil.close(conn);
                        });
                    }
                    else{
                        LOGGER.error("query records failed!");
                        recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, result.cause().getMessage()));
                        JDBCConnUtil.close(conn);
                    }
                });
            }
        });
    }

    /**
     * 插入新病历
     * @param username
     * @param responseMsgHandler
     */
    public void addRecord(String username, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(username);
            String sql = "insert into record(username) values(?)";
            conn.updateWithParams(sql, params, insertResult -> {
                if (insertResult.succeeded()){
                    LOGGER.info("insert record success!");
                    responseMsgHandler.handle(new ResponseMsg<String>("insert record success!"));
                }
                else{
                    LOGGER.error("insert record failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, insertResult.cause().getMessage()));
                }
            });
        });
    }

    /**
     * 更新病历诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateRecord(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(diagnosis).add(id);
            String sql = "update record set diagnosis = ? where id = ?";
            conn.updateWithParams(sql, params, updateResultAsyncResult -> {
                if (updateResultAsyncResult.succeeded()){
                    LOGGER.info("update record success!");
                    responseMsgHandler.handle(new ResponseMsg<String>("update record success!"));
                }
                else{
                    LOGGER.error("update record failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                }
            });
        });
    }

    /**
     * 删除病历,包括病历记录和CT图像
     * @param id
     * @param responseMsgHandler
     */
    public void deleteRecord(int id, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            conn.update("delete from ct where recordId = "+id, updateResultAsyncResult1 -> {
                if (updateResultAsyncResult1.succeeded()){
                    String sql = "delete from record where id = "+id;
                    conn.update(sql, updateResultAsyncResult -> {
                        if (updateResultAsyncResult.succeeded()){
                            LOGGER.info("delete record success!");
                            responseMsgHandler.handle(new ResponseMsg<String>("delete record success!"));
                        }
                        else{
                            LOGGER.error("delete record failed!");
                            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                        }
                    });
                }
                else{
                    LOGGER.error("delete ct by recordId failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult1.cause().getMessage()));
                }
            });

        });
    }
}
