package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/2/28.
 */
public class CTEntity {
    private String id;
    private String des;
    private String date;

    public CTEntity(String id, String des, String date) {
        this.id = id;
        this.des = des;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
