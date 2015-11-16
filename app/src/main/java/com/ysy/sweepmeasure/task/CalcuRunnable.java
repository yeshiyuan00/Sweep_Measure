package com.ysy.sweepmeasure.task;

import android.app.ProgressDialog;
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
    private ProgressDialog dialog;

    public CalcuRunnable(ProgressDialog dialog) {
        initFis();
        convs = new Convs();
        this.dialog = dialog;
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

        /**
         * 读出录音信号
         * */
        for (int i = 0, length = record.length / 2; i < length; i++) {
            short temp = (short) (((record[2 * i + 1] & 0xff) << 8) | (record[2 * i] & 0xff));

            Drecord[i] = temp / 32767.0;
        }
        for (int i = record.length / 2; i < Drecord.length; i++) {
            Drecord[i] = 0.0;
        }

        /**
         * 读出反卷积信号
         * */
        for (int i = 0, length = deconv.length / 8; i < length; i++) {
            byte[] temp = new byte[8];
            for (int j = 0; j < 8; j++) {
                temp[j] = deconv[8 * i + j];
            }
            Ddeconv[i] = ByteUtil.bytesToDouble(temp);
            if (i < 300) {
                Log.e("Test", "Ddeconv[" + i + "]=" + Ddeconv[i]);
            }
        }
        for (int i = deconv.length / 8; i < Ddeconv.length; i++) {
            Ddeconv[i] = 0.0;
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

        //convs.convols(Ddeconv, Drecord, DBuffC, Ddeconv.length, Drecord.length);
        long time = System.currentTimeMillis();
        convs.convols(Ddeconv, Drecord);
        Log.e("Test:", "sumtime=" + (System.currentTimeMillis() - time));


        if (fosBc != null) {
            try {
                fosBc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dialog.dismiss();
    }


    private void initFis() {
        fileDc = new File(FilePath.FILEDCPATH);
        fileRe = new File(FilePath.RECORDPATH);
        fileBc = new File(FilePath.BUFCPATH);
        record = new byte[(int) fileRe.length()];
        deconv = new byte[(int) fileDc.length()];
        int lx = record.length / 2;
        int ly = deconv.length / 8;
        int lz = (int) Math.pow(2, Math.ceil(Math.log(lx + ly - 1) / Math.log(2)));

        Drecord = new double[lz];
        Ddeconv = new double[lz];
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

    }
}
