package com.ysy.sweepmeasure.sweep;

import com.ysy.sweepmeasure.file.FilePath;
import com.ysy.sweepmeasure.file.GlobalData;
import com.ysy.sweepmeasure.util.ByteUtil;
import com.ysy.sweepmeasure.util.FFT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: ysy
 * Date: 2015/9/6
 */
public class Convs {

    private double Mi = 0.0;

    private SaveDataListener dataListener;

    public interface SaveDataListener {
        void savedata(double data);

    }

    public void setDataListener(SaveDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void convols(double[] Ddeconv, double[] Drecord, double[] IMPD) {

        int Lrec = Drecord.length;
        int Lh = Ddeconv.length;
        int Li = Lrec + Lh - 1;
        double[] impt = new double[Li];
        double[] impo = new double[GlobalData.Limpi];

        impt = convol(Ddeconv, Drecord);

        int i, ind = 0;
        double imax = 0.;
        for (i = 0; i < Li; i++) {
            ind = Math.abs(impt[i]) > imax ? i : ind;
            imax = Math.abs(impt[i]) > imax ? Math.abs(impt[i]) : imax;
        }
        Mi = imax;

        for (i = 0; i < GlobalData.Limpi; i++) {
            impo[i] = impt[i + ind - GlobalData.Limpi / 4] / imax;
        }

        int fn = GlobalData.NFFT / 2 + 1;
        int len = GlobalData.NFFT / 2;
        i = 0;
        ind = 0;
        double temp, homax = 0;
        double[] fsample = new double[fn];
        double[] dBsample = new double[fn];
        double[] hit = new double[GlobalData.NFFT];
        double[] hdest = new double[GlobalData.NFFT];
        double[] hot = new double[GlobalData.NFFT];
        Arrays.fill(hit, 0.0);
        Arrays.fill(hdest, 0.0);
        if (GlobalData.range[0] > 0 && GlobalData.range[1] < GlobalData.fs / 2) {
            double fEdge[] = new double[]{0.0, GlobalData.range[0] * 2.0 / 3.0,
                    GlobalData.range[0], GlobalData.range[1],
                    GlobalData.range[1] * 4.0 / 3.0, GlobalData.fs / 2.0};
            double dBEdge[] = new double[]{GlobalData.reg[0], GlobalData.reg[0], GlobalData.reg[1],
                    GlobalData.reg[1], GlobalData.reg[2], GlobalData.reg[2]};
            if (fEdge[4] > fEdge[5]) fEdge[4] = GlobalData.range[1] + 1.0;
            for (i = 0; i < fn; i++) {
                fsample[i] = GlobalData.fs * i / GlobalData.NFFT;
            }
            InterpLinear(fEdge, dBEdge, 6, fsample, dBsample, fn);
            for (i = 0; i < fn; i++) {
                dBsample[i] = Math.pow(10.0, -dBsample[i] / 20.0);
            }
        } else {
            Arrays.fill(dBsample, 0.0);
        }

        System.arraycopy(impo, 0, hit, 0, impo.length);
        System.arraycopy(IMPD, 0, hdest, 0, IMPD.length);
        FFT.rfft(hit, GlobalData.NFFT);
        FFT.rfft(hdest, GlobalData.NFFT);
        hot[0] = hit[0] * hdest[0] / (hit[0] * hit[0] + dBsample[0] * dBsample[0]);
        hot[len] = hit[len] * hdest[len] / (hit[len] * hit[len] + dBsample[len] * dBsample[len]);
        for (i = 1; i < len; i++) {
            temp = hit[i] * hit[i] + hit[GlobalData.NFFT - i] * hit[GlobalData.NFFT - i]
                    + dBsample[i] * dBsample[i];
            hot[i] = (hdest[i] * hit[i]
                    + hdest[GlobalData.NFFT - i] * hit[GlobalData.NFFT - i]) / temp;
            hot[GlobalData.NFFT - i] = (hdest[GlobalData.NFFT - i] * hit[i]
                    - hdest[i] * hit[GlobalData.NFFT - i]) / temp;
        }
        FFT.irfft(hot, GlobalData.NFFT);
        for (i = 0; i < len; i++) {
            temp = hot[i + len] * GlobalData.Md / Mi;
            hot[i + len] = hot[i] * GlobalData.Md / Mi;
            hot[i] = temp;
        }
        for (i = 0; i < GlobalData.NFFT; i++) {
            ind = Math.abs(hot[i]) > homax ? i : ind;
            homax = Math.abs(hot[i]) > homax ? Math.abs(hot[i]) : homax;
        }

        double[] fircoef = new double[128];
        System.arraycopy(hot, ind - 128 / 4, fircoef, 0, 128);


        File fileBc = new File(FilePath.FIRPATH);
        FileOutputStream fosBc = null;

        if (fosBc != null) {
            try {
                fosBc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fosBc = new FileOutputStream(fileBc);// 建立一个可存取字节的文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] Byte_fir = new byte[128 * 8];
        for (i = 0; i < 128; i++) {
            byte[] temp1 = new byte[8];
            temp1 = ByteUtil.doubleToBytes(fircoef[i]);
            System.arraycopy(temp1, 0, Byte_fir, i * 8, 8);
        }

        try {
            fosBc.write(Byte_fir, 0, Byte_fir.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fosBc != null) {
            try {
                fosBc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void InterpLinear(double[] x1, double[] y1, int L1, double[] x2, double[] y2, int L2) {
        int i, j;
        for (i = 0; i < L2; i++) {
            for (j = 0; j < L1 - 1; j++)
                if (x2[i] >= x1[j] && x2[i] <= x1[j + 1]) break;
            y2[i] = y1[j] + (x2[i] - x1[j]) * (y1[j + 1] - y1[j]) / (x1[j + 1] - x1[j]);
        }
    }


    private double[] convol(double[] h, double[] rect) {
        int Lh = h.length;
        int Lx = rect.length;
        int Ly = Lh + Lx - 1;
        int NFFT = (int) Math.pow(2., Math.ceil(Math.log((double) Ly) / Math.log(2.0)));
        int len2 = NFFT / 2;
        double temp;
        double[] ht = new double[NFFT];
        double[] xt = new double[NFFT];
        double[] y = new double[Ly];
        Arrays.fill(ht, 0.0);
        Arrays.fill(xt, 0.0);
        System.arraycopy(h, 0, ht, 0, Lh);
        System.arraycopy(rect, 0, xt, 0, Lx);
        FFT.rfft(ht, NFFT);
        FFT.rfft(xt, NFFT);

        xt[0] = xt[0] * ht[0];
        xt[len2] = xt[len2] * ht[len2];
        for (int i = 1; i < len2; i++) {
            temp = xt[i] * ht[i] - xt[NFFT - i] * ht[NFFT - i];
            xt[NFFT - i] = xt[i] * ht[NFFT - i] + xt[NFFT - i] * ht[i];
            xt[i] = temp;
        }
        FFT.irfft(xt, NFFT);
        System.arraycopy(xt, 0, y, 0, Ly);
        return y;
    }
}
