package com.zju.lab.ct.mapper;

import com.zju.lab.ct.model.CTImage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public interface CTMapper {
    List<CTImage> queryCTs(@Param("recordId") int recordId, @Param("offset") int offset, @Param("rowCount") int rowCount)  throws Exception;
    List<CTImage> queryAllCTsByRecordId(@Param("recordId") int recordId)  throws Exception;
    CTImage queryCTById(@Param("id") int id) throws Exception;
    List<String> queryCTFileByRecordId(@Param("recordId") int recordId) throws Exception;
    List<CTImage> queryCancerCT(@Param("recordId") int recordId) throws Exception;
    List<CTImage> queryCTDiagnosisNotNull(@Param("recordId") int recordId) throws Exception;
    void addCT(CTImage ctImage) throws Exception;
    void updateCTDiagnosis(@Param("id") int id, @Param("diagnosis") String diagnosis) throws Exception;
    void updateCTRecognition(@Param("id") int id, @Param("recognition") int recognition) throws Exception;
    void deleteCTsByRecordId(@Param("recordId") int recordId) throws Exception;
    void deleteCTById(@Param("id") int id) throws Exception;
    int queryCTCountByRecordId(@Param("recordId") int recordId) throws Exception;
}
