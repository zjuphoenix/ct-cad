package com.zju.lab.ct.algorithm.randomforest;

import java.io.Serializable;
import java.util.*;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class RandomForest implements Serializable{

    private static long serialVersionUID = -928033976301546560L;
    private int treeNum;
    private int typeNum;
    private List<CARTTree> trees;

    public RandomForest(int treeNum, int typeNum) {
        this.treeNum = treeNum;
        this.typeNum = typeNum;
        trees = new ArrayList<>(treeNum);
    }

    public void createForest(List<Double[]> dataSet){
        int size = dataSet.size();
        Random r = new Random();
        for (int i = 0; i < treeNum; i++) {
            List<Double[]> sampleSet = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                sampleSet.add(dataSet.get(r.nextInt(size)));
            }
            CARTTree tree = new CARTTree(sampleSet);
            tree.createTree();
            trees.add(tree);
        }
    }

    public int predictType(double[] sample){
        Map<Integer,Integer> res = new HashMap<>(typeNum);
        trees.forEach(tree -> {
            int type = tree.predictType(sample);
            if (res.containsKey(type)){
                res.replace(type, res.get(type)+1);
            }
            else{
                res.put(type, 1);
            }
        });
        int predict = 0;
        int max = -1;
        for (Map.Entry<Integer, Integer> entry: res.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (value>max){
                max = value;
                predict = key;
            }
        }
        return predict;
    }

    public Map<Integer, Integer> predictResult(double[] sample){
        Map<Integer,Integer> res = new HashMap<>(typeNum);
        trees.forEach(tree -> {
            int type = tree.predictType(sample);
            if (res.containsKey(type)){
                res.replace(type, res.get(type)+1);
            }
            else{
                res.put(type, 1);
            }
        });
        return res;
    }

    public int getTreeNum() {
        return treeNum;
    }
}
