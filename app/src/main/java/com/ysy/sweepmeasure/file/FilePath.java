package com.ysy.sweepmeasure.file;

import android.os.Environment;

/**
 * User: ysy
 * Date: 2015/9/6
 */
public class FilePath {

    public static final String FILEDIR = Environment.getExternalStorageDirectory()
            + "/sweep";
    public static final String FILEPATH = Environment.getExternalStorageDirectory()
            + "/sweep/test.txt";
    public static final String FILEDCPATH = Environment.getExternalStorageDirectory()
            + "/sweep/deconv.txt";

    public static final String RECORDPATH = Environment.getExternalStorageDirectory()
            + "/sweep/record.txt";
    public static final String RECORDWAV = Environment.getExternalStorageDirectory()
            + "/sweep/record.wav";

    public static final String BUFCPATH = Environment.getExternalStorageDirectory()
            + "/sweep/bufc.txt";
}
