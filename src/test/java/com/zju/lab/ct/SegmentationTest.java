package com.zju.lab.ct;

import com.zju.lab.ct.algorithm.segmentation.RegionGrowing;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/30.
 */
public class SegmentationTest {
    private static Logger LOGGER = LoggerFactory.getLogger(SegmentationTest.class);

    //@Test
    public void test() throws IOException {
        RegionGrowing.getSegmentationImage("upload/1b5e6e28-fc1d-4e74-80da-7a7779f11f45",100,250,20);
    }

    void func(Integer k){
        k = 3;
    }
    void func2(List<Integer> list){
        /*list.clear();*/
        list = new ArrayList<>();
    }

    @Test
    public void test2() throws IOException {
        Integer i = 2;
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        func(i);
        func2(list);
        System.out.println(i);
        System.out.println(list.size());
    }

}
