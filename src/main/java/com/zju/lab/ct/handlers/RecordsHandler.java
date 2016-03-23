package com.zju.lab.ct.handlers;

import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.RecordsDao;
import com.zju.lab.ct.utils.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@RouteHandler("/api/records")
public class RecordsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsHandler.class);
    private RecordsDao recordsDao;

    public RecordsHandler(RecordsDao recordsDao) {
        this.recordsDao = recordsDao;
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
}
