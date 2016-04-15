package com.zju.lab.ct.algorithm.randomforest;

import java.io.Serializable;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class TreeNode implements Serializable {
    private static long serialVersionUID = -928043976301546560L;

    int feature;
    double splitValue;
    public TreeNode left;
    public TreeNode right;
    int type = -1;

    @Override
    public String toString() {
        return "TreeNode{" +
                "feature=" + feature +
                ", splitValue=" + splitValue +
                '}';
    }
}
