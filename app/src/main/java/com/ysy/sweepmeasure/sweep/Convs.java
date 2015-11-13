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

    public void convols(double[] BufX, double[] BufY) {


        int lz = BufX.length;

        FFT.rfft(BufX, lz);
        FFT.rfft(BufY, lz);

        int len = lz / 2;
        double t;

        BufX[0] = BufX[0] * BufY[0];
        BufX[len] = BufX[len] * BufY[len];

        for (int i = 1; i < len; i++) {
            t = BufX[i] * BufY[i] - BufX[lz - i] * BufY[lz - i];
            BufX[lz - i] = BufX[i] * BufY[lz - i] + BufX[lz - i] * BufY[i];
            BufX[i] = t;
        }

        FFT.irfft(BufX, lz);

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
            temp = ByteUtil.doubleToBytes(BufX[i]);
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
