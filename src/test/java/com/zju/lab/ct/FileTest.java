package com.zju.lab.ct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by wuhaitao on 2016/4/13.
 */
public class FileTest {
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
        int i = 376;
        String normalDir = "E:/graduation/train/normal/";
        File dir = new File("E:/graduation/train/extra");
        File[] files = dir.listFiles();
        for (File file : files){
            forTransfer(file.getAbsolutePath(), normalDir+i+".jpg");
            i++;
        }
    }
}
