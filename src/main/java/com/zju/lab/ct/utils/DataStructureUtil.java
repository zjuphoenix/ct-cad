package com.zju.lab.ct.utils;

/**
 * Created by wuhaitao on 2016/4/15.
 */
public class DataStructureUtil {
    public static boolean checkAllZero(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]!=0){
                    return false;
                }
            }
        }
        return true;
    }
}
