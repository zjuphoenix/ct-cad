package com.zju.lab.ct.utils;

/**
 * Created by wuhaitao on 2016/3/21.
 */
public class RoleMap {
    private static String[] roles = new String[]{"admin", "doctor", "patient"};

    /**
     * 根据编号获取用户类型映射
     * @param role
     * @return
     * @throws Exception
     */
    public static String getRole(int role) throws Exception{
        if (role >= roles.length || role < 0)
            throw new ArrayIndexOutOfBoundsException();
        return roles[role];
    }
}
