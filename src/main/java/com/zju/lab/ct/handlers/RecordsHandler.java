package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.RecordsDao;
import com.zju.lab.ct.dao.ReportDao;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@RouteHandler("/api/records")
public class RecordsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsHandler.class);
    private RecordsDao recordsDao;
    private ReportDao reportDao;

    public RecordsHandler(RecordsDao recordsDao, ReportDao reportDao) {
        this.recordsDao = recordsDao;
        this.reportDao = reportDao;
    }

    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> getRecordsByPage(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int pageIndex = data.getInteger("pageIndex");
            int pageSize = data.getInteger("pageSize");
            String username = data.getString("username");
            if (StringUtils.isEmpty(username)) {
                recordsDao.getRecordsByPage(pageIndex, pageSize, result -> {
                    HttpServerResponse response = ctx.response();
                    ResponseUtil.responseContent(response, result);
                });
            }
            else{
                recordsDao.getRecordsByUserPage(username, pageIndex, pageSize, result -> {
                    HttpServerResponse response = ctx.response();
                    ResponseUtil.responseContent(response, result);
                });
            }
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/:username")
    public Handler<RoutingContext> addRecord(){
        return ctx -> {
            String username = ctx.request().getParam("username");
            recordsDao.addRecord(username, stringResponseMsg -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, stringResponseMsg);
            });
        };
    }

    @RouteMapping(method = RouteMethod.PUT, value = "/:username")
    public Handler<RoutingContext> updateRecord(){
        return ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int id = data.getInteger("id");
            String diagnosis = data.getString("diagnosis");
            recordsDao.updateRecord(id, diagnosis, stringResponseMsg -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, stringResponseMsg);
            });
        };
    }

    /**
     * 根据id删除CT病历
     * @return
     */
    @RouteMapping(method = RouteMethod.DELETE, value = "/:id")
    public Handler<RoutingContext> deleteRecord(){
        return ctx -> {
            int id = Integer.parseInt(ctx.request().getParam("id"));
            recordsDao.deleteRecord(id, stringResponseMsg -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, stringResponseMsg);
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST, value = "/report")
    public Handler<RoutingContext> report(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int recordId = data.getInteger("id");
            String diagnosis = data.getString("diagnosis");
            String username = data.getString("username");
            reportDao.report(recordId, username, diagnosis, stringResponseMsg -> {
                ResponseUtil.responseContent(ctx.response(), stringResponseMsg);
            });
        };
    }

    @RouteMapping(method = RouteMethod.GET, value = "/report/:report")
    public Handler<RoutingContext> getReport(){
        return  ctx -> {
            String report = ctx.request().getParam("report");
            HttpServerResponse response = ctx.response();
            response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            response.setChunked(true);
            response.sendFile(report);
        };
    }
}
