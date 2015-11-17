package com.ysy.sweepmeasure.sweep;

import com.ysy.sweepmeasure.util.FFT;

import java.util.Arrays;

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
    private double[] hinvmic;

    private DataListener dataListener;

    public interface DataListener {
        void dataChange(double[] data);

        void deconvData(double[] data);

        void hinv0Data(double[] data);
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public Sweep(int fs, int f1, int f2, double T, int A, double[] hinvmic) {
        this.fs = fs;
        this.f1 = f1;
        this.f2 = f2;
        this.T = T;
        this.A = A;
        this.hinvmic = hinvmic;
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
        System.out.println("hinvmic.length=" + hinvmic.length);
        double[] hinv0 = new double[hinvmic.length + t - 1];
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
        }
        for (int i = 0; i < t; i++) {
            y[i] = x[t - i - 1] * G[i] / 32767.0;

        }
        hinv0 = OverlapSaveConv(y, 1024);
        if (dataListener != null) {
            dataListener.dataChange(x);
            dataListener.deconvData(y);
            dataListener.hinv0Data(hinv0);
        }
    }

    private double[] OverlapSaveConv(double[] x, int L) {
        int Lh = hinvmic.length;
        int Lx = x.length;
        int Ly = Lh + Lx - 1;
        int Lt = Lh - 1 + L;
        int NFFT = (int) Math.pow(2,
                Math.ceil(Math.log10(Math.max((double) Lh, Lh - 1 + L)) / Math.log10(2.0)));
        int len2 = NFFT / 2;
        int Fn = (int) Math.floor((double) Ly / L);
        int delNx = Lx - Fn * L;
        int delNy = Ly - Fn * L;

        double t;
        double[] ht = new double[NFFT];
        double[] yt = new double[NFFT];
        double[] temp = new double[Lt];
        double[] y = new double[Ly];
        Arrays.fill(ht, 0.0);
        Arrays.fill(yt, 0.0);
        Arrays.fill(temp, 0.0);
        Arrays.fill(y, 0.0);
        System.arraycopy(x, 0, y, 0, Lx);
        System.arraycopy(hinvmic, 0, ht, 0, Lh);
        FFT.rfft(ht, NFFT);
        for (int i = 0; i < Fn; i++) {
            System.arraycopy(y, i * L, temp, Lh - 1, L);
            System.arraycopy(temp, 0, yt, 0, Lt);
            FFT.rfft(yt, NFFT);
            yt[0] = yt[0] * ht[0];
            yt[len2] = yt[len2] * ht[len2];
            for (int j = 1; j < len2; j++) {
                t = yt[j] * ht[j] - yt[NFFT - j] * ht[NFFT - j];
                yt[NFFT - j] = yt[j] * ht[NFFT - j] + yt[NFFT - j] * ht[j];
                yt[j] = t;
            }
            FFT.irfft(yt, NFFT);
            System.arraycopy(yt, Lh - 1, y, i * L, L);
            Arrays.fill(yt, 0.0);
            System.arraycopy(temp, L, temp, 0, Lh - 1);
        }
        if (delNy > 0) {
            if (delNx > 0) {
                System.arraycopy(y, Fn * L, temp, Lh - 1, delNx);
                System.arraycopy(temp, 0, yt, 0, Lh - 1 + delNx);
            } else {
                System.arraycopy(temp, 0, yt, 0, Lh - 1);
            }
            FFT.rfft(yt, NFFT);
            yt[0] = yt[0] * ht[0];
            yt[len2] = yt[len2] * ht[len2];
            for (int j = 1; j < len2; j++) {
                t = yt[j] * ht[j] - yt[NFFT - j] * ht[NFFT - j];
                yt[NFFT - j] = yt[j] * ht[NFFT - j] + yt[NFFT - j] * ht[j];
                yt[j] = t;
            }
            FFT.irfft(yt, NFFT);
            System.arraycopy(yt, Lh - 1, y, Fn * L, delNy);
        }
        return y;
    }
}
