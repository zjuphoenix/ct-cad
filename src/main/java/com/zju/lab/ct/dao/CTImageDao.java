package com.zju.lab.ct.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.cache.CTPageKey;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.RecordMapper;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.Record;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.verticle.EventBusMessage;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author wuhaitao
 * @date 2016/3/10 22:15
 */
@HandlerDao
public class CTImageDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTImageDao.class);
    private Vertx vertx;
    private SqlSessionFactory sqlSessionFactory;
    private LoadingCache<CTPageKey, JsonObject> cache;
    @Inject
    public CTImageDao(Vertx vertx, SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
        this.vertx = vertx;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<CTPageKey, JsonObject>() {
                    @Override
                    public JsonObject load(CTPageKey ctPageKey) throws Exception {
                        LOGGER.info("query db ct table.");
                        SqlSession session= sqlSessionFactory.openSession();
                        CTMapper ctMapper = session.getMapper(CTMapper.class);
                        int recordId = ctPageKey.getRecordId();
                        int pageIndex = ctPageKey.getPageIndex();
                        int pageSize = ctPageKey.getPageSize();
                        List<CTImage> ctImages = ctMapper.queryCTs(recordId, (pageIndex-1)*pageSize, pageSize);
                        int sum = ctMapper.queryCTCountByRecordId(recordId);
                        JsonObject res = new JsonObject();
                        res.put("ct", ctImages);
                        res.put("count", sum);
                        return res;
                    }
                });
    }

    private JsonObject ct2Json(CTImage ctImage){
        JsonObject obj = new JsonObject();
        obj.put("id", ctImage.getId());
        obj.put("type", ctImage.getType());
        obj.put("file", ctImage.getFile());
        obj.put("diagnosis", ctImage.getDiagnosis());
        obj.put("recordId", ctImage.getRecordId());
        obj.put("recognition", ctImage.getRecognition());
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
                ctImageHandler.handle(new ResponseMsg<>(ct2Json(ctImage)));
            }
            else{
                LOGGER.error("ctimage not found!");
                ctImageHandler.handle(new ResponseMsg<>(HttpCode.NOT_FOUND, "ctimage not found!"));
            }
        } catch (Exception e) {
            LOGGER.error("get ctimage by id failed!");
            ctImageHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
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
                responseMsgHandler.handle(new ResponseMsg<>("delete ct success!"));
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
                responseMsgHandler.handle(new ResponseMsg<>(HttpCode.NOT_FOUND, "ct not found"));
            }
            session.commit();
            cache.invalidateAll();
        } catch (Exception e) {
            LOGGER.error("delete ctimage by id {} failed!", id, e);
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 根据recordId获取所有CT图像
     * @param recordId
     * @param ctsHandler
     */
    public void getCTImages(int recordId, Handler<ResponseMsg<JsonObject>> ctsHandler){
        SqlSession session= sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            List<CTImage> ctImages = ctMapper.queryAllCTsByRecordId(recordId);
            //List<JsonObject> cts = ctImages.stream().flatMap(ctImage -> Stream.of(ct2Json(ctImage))).collect(Collectors.toList());
            ctsHandler.handle(new ResponseMsg<>(new JsonObject().put("ct",ctImages)));
        } catch (Exception e) {
            ctsHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 插入一张CT图像记录,在单张CT图像上传后调用
     * @param ctImage
     * @param responseMsgHandler
     */
    public void addCTImage(CTImage ctImage, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.addCT(ctImage);
            session.commit();
            cache.invalidateAll();
            responseMsgHandler.handle(new ResponseMsg<>("upload ct success"));
        } catch (Exception e) {
            LOGGER.info("insert ct {} failed!", ctImage.getFile());
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
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
            cache.invalidateAll();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 更新CT图像诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateCTImage(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.updateCTDiagnosis(id, diagnosis);
            session.commit();
            cache.invalidateAll();
            LOGGER.info("update ct success!");
            responseMsgHandler.handle(new ResponseMsg<>("update ct diagnosis success!"));
        } catch (Exception e) {
            LOGGER.error("update ct diagnosis failed!");
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 根据recordId获取CT图像分页数据
     * @param recordId
     * @param pageIndex
     * @param pageSize
     * @param ctsHandler
     */
    public void getCTImagesByPage(int recordId, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> ctsHandler){
        try {
            JsonObject res = cache.get(new CTPageKey(pageIndex, pageSize, recordId));
            ctsHandler.handle(new ResponseMsg<>(res));
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            ctsHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 更新CT图像全局特征识别结果
     * @param id
     * @param recognition
     * @param responseMsgHandler
     */
    public void updateRecognition(int id, int recognition, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            ctMapper.updateCTRecognition(id, recognition);
            session.commit();
            cache.invalidateAll();
            LOGGER.info("update ct recognition success!");
            responseMsgHandler.handle(new ResponseMsg<>("update ct recognition success!"));
        } catch (Exception e) {
            LOGGER.error("update ct recognition failed!");
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
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
                ctsHandler.handle(new ResponseMsg<>(jsonObject));
            }
            else{
                jsonObject.put("status", -1);
                ctsHandler.handle(new ResponseMsg<>(jsonObject));
            }
        } catch (Exception e) {
            LOGGER.error("query ct data failed!");
            ctsHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
