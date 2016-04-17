package com.zju.lab.ct.handlers;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.RecordsDao;
import com.zju.lab.ct.dao.ReportDao;
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
    @Inject
    private RecordsDao recordsDao;
    @Inject
    private ReportDao reportDao;


    /**
     * 病历分页管理，区分管理员、医生和病人，病人按用户名查询自己的病历，管理员和医生查询所有病历
     * @return
     */
    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> getRecordsByPage(){
        return  ctx -> {
            JsonObject data = ctx.getBodyAsJson();
            int pageIndex = data.getInteger("pageIndex");
            int pageSize = data.getInteger("pageSize");
            String username = data.getString("username");
            /*LOGGER.info("get records...");*/
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

    /**
     * 插入病历，目前还没有这个操作入口
     * @return
     */
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

    /**
     * 更新病历诊断结果
     * @return
     */
    @RouteMapping(method = RouteMethod.PUT, value = "/diagnosis")
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
     * DELETE /api/records/:id
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

    /**
     * 生成报表
     * @return
     */
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

    /**
     * 报表文件浏览器预览下载请求
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/report/:report")
    public Handler<RoutingContext> getReport(){
        return  ctx -> {
            String report = ctx.request().getParam("report");
            HttpServerResponse response = ctx.response();
            //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            response.setChunked(true);
            response.sendFile(report);
        };
    }
}
