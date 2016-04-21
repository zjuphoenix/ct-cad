package com.zju.lab.ct.model;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public class UserDto {
    private String USERNAME;
    private String ROLE;

    public UserDto() {
    }

    public UserDto(String USERNAME, String ROLE) {
        this.USERNAME = USERNAME;
        this.ROLE = ROLE;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getROLE() {
        return ROLE;
    }

    public void setROLE(String ROLE) {
        this.ROLE = ROLE;
    }
}
