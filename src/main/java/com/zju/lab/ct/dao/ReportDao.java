package com.zju.lab.ct.dao;

import com.google.inject.Inject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.apache.commons.lang3.StringUtils;
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
    protected JDBCClient sqlite = null;

    @Inject
    public ReportDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "report");
    }

    /**
     * 生成报表函数
     * @param recordId
     * @param username
     * @param diagnosis
     * @param result
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    private String generateReportFile(int recordId, String username, String diagnosis, List<JsonObject> result) throws IOException, DocumentException {
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
        for (JsonObject obj : result){
            Image ct = Image.getInstance("upload/"+obj.getString("file"));
            ct.setAlignment(Image.MIDDLE);
            ct.scaleAbsolute(200,200);//控制图片大小
            document.add(ct);
            paragraph=new Paragraph(obj.getString("diagnosis"), chinese);
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
        sqlite.getConnection(sqlConnectionAsyncResult -> {
            if (sqlConnectionAsyncResult.succeeded()){
                SQLConnection sqlConnection = sqlConnectionAsyncResult.result();
                String sql = "select * from ct where recordId = ? and diagnosis is not null and diagnosis is not ''";
                sqlConnection.queryWithParams(sql, new JsonArray().add(recordId), resultSetAsyncResult -> {
                    if (resultSetAsyncResult.succeeded()){
                        ResultSet resultSet = resultSetAsyncResult.result();
                        List<JsonObject> result = resultSet.getRows();
                        if (!result.isEmpty()){
                            try {
                                String fileName = generateReportFile(recordId,username,diagnosis,result);
                                responseMsgHandler.handle(new ResponseMsg<String>(fileName));
                            } catch (IOException e) {
                                LOGGER.error(e.getMessage(), e);
                                responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
                            } catch (DocumentException e) {
                                LOGGER.error(e.getMessage(), e);
                                responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
                            }
                        }
                        else{
                            responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.NULL_CONTENT, "no available ct record"));
                        }
                    }
                    else{
                        responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, resultSetAsyncResult.cause().getMessage()));
                    }
                });
            }
            else{
                responseMsgHandler.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, sqlConnectionAsyncResult.cause().getMessage()));
            }
        });
    }
}
