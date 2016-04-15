package com.zju.lab.ct.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zju.lab.ct.algorithm.randomforest.RandomForest;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by wuhaitao on 2015/12/10.
 */
public class AppUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

    private static final String CONFIG_NAME = "conf/config.json";
    private static final AppUtil me = new AppUtil();

    private static JsonObject config;

    private AppUtil() {
        try {
            File conf = new File(CONFIG_NAME);
            LOGGER.debug("Initialize configuration from path : {}", conf.getAbsolutePath());
            ObjectMapper mapper = new ObjectMapper();
            config = new JsonObject((Map<String, Object>) mapper.readValue(conf, Map.class));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static String configStr(String key) {
        return config.getString(key);
    }

    public static Integer configInt(String key) {
        return config.getInteger(key);
    }

    public static boolean configBoolean(String key) {
        return config.getBoolean(key);
    }

    public static JDBCClient getJdbcClient(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));

        String username = AppUtil.configStr("db.user");
        if (StringUtils.isNotBlank(username))
            config.put("user", username);

        String password = AppUtil.configStr("db.password");
        if (StringUtils.isNotBlank(password))
            config.put("password", password);

        return JDBCClient.createShared(vertx, config);
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int x = 0xFF & bytes[i];
            chars[i * 2] = HEX_CHARS[x >>> 4];
            chars[1 + i * 2] = HEX_CHARS[0x0F & x];
        }
        return new String(chars);
    }

    public static String computeHash(String password, String salt, String algo) {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            String concat = (salt == null ? "" : salt) + password;
            byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bHash);
        } catch (NoSuchAlgorithmException e) {
            throw new VertxException(e);
        }
    }

    public static String getUploadDir() {
        return AppUtil.configStr("upload.path") == null ?
                BodyHandler.DEFAULT_UPLOADS_DIRECTORY : AppUtil.configStr("upload.path");
        /*try {
            String path = URLDecoder.decode(AppUtil.class.getClassLoader().getResource("webroot").getPath(), "UTF-8");
            return AppUtil.configStr("upload.path") == null ?
                    BodyHandler.DEFAULT_UPLOADS_DIRECTORY : path + File.separator + AppUtil.configStr("upload.path");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
            return BodyHandler.DEFAULT_UPLOADS_DIRECTORY;
        }*/
    }

    public static String getSegmentationDir(){
        return AppUtil.configStr("segmentation");
    }

    public static RandomForest getRandomForestModel() throws IOException, ClassNotFoundException {
        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(config.getString("RandomForest")));
        //读取对象people,反序列化
        RandomForest randomForest = (RandomForest) ois.readObject();
        return randomForest;
    }

    public static RandomForest getLungRandomForestModel() throws IOException, ClassNotFoundException {
        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(config.getString("RandomForest_Lung")));
        //读取对象people,反序列化
        RandomForest randomForest = (RandomForest) ois.readObject();
        return randomForest;
    }

    public static RandomForest getGlobalFeatureRecognitionModel() throws IOException, ClassNotFoundException {
        //实例化ObjectInputStream对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(config.getString("GlobalFeatureRecognition")));
        //读取对象people,反序列化
        RandomForest randomForest = (RandomForest) ois.readObject();
        return randomForest;
    }

}
