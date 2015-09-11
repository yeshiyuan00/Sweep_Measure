package com.ysy.sweepmeasure.task;

import android.util.Log;

import com.ysy.sweepmeasure.file.FilePath;
import com.ysy.sweepmeasure.sweep.Convs;
import com.ysy.sweepmeasure.util.ByteUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: ysy
 * Date: 2015/9/6
 */
public class CalcuRunnable implements Runnable {
    private FileInputStream fisDc = null;
    private FileInputStream fisRe = null;
    private FileOutputStream fosBc = null;
    private File fileDc;
    private File fileRe;
    private File fileBc;
    private byte[] record = null;
    private byte[] deconv = null;
    private double[] Drecord = null;
    private double[] Ddeconv = null;
    private double[] DBuffC = null;

    private Convs convs;

    public CalcuRunnable() {
        initFis();
        convs = new Convs();
    }


    @Override
    public void run() {
        try {
            fisRe.read(record, 0, (int) fileRe.length());
            fisDc.read(deconv, 0, (int) fileDc.length());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fisDc != null) {
            try {
                fisDc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fisRe != null) {
            try {
                fisRe.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < Drecord.length; i++) {
            short temp = (short) (((record[2 * i+1] & 0xff) << 8) | (record[2 * i] & 0xff));

            Drecord[i] = temp / 32767.0;
        }

        for (int i = 0; i < Ddeconv.length; i++) {
            byte[] temp = new byte[8];
            for (int j = 0; j < 8; j++) {
                temp[j] = deconv[8 * i + j];
            }
            Ddeconv[i] = ByteUtil.bytesToDouble(temp);
            if (i < 300) {
                Log.e("Test", "Ddeconv[" + i + "]=" + Ddeconv[i]);
            }
        }
        convs.setDataListener(new Convs.SaveDataListener() {
            @Override
            public void savedata(double data) {
                byte[] temp = new byte[8];
                temp = ByteUtil.doubleToBytes(data);
                try {
                    fosBc.write(temp, 0, temp.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        convs.convols(Ddeconv, Drecord, DBuffC, Ddeconv.length, Drecord.length);

        if (fosBc != null) {
            try {
                fosBc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void initFis() {
        fileDc = new File(FilePath.FILEDCPATH);
        fileRe = new File(FilePath.RECORDPATH);
        fileBc = new File(FilePath.BUFCPATH);
        record = new byte[(int) fileRe.length()];
        deconv = new byte[(int) fileDc.length()];
        Drecord = new double[record.length / 2];
        Ddeconv = new double[deconv.length / 8];
        DBuffC = new double[Drecord.length + Ddeconv.length - 1];

        if (fisDc != null) {
            try {
                fisDc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fisDc = new FileInputStream(fileDc);// 建立一个可存取字节的文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        if (fisRe != null) {
            try {
                fisRe.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fisRe = new FileInputStream(fileRe);// 建立一个可存取字节的文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
    }
}
