package com.zju.lab.ct.algorithm.randomforest;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class TreeNode {
    int feature;
    double splitValue;
    TreeNode left;
    TreeNode right;
    int type = -1;
}
