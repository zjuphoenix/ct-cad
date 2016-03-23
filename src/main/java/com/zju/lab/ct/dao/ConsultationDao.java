package com.zju.lab.ct.dao;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.model.Consultation;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/8.
 */
@HandlerDao
public class ConsultationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationDao.class);

    protected JDBCClient sqlite = null;

    public ConsultationDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "consultation");
    }

    public void uploadCT(Consultation consultation, Handler<Consultation> consultationHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                consultationHandler.handle(null);
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from consultation where id = "+consultation.getId(), result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        if (objs != null && !objs.isEmpty()) {

                            consultationHandler.handle(consultation);
                        }
                        else{
                            consultationHandler.handle(null);
                        }
                    }
                    else{
                        LOGGER.error("insert data failed!");
                        consultationHandler.handle(null);
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });
    }

    public void getConsultations(Handler<List<Consultation>> consultationsHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                consultationsHandler.handle(null);
            }
            else{
                SQLConnection conn = connection.result();
                conn.query("select * from consultation", result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        List<Consultation> consultations = null;
                        Consultation consultation = null;
                        if (objs != null && !objs.isEmpty()) {
                            consultations = new ArrayList<>();
                            for (JsonObject obj : objs) {
                                consultation = new Consultation();
                                consultation.setId(obj.getInteger("id"));
                                consultation.setCreated(obj.getString("created"));
                                consultation.setRecord(obj.getString("record"));
                                consultation.setUpdated(obj.getString("updated"));
                                consultations.add(consultation);
                            }
                            consultationsHandler.handle(consultations);
                        }
                        else{
                            consultationsHandler.handle(null);
                        }
                    }
                    else{
                        LOGGER.error("insert data failed!");
                        consultationsHandler.handle(null);
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });
    }

    public void getConsultationsByPage(int pageIndex, int pageSize, Handler<JsonObject> consultationsHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
                LOGGER.error("connection sqlite failed!");
                consultationsHandler.handle(null);
            }
            else{
                SQLConnection conn = connection.result();
                JsonArray params = new JsonArray().add(pageSize).add((pageIndex-1)*pageSize);
                conn.queryWithParams("select * from consultation limit ? offset ?", params, result -> {
                    if (result.succeeded()){
                        List<JsonObject> objs = result.result().getRows();
                        JsonObject res = new JsonObject();
                        res.put("consultations", objs);
                        conn.query("select count(*) from consultation", count -> {
                            if (count.succeeded()) {
                                int sum = count.result().getRows().get(0).getInteger("count(*)");
                                res.put("count", sum);
                                consultationsHandler.handle(res);
                            }
                            else{
                                consultationsHandler.handle(null);
                            }
                        });
                    }
                    else{
                        LOGGER.error("insert data failed!");
                        consultationsHandler.handle(null);
                    }
                    JDBCConnUtil.close(conn);
                });
            }
        });
    }

    public void addConsultation(Consultation consultation, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
               /*System.out.println("connection sqlite failed!");*/
               LOGGER.error("connection sqlite failed!");
               responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, "sqlite connected failed!"));
               return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray().add(consultation.getId()).add(consultation.getCreated()).add(consultation.getRecord()).add(consultation.getUpdated());
            String sql = "insert into consultation(id,created,record,updated) values(?,?,?,?)";
            conn.updateWithParams(sql, params, insertResult -> {
                if (insertResult.succeeded()){
                    /*System.out.println("insert data success!");*/
                    LOGGER.info("insert data success!");
                    responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.OK, "insert consultation success!"));
                }
                else{
                    /*System.out.println("insert data failed!");*/
                    LOGGER.error("insert data failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, "sqlite insert data failed!"));
                }
            });
        });
    }

    public void updateConsultation(Consultation consultation, Handler<ResponseMsg<String>> responseMsgHandler){
        sqlite.getConnection(connection -> {
            if (connection.failed()){
               /*System.out.println("connection sqlite failed!");*/
                LOGGER.error("connection sqlite failed!");
                responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, "sqlite connected failed!"));
                return;
            }
            SQLConnection conn = connection.result();
            JsonArray params = new JsonArray();
            StringBuilder sql = new StringBuilder("update consultation set updated = ?");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String updated = sdf.format(new Date());
            params.add(updated);
            if (StringUtils.isNotEmpty(consultation.getRecord())){
                params.add(consultation.getRecord());
                sql.append(",record = ?");
            }
            params.add(consultation.getId());
            sql.append(" where id = ?");
            conn.updateWithParams(sql.toString(), params, insertResult -> {
                if (insertResult.succeeded()) {
                    LOGGER.info("update consultation success!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.OK, "update consultation success!"));
                } else {
                    LOGGER.error("update consultation failed!");
                    responseMsgHandler.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, "sqlite update consultation failed!"));
                }
            });
        });
    }
}