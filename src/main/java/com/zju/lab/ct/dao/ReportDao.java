package com.zju.lab.ct.dao;

import com.google.inject.Inject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.mapper.CTMapper;
import com.zju.lab.ct.model.CTImage;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/24.
 */
@HandlerDao
public class ReportDao {

    private static Logger LOGGER = LoggerFactory.getLogger(ReportDao.class);
    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public ReportDao(Vertx vertx, SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     *
     * @param recordId
     * @param username
     * @param diagnosis
     * @param result
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    private String generateReportFile(int recordId, String username, String diagnosis, List<CTImage> result) throws IOException, DocumentException {
        String fileName = "id_"+recordId+"_username_"+username+".pdf";
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        writer.setStrictImageSequence(true);
        document.open();

        BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
        Font chinese = new Font(bfChinese, 12, Font.NORMAL);
        /*标题*/
        Paragraph paragraph = new Paragraph("CT诊断报表", chinese);
        paragraph.setAlignment(Paragraph.TITLE);
        document.add(paragraph);
        /*病人信息*/
        String info = "病历ID:"+recordId+"  "+"病人姓名:"+username;
        paragraph = new Paragraph(info, chinese);
        paragraph.setAlignment(Paragraph.AUTHOR);
        document.add(paragraph);
        /*病历诊断*/
        if (!StringUtils.isEmpty(diagnosis)){
            paragraph = new Paragraph(diagnosis, chinese);
            paragraph.setAlignment(Paragraph.BODY);
            document.add(paragraph);
        }
        /*CT诊断*/
        for (CTImage ctImage : result){
            Image ct = Image.getInstance("upload/"+ctImage.getFile());
            ct.setAlignment(Image.MIDDLE);
            ct.scaleAbsolute(200,200);//控制图片大小
            document.add(ct);
            paragraph=new Paragraph(ctImage.getDiagnosis(), chinese);
            document.add(paragraph);
        }
        document.close();
        return fileName;
    }

    /**
     * 生成报表操作接口
     * @param recordId
     * @param username
     * @param diagnosis
     * @param responseMsgHandler
     */
    public void report(int recordId, String username, String diagnosis, Handler<ResponseMsg<String>> responseMsgHandler){
        SqlSession session = sqlSessionFactory.openSession();
        CTMapper ctMapper = session.getMapper(CTMapper.class);
        try {
            List<CTImage> ctImages = ctMapper.queryCTDiagnosisNotNull(recordId);
            if (ctImages!=null && !ctImages.isEmpty()){
                String fileName = generateReportFile(recordId,username,diagnosis,ctImages);
                responseMsgHandler.handle(new ResponseMsg<>(fileName));
            }
            else{
                responseMsgHandler.handle(new ResponseMsg<>(HttpCode.NULL_CONTENT, "no available ct record"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
