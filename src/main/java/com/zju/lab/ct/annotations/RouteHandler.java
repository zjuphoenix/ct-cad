package com.zju.lab.ct.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wuhaitao on 2015/12/10.
 * 标识需要使用的Handler，扫描时会扫描包含该注解的handler，不加注解则不会被扫描到
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteHandler {
    String value() default "";
}
