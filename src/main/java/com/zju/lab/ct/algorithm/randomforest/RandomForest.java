package com.zju.lab.ct.algorithm.randomforest;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by wuhaitao on 2016/2/23.
 */
public class RandomForest implements Serializable{

    private static long serialVersionUID = -928133976301546560L;
    private int treeNum;
    private int typeNum;
    private List<CARTTree> trees;

    public RandomForest(int treeNum, int typeNum) {
        this.treeNum = treeNum;
        this.typeNum = typeNum;
        trees = new ArrayList<>(treeNum);
    }

    public void createForest(List<Double[]> dataSet) throws InterruptedException, ExecutionException {
        int size = dataSet.size();
        ExecutorService executor = Executors.newFixedThreadPool(size);
        List<Callable<CARTTree>> tasks = new ArrayList<>(size);
        for (int i = 0; i < treeNum; i++) {
            tasks.add(new Callable<CARTTree>() {

                @Override
                public CARTTree call() throws Exception {
                    Random r = new Random();
                    List<Double[]> sampleSet = new ArrayList<>(size);
                    for (int j = 0; j < size; j++) {
                        sampleSet.add(dataSet.get(r.nextInt(size)));
                    }
                    CARTTree tree = new CARTTree(sampleSet);
                    tree.createTree();
                    return tree;
                }
            });
        }
        List<Future<CARTTree>> futures = executor.invokeAll(tasks);
        for (Future<CARTTree> future : futures){
            trees.add(future.get());
        }
        executor.shutdown();
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

    public List<CARTTree> getTrees() {
        return trees;
    }
}
