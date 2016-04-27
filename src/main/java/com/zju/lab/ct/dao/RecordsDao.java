package com.zju.lab.ct.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.cache.RecordPageKey;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.RecordMapper;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.Record;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@HandlerDao
public class RecordsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsDao.class);

    private LoadingCache<RecordPageKey, JsonObject> cache = null;

    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public RecordsDao(SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new CacheLoader<RecordPageKey, JsonObject>() {
                    @Override
                    public JsonObject load(RecordPageKey recordPageKey) throws Exception {
                        LOGGER.info("query db record table.");
                        SqlSession session= sqlSessionFactory.openSession();
                        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
                        String username = recordPageKey.getUsername();
                        List<Record> records = recordMapper.queryRecords(username, (recordPageKey.getPageIndex()-1)* recordPageKey.getPageSize(), recordPageKey.getPageSize());
                        int count = recordMapper.queryRecordsCount();
                        JsonObject res = new JsonObject();
                        res.put("records", records);
                        res.put("count", count);
                        return res;
                        /*List<JsonObject> result = records.stream().flatMap(record -> {
                            JsonObject obj = new JsonObject();
                            obj.put("id", record.getId());
                            obj.put("diagnosis", record.getDiagnosis());
                            obj.put("username", record.getUsername());
                            return Stream.of(obj);
                        }).collect(Collectors.toList());
                        return result;*/
                    }
                });
    }

    /**
     * 按页查询病历记录
     * @param username
     * @param pageIndex
     * @param pageSize
     * @param recordsHandler
     */
    public void getRecordsByPage(String username, int pageIndex, int pageSize, Handler<ResponseMsg<JsonObject>> recordsHandler){
        RecordPageKey recordPageKey = new RecordPageKey(pageIndex, pageSize, StringUtils.isEmpty(username)?null:username);
        try {
            JsonObject res = cache.get(recordPageKey);
            recordsHandler.handle(new ResponseMsg<>(res));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            recordsHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 插入新病历
     * @param username
     * @param responseMsgHandler
     */
    public void addRecord(String username, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        try {
            Record record = new Record();
            record.setUsername(username);
            recordMapper.addRecord(record);
            session.commit();
            LOGGER.info("insert record success!");
            responseMsgHandler.handle(new ResponseMsg<>("insert record success!"));
        } catch (Exception e) {
            LOGGER.error("insert record failed!");
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
    }

    /**
     * 更新病历诊断结果
     * @param id
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void updateRecord(int id, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session= sqlSessionFactory.openSession(false);
        RecordMapper recordMapper = session.getMapper(RecordMapper.class);
        try {
            recordMapper.updateRecord(id, diagnosis);
            session.commit();
            LOGGER.info("update record success!");
            responseMsgHandler.handle(new ResponseMsg<>("update record success!"));
        } catch (Exception e) {
            LOGGER.error("update record failed!");
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
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
            LOGGER.info("delete record success!");
            responseMsgHandler.handle(new ResponseMsg<>("delete record success!"));
        } catch (Exception e) {
            LOGGER.error("delete record failed!", e);
            responseMsgHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
        cache.invalidateAll();
    }
}
