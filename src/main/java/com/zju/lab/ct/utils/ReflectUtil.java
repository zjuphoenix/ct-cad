package com.zju.lab.ct.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by wuhaitao on 2016/3/9.
 */
public class ReflectUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class);

    public static boolean hasParams(Class<?> clazz, Class<?> paramType){
        try {
            Constructor constructor = clazz.getConstructor(paramType);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
