package com.zju.lab.ct.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.cache.PageKey;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.RecordMapper;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.Record;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.DBUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@HandlerDao
public class RecordsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsDao.class);

    /*protected JDBCClient sqlite = null;

    private JsonObject sqliteConfig = null;*/

    private LoadingCache<PageKey, List<JsonObject>> cache = null;

    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public RecordsDao(Vertx vertx, SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
        /*this.sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        this.sqlite = JDBCClient.createShared(vertx, sqliteConfig, "records");*/
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<PageKey, List<JsonObject>>() {
                    @Override
                    public List<JsonObject> load(PageKey pageKey) throws Exception {
                        LOGGER.info("query db.");
                        SqlSession session= sqlSessionFactory.openSession();
                        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
                        List<Record> records = recordMapper.queryRecords(null, (pageKey.getPageIndex()-1)*pageKey.getPageSize(), pageKey.getPageSize());
                        List<JsonObject> result = records.stream().flatMap(record -> {
                            JsonObject obj = new JsonObject();
                            obj.put("id", record.getId());
                            obj.put("diagnosis", record.getDiagnosis());
                            obj.put("username", record.getUsername());
                            return Stream.of(obj);
                        }).collect(Collectors.toList());
                        return result;
                        //return DBUtil.queryRecords(pageKey.getPageIndex(), pageKey.getPageSize(), pageKey.getUsername());
                    }
                });
    }

    /**
     * 按页查询病历记录
     * @param pageIndex
     * @param pageSize
     * @param recordsHandler
     */
    public void getRecordsByPage(int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> recordsHandler){
        PageKey pageKey = new PageKey(pageIndex, pageSize, null);
        try {
            List<JsonObject> records = cache.get(pageKey);
            SqlSession session= sqlSessionFactory.openSession();
            RecordMapper recordMapper = session.getMapper(RecordMapper.class);
            int count = recordMapper.queryRecordsCount();
            /*int count = DBUtil.getRecordCount(null);*/
            JsonObject res = new JsonObject();
            res.put("records", records);
            res.put("count", count);
            recordsHandler.handle(new ResponseMsg<JsonObject>(res));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
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
        });*/
    }

    /**
     * 按用户和页查询病历
     * @param username
     * @param pageIndex
     * @param pageSize
     * @param recordsHandler
     */
    public void getRecordsByUserPage(String username, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> recordsHandler){
        PageKey pageKey = new PageKey(pageIndex, pageSize, username);
        try {
            List<JsonObject> records = cache.get(pageKey);
            /*int count = DBUtil.getRecordCount(username);*/
            SqlSession session= sqlSessionFactory.openSession();
            RecordMapper recordMapper = session.getMapper(RecordMapper.class);
            int count = recordMapper.queryRecordsCountByUsername(username);
            JsonObject res = new JsonObject();
            res.put("records", records);
            res.put("count", count);
            recordsHandler.handle(new ResponseMsg<JsonObject>(res));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            recordsHandler.handle(new ResponseMsg<JsonObject>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        /*sqlite.getConnection(connection -> {
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
        });*/
    }

    /**
     * 插入新病历
     * @param username
     * @param responseMsgHandler
     */
    public void addRecord(String username, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession();
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        try {
            Record record = new Record();
            record.setUsername(username);
            recordMapper.addRecord(record);
            LOGGER.info("insert record success!");
            responseMsgHandler.handle(new ResponseMsg<String>("insert record success!"));
        } catch (Exception e) {
            LOGGER.error("insert record failed!");
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
        /*sqlite.getConnection(connection -> {
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
        });*/
    }

    /**
     * 更新病历诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateRecord(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession();
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        try {
            recordMapper.updateRecord(id, diagnosis);
            LOGGER.info("update record success!");
            responseMsgHandler.handle(new ResponseMsg<String>("update record success!"));
        } catch (Exception e) {
            LOGGER.error("update record failed!");
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
        /*sqlite.getConnection(connection -> {
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
        });*/
    }

    /**
     * 删除病历,包括病历记录和CT图像
     * @param id
     * @param responseMsgHandler
     */
    public void deleteRecord(int id, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            List<String> files = ctMapper.queryCTFileByRecordId(id);
            files.forEach(ct -> {
                File file = new File(AppUtil.getUploadDir()+File.separator+ct);
                if (file.exists()){
                    file.delete();
                }
                else{
                    LOGGER.info("ct image {} is not existing!",ct);
                }
            });
            ctMapper.deleteCTsByRecordId(id);
            recordMapper.deleteRecord(id);
            session.commit();
        } catch (Exception e) {
            LOGGER.error("delete record failed!", e);
            responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
        /*sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
                return;
            }
            SQLConnection conn = connection.result();
            conn.query("select file from ct where recordId = "+id, resultSetAsyncResult -> {
                List<JsonObject> jsonObjects = resultSetAsyncResult.result().getRows();
                for (JsonObject obj : jsonObjects){
                    File file = new File(AppUtil.getUploadDir()+File.separator+obj.getString("file"));
                    if (file.exists()){
                        file.delete();
                    }
                    else{
                        LOGGER.info("ct image {} is not existing!",obj.getString("file"));
                    }
                }
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
        });*/
    }
}
