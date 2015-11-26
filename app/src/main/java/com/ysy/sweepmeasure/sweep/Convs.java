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

    public void convols(double[] Ddeconv, double[] Drecord, double[] IMPD, double[] WINBLK) {

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
        //Mi = imax;

        for (i = 0; i < GlobalData.Limpi; i++) {

            impo[i] = impt[i + ind - GlobalData.Limpi / 2] * WINBLK[i];
        }

        int NFFT = 1024;
        double[] temp = new double[1024];
        double[] srcdB = new double[513];
        for (i = 0; i < 512; i++) {
            temp[i] = impo[i];
        }
        for (i = 512; i < 1024; i++) {
            temp[i] = 0.0;
        }

        FFT.rfft(temp, NFFT);

        srcdB[0] = 10.0 * Math.log10(temp[0] * temp[0]);
        srcdB[512] = 10.0 * Math.log10(temp[512] * temp[512]);
        for (i = 1; i < 512; i++) {
            srcdB[i] = 10.0 * Math.log10(temp[i] * temp[i] + temp[1024 - i] * temp[1024 - i]);
        }

        File fileBc = new File(FilePath.SRCDBPATH);
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

        byte[] Byte_src = new byte[513 * 8];
        byte[] temp1 = new byte[8];
        for (i = 0; i < 513; i++) {
            temp1 = ByteUtil.doubleToBytes(srcdB[i]);
            System.arraycopy(temp1, 0, Byte_src, i * 8, 8);
        }

        try {
            fosBc.write(Byte_src, 0, Byte_src.length);
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
        int NFFT = (int) Math.pow(2.0, Math.ceil(Math.log((double) Ly) / Math.log(2.0)));
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
