package com.zju.lab.ct.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by wuhaitao on 2016/2/28.
 */
public class FileUtil {
    public static String[] getCTNumbers() throws UnsupportedEncodingException {
        URL url = FileUtil.class.getClassLoader().getResource("webroot/ctimage");
        String fileName = URLDecoder.decode(url.getFile(), "UTF-8");
        File root = new File(fileName);
        String[] listNumber = root.list();
        return listNumber;
    }

    public static String[] findCTById(String id) throws UnsupportedEncodingException {
        URL url = FileUtil.class.getClassLoader().getResource("webroot/ctimage");
        String fileName = URLDecoder.decode(url.getFile(), "UTF-8");
        String fileDir = fileName+File.separator+id+File.separator+"2";
        File dir = new File(fileDir);
        File[] files = dir.listFiles();
        String[] cts = new String[files.length];
        String str = files[0].getPath();
        int index = str.indexOf("ctimage");
        for (int i = 0; i < files.length; i++) {
            cts[i] = files[i].getPath().substring(index);
        }
        return cts;
    }
}
