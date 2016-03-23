package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/3/9.
 */
public class Consultation {
    private int id;
    private String created;
    private String record;
    private String updated;

    public Consultation() {
    }

    public Consultation(int id) {
        this.id = id;
    }

    public Consultation(int id, String created, String record, String updated) {
        this.id = id;
        this.created = created;
        this.record = record;
        this.updated = updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
