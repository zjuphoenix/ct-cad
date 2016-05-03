package com.zju.lab.ct.algorithm.randomforest;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.RandomForestModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wuhaitao
 * @date 2016/5/2 23:17
 */
public class RandomForestDecorator {
    private RandomForestModel randomForestModel;

    public RandomForestDecorator(RandomForestModel randomForestModel) {
        this.randomForestModel = randomForestModel;
    }

    public int predictType(double[] sample){
        double label = randomForestModel.predict(Vectors.dense(sample))+1;
        return (int)label;
    }

    public Map<Integer, Integer> predictResult(double[] sample){
        Map<Integer,Integer> res = new HashMap<>(8);
        Vector vector = Vectors.dense(sample);
        double label = 0;
        int type = 0;
        DecisionTreeModel[] decisionTreeModels = randomForestModel.trees();
        for (DecisionTreeModel decisionTreeModel : decisionTreeModels){
            label = decisionTreeModel.predict(vector)+1;
            type = (int)label;
            if (res.containsKey(type)){
                res.replace(type, res.get(type)+1);
            }
            else{
                res.put(type, 1);
            }
        }
        return res;
    }

    public int getTreeNum() {
        return randomForestModel.numTrees();
    }
}
