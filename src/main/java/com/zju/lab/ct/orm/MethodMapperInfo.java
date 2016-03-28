package com.zju.lab.ct.orm;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/25.
 */
public class MethodMapperInfo {
    private Annotation annotation;
    private String value;
    private List<String> params;

    public MethodMapperInfo(Annotation annotation, String value, List<String> params) {
        this.annotation = annotation;
        this.value = value;
        this.params = params;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
