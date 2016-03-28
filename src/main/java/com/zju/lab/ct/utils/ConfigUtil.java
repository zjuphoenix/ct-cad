package com.zju.lab.ct.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Created by wuhaitao on 2016/3/27.
 */
public class ConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    public static JsonObject getLiverLesion() throws IOException {
        URL url = ConfigUtil.class.getClassLoader().getResource(Constants.LESION);
        LOGGER.debug("Initialize liver lesion type from path : {}", url);
        ObjectMapper mapper = new ObjectMapper();
        return new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
    }

    public static JsonObject getLungLesion() throws IOException {
        URL url = ConfigUtil.class.getClassLoader().getResource(Constants.LUNG);
        LOGGER.debug("Initialize lung lesion type from path : {}", url);
        ObjectMapper mapper = new ObjectMapper();
        return new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class));
    }
}
