package com.ysy.sweepmeasure.sweep;

/**
 * User: ysy
 * Date: 2015/8/19
 */
public class Sweep {

    private int fs;
    private int f1;
    private int f2;
    private double T;
    private int A;

    private DataListener dataListener;

    public interface DataListener {
        void dataChange(double data);
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public Sweep(int fs, int f1, int f2, double T, int A) {
        this.fs = fs;
        this.f1 = f1;
        this.f2 = f2;
        this.T = T;
        this.A = A;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public double getFs() {
        return this.fs;
    }

    public void setF1(int f1) {
        this.f1 = f1;
    }

    public double getF1() {
        return this.f1;
    }

    public void setF2(int f2) {
        this.f2 = f2;
    }

    public double getF2() {
        return this.f2;
    }

    public void setT(double T) {
        this.T = T;
    }

    public double getT() {
        return this.T;
    }

    public void setA(int A) {
        this.A = A;
    }

    public double getA() {
        return this.A;
    }

    public void GenerateWave(double[] x, double[] y) {
        final int t = (int) (fs * T);
        double[] x0 = new double[t];
        double[] G = new double[t];
        double ti, xmax = 0.0;
        for (int i = 0; i < t; i++) {
            ti = (double) i / (double) fs;
            x0[i] = Math.sin(2.0 * Math.PI * f1 * T * (Math.exp(Math.log(f2 / f1) * ti / T) - 1.0) / (Math.log(f2 / f1)));
            G[i] = Math.pow(10.0, (-0.3 * (Math.log(Math.exp(Math.log(f2 / f1) * ti / T)) / Math.log(2))));
        }

        for (int i = 0; i < t; i++) {
            if (Math.abs(x0[i]) >= xmax) xmax = Math.abs(x0[i]);
        }

        for (int i = 0; i < t; i++) {
            x[i] = x0[i] * A / (xmax);
            dataListener.dataChange(x[i]);
        }
        for (int i = 0; i < t; i++) {
            y[i] = x[t - i - 1] * G[i];
        }
    }
}
