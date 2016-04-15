package com.zju.lab.ct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Created by wuhaitao on 2016/4/13.
 */
public class FileCopyTest {
    public static void forTransfer(String file1, String file2) throws Exception{
        int length=2097152;
        File f1 = new File(file1);
        File f2 = new File(file2);
        f2.createNewFile();
        FileInputStream in=new FileInputStream(f1);
        FileOutputStream out=new FileOutputStream(f2);
        FileChannel inC=in.getChannel();
        FileChannel outC=out.getChannel();
        while(true){
            if(inC.position()==inC.size()){
                inC.close();
                outC.close();
                break;
            }
            if((inC.size()-inC.position())<20971520)
                length=(int)(inC.size()-inC.position());
            else
                length=20971520;
            inC.transferTo(inC.position(),length,outC);
            inC.position(inC.position()+length);
        }
    }
    public static void main(String[] args) throws Exception {
        String[] normal = new String[]{
                "E:/graduation/data/Zhang_Lian_Mu",
                "E:/graduation/data/Chen_Xiao_Bo",
                "E:/graduation/data/Zhou_Guo_Ling",
                "E:/graduation/data/Zhao_Mei"
        };
        String[] cancer = new String[]{
                "E:/graduation/data/Hu_Yao_Zhen",
                "E:/graduation/data/Lu_Sheng_Gao",
                "E:/graduation/data/Shi_Quan_Ping",
                "E:/graduation/data/Wu_Yong_Hang",
                "E:/graduation/data/Xiong_Shi_Wen",
                /*"E:/graduation/data/Xu_Song_Yin",*/
                /*"E:/graduation/data/Yu_A_Xian",*/
                /*"E:/graduation/data/Yu_Miao_Fu",*/
                "E:/graduation/data/Zhou_Jun",
                "E:/graduation/data/Zhu_Jian_Guo",
                "E:/graduation/data/Yuan_Chao_Ming",
                "E:/graduation/data/Pan_Fu_Tang"
        };
        int i = 0;
        String normalDir = "E:/graduation/train/normal/";
        String cancerDir = "E:/graduation/train/cancer/";
        /*for (String f : normal){
            File dir = new File(f);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("seg.jpg");
                }
            });
            for (File file : files){
                forTransfer(file.getAbsolutePath(), normalDir+i+".jpg");
                i++;
            }
        }*/

        i = 0;
        for (String f : cancer){
            File dir = new File(f);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("seg.jpg");
                }
            });
            for (File file : files){
                forTransfer(file.getAbsolutePath(), cancerDir+i+".jpg");
                i++;
            }
        }
    }
}
