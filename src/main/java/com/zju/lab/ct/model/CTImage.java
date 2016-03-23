package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/3/10.
 */
public class CTImage {
    private int id;
    private String type;
    private String file;
    private String diagnosis;
    private int recordId;

    public CTImage() {
    }

    public CTImage(int id, String type, String file, String diagnosis, int recordId) {
        this.id = id;
        this.type = type;
        this.file = file;
        this.diagnosis = diagnosis;
        this.recordId = recordId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
}
