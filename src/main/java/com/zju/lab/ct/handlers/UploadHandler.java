package com.zju.lab.ct.handlers;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.CTImageDao;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by wuhaitao on 2016/3/8.
 */
@RouteHandler("/upload")
public class UploadHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHandler.class);

    @Inject
    private CTImageDao ctImageDao;


    /**
     * /upload
     * POST
     * 单文件上传
     * @return
     */
    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> upload() {
        return ctx -> {
            HttpServerRequest request = ctx.request();
            int id = Integer.parseInt(request.getParam("id"));
            int type = Integer.parseInt(request.getParam("type"));
            Set<FileUpload> files = ctx.fileUploads();
            for (FileUpload file : files) {
                String path = file.uploadedFileName();
                String img = path.substring(path.indexOf(AppUtil.configStr("upload.path"))+7);
                CTImage ctImage = new CTImage();
                ctImage.setType(type == 1 ? "肝脏" : "肺部");
                ctImage.setFile(img);
                ctImage.setDiagnosis("");
                ctImage.setRecordId(id);
                LOGGER.info("upload path : {}", path);
                ctImageDao.addCTImage(ctImage, responseMsg -> {
                    HttpServerResponse response = ctx.response();
                    ResponseUtil.responseContent(response, responseMsg);
                });
                break;
            }
        };
    }

    /**
     * /upload/files
     * POST
     * 多文件上传
     * @return
     */
    @RouteMapping(method = RouteMethod.POST, value = "/files")
    public Handler<RoutingContext> uploadFiles() {
        return ctx -> {
            String username = ctx.user().principal().getString("username");
            HttpServerRequest request = ctx.request();
            int type = Integer.parseInt(request.getParam("type"));
            Set<FileUpload> files = ctx.fileUploads();
            List<CTImage> ctImages = new ArrayList<>(files.size());
            files.forEach(file -> {
                String path = file.uploadedFileName();
                String img = path.substring(path.indexOf(AppUtil.configStr("upload.path"))+7);
                CTImage ctImage = new CTImage();
                ctImage.setType(type == 1 ? "肝脏" : "肺部");
                ctImage.setFile(img);
                ctImage.setDiagnosis("");
                LOGGER.info("upload path : {}", path);
                ctImages.add(ctImage);
            });
            ctImageDao.addCTImages(username, ctImages, responseMsg -> {
                HttpServerResponse response = ctx.response();
                ResponseUtil.responseContent(response, responseMsg);
            });
        };
    }

    /**
     * /upload/:image
     * GET
     * 获取上传的图片在前端显示，这里的图片不包含在静态资源里，而是通过web请求获取
     * @return
     */
    @RouteMapping(method = RouteMethod.GET, value = "/:image")
    public Handler<RoutingContext> getCTImage(){
        return  ctx -> {
            String image = ctx.request().getParam("image");
            HttpServerResponse response = ctx.response();
            //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            response.setChunked(true);
            response.sendFile(AppUtil.getUploadDir() + File.separator + image);
        };
    }

}