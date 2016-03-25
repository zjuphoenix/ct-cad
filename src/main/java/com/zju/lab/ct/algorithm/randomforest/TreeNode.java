package com.zju.lab.ct.algorithm.randomforest;

import java.io.Serializable;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class TreeNode implements Serializable {
    int feature;
    double splitValue;
    TreeNode left;
    TreeNode right;
    int type = -1;
}
