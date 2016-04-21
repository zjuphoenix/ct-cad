package com.zju.lab.ct.mapper;

import com.zju.lab.ct.model.Record;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public interface RecordMapper {
    List<Record> queryRecords(@Param("username") String username, @Param("offset") int offset, @Param("rowCount") int rowCount) throws Exception;
    void addRecord(Record record) throws Exception;
    void updateRecord(@Param("id") int id, @Param("diagnosis") String diagnosis) throws Exception;
    void deleteRecord(@Param("id") int id) throws Exception;
    int queryRecordsCount() throws Exception;
    int queryRecordsCountByUsername(@Param("username") String username) throws Exception;
}
