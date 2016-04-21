package com.zju.lab.ct.model;

import java.util.DoubleSummaryStatistics;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public class Feature {
    private double f1;
    private double f2;
    private double f3;
    private double f4;
    private double f5;
    private double f6;
    private double f7;
    private double f8;
    private double f9;
    private double f10;
    private double f11;
    private double f12;
    private double f13;
    private double f14;
    private double f15;
    private double f16;
    private double f17;
    private double f18;
    private double f19;
    private double f20;
    private double f21;
    private double f22;
    private double f23;
    private double f24;
    private double f25;
    private double f26;
    private int label;

    public Feature() {
    }

    public Feature(double[] feature, int label) {
        f1 = feature[0];
        f2 = feature[1];
        f3 = feature[2];
        f4 = feature[3];
        f5 = feature[4];
        f6 = feature[5];
        f7 = feature[6];
        f8 = feature[7];
        f9 = feature[8];
        f10 = feature[9];
        f11 = feature[10];
        f12 = feature[11];
        f13 = feature[12];
        f14 = feature[13];
        f15 = feature[14];
        f16 = feature[15];
        f17 = feature[16];
        f18 = feature[17];
        f19 = feature[18];
        f20 = feature[19];
        f21 = feature[20];
        f22 = feature[21];
        f23 = feature[22];
        f24 = feature[23];
        f25 = feature[24];
        f26 = feature[25];
        this.label = label;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public double getF5() {
        return f5;
    }

    public void setF5(double f5) {
        this.f5 = f5;
    }

    public double getF6() {
        return f6;
    }

    public void setF6(double f6) {
        this.f6 = f6;
    }

    public double getF7() {
        return f7;
    }

    public void setF7(double f7) {
        this.f7 = f7;
    }

    public double getF8() {
        return f8;
    }

    public void setF8(double f8) {
        this.f8 = f8;
    }

    public double getF9() {
        return f9;
    }

    public void setF9(double f9) {
        this.f9 = f9;
    }

    public double getF10() {
        return f10;
    }

    public void setF10(double f10) {
        this.f10 = f10;
    }

    public double getF11() {
        return f11;
    }

    public void setF11(double f11) {
        this.f11 = f11;
    }

    public double getF12() {
        return f12;
    }

    public void setF12(double f12) {
        this.f12 = f12;
    }

    public double getF13() {
        return f13;
    }

    public void setF13(double f13) {
        this.f13 = f13;
    }

    public double getF14() {
        return f14;
    }

    public void setF14(double f14) {
        this.f14 = f14;
    }

    public double getF15() {
        return f15;
    }

    public void setF15(double f15) {
        this.f15 = f15;
    }

    public double getF16() {
        return f16;
    }

    public void setF16(double f16) {
        this.f16 = f16;
    }

    public double getF17() {
        return f17;
    }

    public void setF17(double f17) {
        this.f17 = f17;
    }

    public double getF18() {
        return f18;
    }

    public void setF18(double f18) {
        this.f18 = f18;
    }

    public double getF19() {
        return f19;
    }

    public void setF19(double f19) {
        this.f19 = f19;
    }

    public double getF20() {
        return f20;
    }

    public void setF20(double f20) {
        this.f20 = f20;
    }

    public double getF21() {
        return f21;
    }

    public void setF21(double f21) {
        this.f21 = f21;
    }

    public double getF22() {
        return f22;
    }

    public void setF22(double f22) {
        this.f22 = f22;
    }

    public double getF23() {
        return f23;
    }

    public void setF23(double f23) {
        this.f23 = f23;
    }

    public double getF24() {
        return f24;
    }

    public void setF24(double f24) {
        this.f24 = f24;
    }

    public double getF25() {
        return f25;
    }

    public void setF25(double f25) {
        this.f25 = f25;
    }

    public double getF26() {
        return f26;
    }

    public void setF26(double f26) {
        this.f26 = f26;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public Double[] featureVector(){
        Double[] sample = new Double[]{f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,(double)label};
        return sample;
    }
}
