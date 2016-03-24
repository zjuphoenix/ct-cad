package com.zju.lab.ct;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wuhaitao on 2016/3/24.
 */
public class PDFTest {

    @Before
    public void setup(){

    }

    @Test
    public void test() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Helloworld.pdf"));
        writer.setStrictImageSequence(true);
        document.open();

        BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
        Font chinese = new Font(bfChinese, 12, Font.NORMAL);

        Paragraph paragraph = new Paragraph("CT诊断报表", chinese);
        paragraph.setAlignment(Paragraph.TITLE);
        document.add(paragraph);

        /*病人信息*/
        String info = "病历ID:1  病人姓名:zhangsan";
        paragraph = new Paragraph(info, chinese);
        paragraph.setAlignment(Paragraph.AUTHOR);
        document.add(paragraph);

        /*病历诊断*/
        paragraph = new Paragraph("肝表面欠光整，肝各叶比例失调，肝裂增宽，肝内见多发低密度结节灶，右肝为主，较大一枚直径约为82mm，病灶内以囊性成分为主，增强扫描动脉期病灶内可见不规则强化，门静脉期及延迟期强化减退，呈低密度灶，门静脉显示清晰，内未见明显充盈缺损，肝门部结构清晰。胆囊未见增大，壁水肿增厚，腔内未见异常密度灶；胆总管未见明显扩张；脾脏增大，大于5个肋单元；胰腺结构显示清晰，胰管未见明显扩张，强化后胰腺实质均匀强化。后腹膜未见明显肿大淋巴结影。肝右、肝中静脉显影浅淡，胃底，脾门处见迂曲血管团影。", chinese);
        paragraph.setAlignment(Paragraph.BODY);
        document.add(paragraph);


        Image ct1 = Image.getInstance("upload/2c62ab0c-1cda-49b2-8032-dbe40bece027");
        ct1.setAlignment(Image.MIDDLE);
        ct1.scaleAbsolute(200,200);//控制图片大小
        /*ct1.setAbsolutePosition(0,20);//控制图片位置*/
        document.add(ct1);
        paragraph=new Paragraph("右肝后下段占位，肝癌考虑。肝硬化、脾大、腹水。", chinese);
        document.add(paragraph);

        Image ct2 = Image.getInstance("upload/a85b4ce5-ca23-4fff-a67e-5e9ce838954a");
        ct2.setAlignment(Image.MIDDLE);
        ct2.scaleAbsolute(200,200);//控制图片大小
        document.add(ct2);
        paragraph=new Paragraph("右肝癌伴子灶形成首先考虑。  肝硬化、脾大、腹水；胆囊水肿。  后腹膜多发淋巴结肿大。  胃窦部小结节，息肉待排，请结合其他检查。", chinese);
        document.add(paragraph);

        Image ct3 = Image.getInstance("upload/e8c06fb5-ad6b-491d-816c-9e910645cc7d");
        ct3.setAlignment(Image.MIDDLE);
        ct3.scaleAbsolute(200,200);//控制图片大小
        document.add(ct3);
        paragraph=new Paragraph("肝内多发结节，肝癌伴多发子灶形成考虑。  肝硬化，脾大，胆囊壁水肿，门脉高压伴侧枝循环开放。 ", chinese);
        document.add(paragraph);

        document.close();
    }

    @After
    public void destroy(){

    }
}
