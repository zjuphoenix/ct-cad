package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.ConsultationDao;
import com.zju.lab.ct.model.Consultation;
import com.zju.lab.ct.model.HttpCode;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuhaitao on 2016/3/9.
 */
@RouteHandler("/api/consultation")
public class ConsultationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationHandler.class);

    private ConsultationDao consultationDao;

    public ConsultationHandler(ConsultationDao consultationDao) {
        this.consultationDao = consultationDao;
    }

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> getConsultations(){
        return  ctx -> {
            consultationDao.getConsultations(result -> {
                JsonArray cts = new JsonArray();
                if (result != null){
                    for (Consultation consultation : result){
                        JsonObject obj = new JsonObject();
                        obj.put("id", consultation.getId());
                        obj.put("created", consultation.getCreated());
                        obj.put("record", consultation.getRecord());
                        obj.put("updated", consultation.getUpdated());
                        cts.add(obj);
                    }
                }
                HttpServerResponse response = ctx.response();
                response.putHeader("Access-Control-Allow-Origin", "*")
                        .putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS")
                        .putHeader("Access-Control-Max-Age", "60")
                        .putHeader("Access-Control-Allow-Credentials", "false");
                response.setChunked(true);
                response.end(cts.encode());
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/page")
    public Handler<RoutingContext> getConsultationsByPage(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int pageIndex = data.getInteger("pageIndex");
            int pageSize = data.getInteger("pageSize");
            consultationDao.getConsultationsByPage(pageIndex, pageSize, result -> {
                JsonArray cts = new JsonArray();
                HttpServerResponse response = ctx.response();
                response.setChunked(true);
                response.putHeader("Access-Control-Allow-Origin", "*")
                        .putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS")
                        .putHeader("Access-Control-Max-Age", "60")
                        .putHeader("Access-Control-Allow-Credentials", "false")
                        .putHeader("Access-Control-Allow-Headers", "true");
                if (result != null){
                    response.end(result.encode());
                }
                else {
                    response.setStatusCode(HttpCode.NULL_CONTENT.getCode()).end();
                }
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/add")
    public Handler<RoutingContext> addConsultation(){
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = Integer.parseInt(data.getString("id"));
            String created = data.getString("created");
            String record = data.getString("record");
            String updated = data.getString("updated");
            HttpServerResponse response = ctx.response();
            response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            consultationDao.addConsultation(new Consultation(id,created,record,updated), responseMsg -> {
                if (responseMsg.getCode().getCode() == HttpCode.OK.getCode()){
                    response.end(responseMsg.getContent());
                }
                else{
                    response.setStatusCode(responseMsg.getCode().getCode()).end(responseMsg.getContent());
                }
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/update")
    public Handler<RoutingContext> updateConsultation(){
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = Integer.parseInt(data.getString("id"));
            String record = data.getString("record");
            Consultation consultation = new Consultation(id);
            if (StringUtils.isNotEmpty(record)){
                consultation.setRecord(record);
            }
            HttpServerResponse response = ctx.response();
            response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            consultationDao.updateConsultation(consultation, responseMsg -> {
                if (responseMsg.getCode().getCode() == HttpCode.OK.getCode()){
                    response.end(responseMsg.getContent());
                }
                else{
                    response.setStatusCode(responseMsg.getCode().getCode()).end(responseMsg.getContent());
                }
            });
        };
    }
}
