package com.zju.lab.ct.algorithm.randomforest;

import java.io.Serializable;
import java.util.*;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class CARTTree implements Serializable{

    private static long serialVersionUID = -928033976301556560L;

    private int featureNum;
    private List<Double[]> dataSet;
    private TreeNode decisionTree;

    public CARTTree(List<Double[]> dataSet) {
        this.dataSet = dataSet;
        this.featureNum = dataSet.get(0).length - 1;
    }

    public void createTree() {
        decisionTree = createNode(dataSet);
    }

    private TreeNode createNode(List<Double[]> samples) {
        TreeNode treeNode = new TreeNode();
        int desc = -1;
        boolean isLeaf = true;
        for (Double[] sample : samples){
            if (desc == -1){
                desc = sample[featureNum].intValue();
            }
            else if(desc != sample[featureNum].intValue()){
                isLeaf = false;
            }
        }
        if (isLeaf){
            treeNode.type = desc;
            return treeNode;
        }
        int bestFeatureIndex = 0;
        double bestSplitValue = 0;
        double bestGini = Double.MIN_VALUE;
        List<Double[]> samples1 = null;
        List<Double[]> samples2 = null;

        Random random = new Random();
        int r = 0;
        boolean[] flag = new boolean[featureNum];
        for (int i = 0; i < 6; i++) {
            do {
               r = random.nextInt(featureNum);
            }while(flag[r]);
            flag[r] = true;
        }
        //遍历每个特征属性
        for (int i = 0; i < featureNum; i++) {
            if (flag[i]) {
                Set<Double> set = new HashSet<>(samples.size());
                int size = samples.size();
                //遍历该特征属性每个特征值
                for (int j = 0; j < size; j++) {
                    if (!set.contains(samples.get(j)[i])) {
                        set.add(samples.get(j)[i]);
                    }
                    List<Double[]> leftSamples = new ArrayList<>(samples.size());
                    List<Double[]> rightSamples = new ArrayList<>(samples.size());
                    for (Double[] sample2 : samples) {
                        if (sample2[i] <= samples.get(j)[i]) {
                            leftSamples.add(sample2);
                        } else {
                            rightSamples.add(sample2);
                        }
                    }
                    Map<Integer, Integer> map1 = new HashMap<>(featureNum);
                    Map<Integer, Integer> map2 = new HashMap<>(featureNum);
                    int total1 = leftSamples.size();
                    int total2 = rightSamples.size();
                    double gini = 0;
                    int type = 0;
                    for (Double[] sample : leftSamples) {
                        type = (int) sample[featureNum].doubleValue();
                        if (map1.containsKey(type)) {
                            map1.replace(type, map1.get(type) + 1);
                        } else {
                            map1.put(type, 1);
                        }
                    }
                    for (Map.Entry<Integer, Integer> entry : map1.entrySet()) {
                        int value = entry.getValue();
                        gini += Math.pow(value * 1.0 / total1, 2);
                    }

                    for (Double[] sample : rightSamples) {
                        type = (int) sample[featureNum].doubleValue();
                        if (map2.containsKey(type)) {
                            map2.replace(type, map2.get(type) + 1);
                        } else {
                            map2.put(type, 1);
                        }
                    }
                    for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
                        int value = entry.getValue();
                        gini += Math.pow(value * 1.0 / total2, 2);
                    }

                    if (gini > bestGini) {
                        bestGini = gini;
                        bestFeatureIndex = i;
                        bestSplitValue = samples.get(j)[i];
                        samples1 = leftSamples;
                        samples2 = rightSamples;
                    }
                }
            }
        }

        treeNode.feature = bestFeatureIndex;
        treeNode.splitValue = bestSplitValue;
        treeNode.left = createNode(samples1);
        treeNode.right = createNode(samples2);
        return treeNode;
    }

    public int predictType(double[] sample){
        return predictType(decisionTree, sample);
    }
    private int predictType(TreeNode treeNode, double[] sample){
        if(treeNode.type != -1){
            return treeNode.type;
        }
        if (sample[treeNode.feature] <= treeNode.splitValue){
            return predictType(treeNode.left, sample);
        }
        else{
            return predictType(treeNode.right, sample);
        }
    }

    public TreeNode getDecisionTree() {
        return decisionTree;
    }

    @Override
    public String toString() {
        return "CARTTree{" +
                "featureNum=" + featureNum +
                ", dataSet=" + dataSet +
                ", decisionTree=" + decisionTree +
                '}';
    }
}
