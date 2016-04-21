package com.zju.lab.ct.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.cache.PageKey;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.mapper.RecordMapper;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.Record;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wuhaitao on 2016/3/23.
 */
@HandlerDao
public class RecordsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsDao.class);

    private LoadingCache<PageKey, List<JsonObject>> cache = null;

    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public RecordsDao(SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
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
            /*List<JsonObject> records = cache.get(pageKey);*/
            SqlSession session= sqlSessionFactory.openSession();
            RecordMapper recordMapper = session.getMapper(RecordMapper.class);
            List<Record> records = recordMapper.queryRecords(null, (pageIndex-1)*pageSize, pageSize);
            /*List<JsonObject> result = records.stream().flatMap(record -> {
                JsonObject obj = new JsonObject();
                obj.put("id", record.getId());
                obj.put("diagnosis", record.getDiagnosis());
                obj.put("username", record.getUsername());
                return Stream.of(obj);
            }).collect(Collectors.toList());*/
            int count = recordMapper.queryRecordsCount();
            JsonObject res = new JsonObject();
            res.put("records", records);
            res.put("count", count);
            recordsHandler.handle(new ResponseMsg<>(res));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            recordsHandler.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
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
            /*List<JsonObject> records = cache.get(pageKey);*/
            /*int count = DBUtil.getRecordCount(username);*/
            SqlSession session= sqlSessionFactory.openSession();
            RecordMapper recordMapper = session.getMapper(RecordMapper.class);
            List<Record> records = recordMapper.queryRecords(username, (pageIndex-1)*pageSize, pageSize);
            int count = recordMapper.queryRecordsCountByUsername(username);
            JsonObject res = new JsonObject();
            res.put("records", records);
            res.put("count", count);
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
