package com.ggec.graphicalfir;

import java.util.Arrays;

/**
 * User: ysy
 * Date: 2015/11/24
 * Time: 10:56
 */
public class DBTool {

    public static double[] FilterFreq(double fc, double fb, double gain) {

        double[] result;
        double pi = Math.PI;
        double fs = 44100.0;
        double K = Math.tan(pi * fc / fs);
        double Q = fc / fb;
        double V0, temp, temp1, temp2;
        double[] numerator = new double[3];
        double[] denominator = new double[3];

        if (gain >= 0) {
            V0 = Math.pow(10.0, gain / 20.0);
            temp1 = V0 / Q;
            temp2 = 1.0 / Q;
        } else {
            V0 = Math.pow(10., -gain / 20.0);
            temp1 = 1.0 / Q;
            temp2 = V0 / Q;
        }

        temp = 1.0 + temp2 * K + K * K;
        numerator[0] = (1.0 + temp1 * K + K * K) / temp;
        numerator[1] = 2.0 * (K * K - 1.0) / temp;
        numerator[2] = (1.0 - temp1 * K + K * K) / temp;
        denominator[0] = 1.0;
        denominator[1] = 2.0 * (K * K - 1.0) / temp;
        denominator[2] = (1.0 - temp2 * K + K * K) / temp;

        result = FilterFreqdB(numerator, denominator, 2, 2, 512);

        return result;
    }

    public static double[] FilterFreqdB(double[] numerator, double[] denominator,
                                        int Lnum, int Ldenom, int len) {

        double ar, ai, br, bi, re, im, t, w, co, si;
        double pi = Math.PI;
        int i, j;
        double[] result = new double[len + 1];
        for (i = 0; i <= len; i++) {
            w = pi * i / len;
            co = Math.cos(w);
            si = -Math.sin(w);
            br = 0.;
            bi = 0.;
            for (j = Lnum; j >= 1; j--) {
                re = br;
                im = bi;
                br = (re + numerator[j]) * co - im * si;
                bi = (re + numerator[j]) * si + im * co;
            }
            br += numerator[0];
            ar = 0.0;
            ai = 0.0;
            for (j = Ldenom; j >= 1; j--) {
                re = ar;
                im = ai;
                ar = (re + denominator[j]) * co - im * si;
                ai = (re + denominator[j]) * si + im * co;
            }
            ar += denominator[0];
            t = ar * ar + ai * ai;
            re = (br * ar + bi * ai) / t;
            im = (bi * ar - br * ai) / t;
            result[i] = 10 * Math.log10(re * re + im * im);
        }

        return result;
    }

    public static double[] CompensationFIR(double[] srcdB, double[] deldB) {

        double[] hinv = new double[128];
        int NFFT = 1024, i, indm = 0;
        double fs = 44100;
        double[] f, B, temp;
        double Ap, abs2, hinvm = 0.0, t;
        f = new double[NFFT / 2 + 1];
        B = new double[NFFT / 2 + 1];

        temp = new double[NFFT];
        Arrays.fill(temp, 0.0);
        double fEdge[] = new double[]{0.0, 200.0, 300.0, 16000.0, 16000.0 * 4.0 / 3.0, fs / 2.0};
        double dBEdge[] = new double[]{40.0, 40.0, 20.0, 20.0, -3.0, -3.0};
        for (i = 0; i <= NFFT / 2; i++) {
            f[i] = fs * i / NFFT;
        }
        InterpLinear(fEdge, dBEdge, 6, f, B, NFFT / 2 + 1);
        for (i = 0; i <= NFFT / 2; i++) {
            B[i] = Math.pow(10.0, -B[i] / 20);
            Ap = Math.pow(10.0, deldB[i] / 20);
            abs2 = Math.pow(10.0, srcdB[i] / 10);
            temp[i] = Ap * abs2 / (abs2 + B[i] * B[i]);
        }
        FFT.irfft(temp, NFFT);
        for (i = 0; i < NFFT / 2; i++) {
            t = temp[i + NFFT / 2];
            temp[i + NFFT / 2] = temp[i];
            temp[i] = t;
        }
        for (i = 0; i < NFFT; i++) {
            indm = Math.abs(temp[i]) > hinvm ? i : indm;
            hinvm = Math.abs(temp[i]) > hinvm ? Math.abs(temp[i]) : hinvm;
        }
        for (i = 0; i < 128; i++) {
            hinv[i] = temp[i + indm - 64] / hinvm;
        }
        return hinv;
    }


    public static void InterpLinear(double[] x1, double[] y1, int L1, double[] x2, double[] y2, int L2) {
        int i, j;
        for (i = 0; i < L2; i++) {
            for (j = 0; j < L1 - 1; j++)
                if (x2[i] >= x1[j] && x2[i] <= x1[j + 1]) break;
            y2[i] = y1[j] + (x2[i] - x1[j]) * (y1[j + 1] - y1[j]) / (x1[j + 1] - x1[j]);
        }
    }

}
