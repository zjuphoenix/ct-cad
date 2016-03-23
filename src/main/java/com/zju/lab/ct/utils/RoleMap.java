package com.zju.lab.ct.utils;

/**
 * Created by wuhaitao on 2016/3/21.
 */
public class RoleMap {
    private static String[] roles = new String[]{"admin", "doctor", "patient"};

    public static String getRole(int role) throws Exception{
        if (role >= roles.length || role < 0)
            throw new ArrayIndexOutOfBoundsException();
        return roles[role];
    }
}
