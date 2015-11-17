package com.ysy.sweepmeasure.sweep;

import com.ysy.sweepmeasure.file.FilePath;
import com.ysy.sweepmeasure.util.ByteUtil;
import com.ysy.sweepmeasure.util.FFT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: ysy
 * Date: 2015/9/6
 */
public class Convs {

    private SaveDataListener dataListener;

    public interface SaveDataListener {
        void savedata(double data);

    }

    public void setDataListener(SaveDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void convols(double[] Ddeconv, double[] Drecord) {



        int lz = Ddeconv.length;

        FFT.rfft(Ddeconv, lz);
        FFT.rfft(Drecord, lz);

        int len = lz / 2;
        double t;

        Ddeconv[0] = Ddeconv[0] * Drecord[0];
        Ddeconv[len] = Ddeconv[len] * Drecord[len];

        for (int i = 1; i < len; i++) {
            t = Ddeconv[i] * Drecord[i] - Ddeconv[lz - i] * Drecord[lz - i];
            Ddeconv[lz - i] = Ddeconv[i] * Drecord[lz - i] + Ddeconv[lz - i] * Drecord[i];
            Ddeconv[i] = t;
        }

        FFT.irfft(Ddeconv, lz);

        File fileBc = new File(FilePath.BUFCPATH);
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

        byte[] Byte_C = new byte[lz * 8];
        for (int i = 0; i < lz; i++) {
            byte[] temp = new byte[8];
            temp = ByteUtil.doubleToBytes(Ddeconv[i]);
            System.arraycopy(temp, 0, Byte_C, i * 8, 8);

        }

        try {
            fosBc.write(Byte_C, 0, Byte_C.length);
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
}
