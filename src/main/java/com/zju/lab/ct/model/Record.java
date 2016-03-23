package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/3/23.
 */
public class Record {
    private int id;
    private String diagnosis;
    private String username;

    public Record(int id, String diagnosis, String username) {
        this.id = id;
        this.diagnosis = diagnosis;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
