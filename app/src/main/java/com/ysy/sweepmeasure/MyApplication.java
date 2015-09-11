package com.ysy.sweepmeasure;

import android.app.Application;
import android.os.Environment;

import java.io.File;

/**
 * User: ysy
 * Date: 2015/8/19
 */
public class MyApplication extends Application {
    private final String FILEDIR = Environment.getExternalStorageDirectory()
            + "/sweep";

    @Override
    public void onCreate() {
        super.onCreate();
        File dir = new File(FILEDIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
