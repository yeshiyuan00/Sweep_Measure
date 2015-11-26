package com.ysy.sweepmeasure;

import android.app.Activity;
import android.app.Application;
import android.os.Environment;

import com.ysy.sweepmeasure.helper.SettingHelper;

import java.io.File;

/**
 * User: ysy
 * Date: 2015/8/19
 */
public class MyApplication extends Application {
    private final String FILEDIR = Environment.getExternalStorageDirectory()
            + "/sweep";

    public static double fir_fc1, fir_fc2, fir_fc3, fir_fc4, fir_fc5;
    public static double fir_fb1, fir_fb2, fir_fb3, fir_fb4, fir_fb5;
    public static double fir_g1, fir_g2, fir_g3, fir_g4, fir_g5;

    @Override
    public void onCreate() {
        super.onCreate();
        File dir = new File(FILEDIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void writeFirCorFromDatabase(Activity activity) {
        SettingHelper.setEditor(activity, "fir_fc1", fir_fc1);
        SettingHelper.setEditor(activity, "fir_fc2", fir_fc2);
        SettingHelper.setEditor(activity, "fir_fc3", fir_fc3);
        SettingHelper.setEditor(activity, "fir_fc4", fir_fc4);
        SettingHelper.setEditor(activity, "fir_fc5", fir_fc5);

        SettingHelper.setEditor(activity, "fir_fb1", fir_fb1);
        SettingHelper.setEditor(activity, "fir_fb2", fir_fb2);
        SettingHelper.setEditor(activity, "fir_fb3", fir_fb3);
        SettingHelper.setEditor(activity, "fir_fb4", fir_fb4);
        SettingHelper.setEditor(activity, "fir_fb5", fir_fb5);

        SettingHelper.setEditor(activity, "fir_g1", fir_g1);
        SettingHelper.setEditor(activity, "fir_g2", fir_g2);
        SettingHelper.setEditor(activity, "fir_g3", fir_g3);
        SettingHelper.setEditor(activity, "fir_g4", fir_g4);
        SettingHelper.setEditor(activity, "fir_g5", fir_g5);
    }

    private void readFirCorFromDatabase() {
        fir_fc1 = SettingHelper.getSharedPreferences(this, "fir_fc1", 500.0);
        fir_fc2 = SettingHelper.getSharedPreferences(this, "fir_fc2", 1000.0);
        fir_fc3 = SettingHelper.getSharedPreferences(this, "fir_fc3", 3000.0);
        fir_fc4 = SettingHelper.getSharedPreferences(this, "fir_fc4", 8000.0);
        fir_fc5 = SettingHelper.getSharedPreferences(this, "fir_fc5", 12000.0);

        fir_fb1 = SettingHelper.getSharedPreferences(this, "fir_fb1", 500.0);
        fir_fb2 = SettingHelper.getSharedPreferences(this, "fir_fb2", 500.0);
        fir_fb3 = SettingHelper.getSharedPreferences(this, "fir_fb3", 1000.0);
        fir_fb4 = SettingHelper.getSharedPreferences(this, "fir_fb4", 2000.0);
        fir_fb5 = SettingHelper.getSharedPreferences(this, "fir_fb5", 2000.0);

        fir_g1 = SettingHelper.getSharedPreferences(this, "fir_g1", 2.0);
        fir_g2 = SettingHelper.getSharedPreferences(this, "fir_g2", 3.0);
        fir_g3 = SettingHelper.getSharedPreferences(this, "fir_g3", 1.0);
        fir_g4 = SettingHelper.getSharedPreferences(this, "fir_g4", -2.0);
        fir_g5 = SettingHelper.getSharedPreferences(this, "fir_g5", -3.0);
    }

    public void resetFirCof() {
        fir_fc1 = 500.0;
        fir_fc2 = 1000.0;
        fir_fc3 = 3000.0;
        fir_fc4 = 8000.0;
        fir_fc5 = 12000.0;

        fir_fb1 = 500.0;
        fir_fb2 = 500.0;
        fir_fb3 = 1000.0;
        fir_fb4 = 2000.0;
        fir_fb5 = 2000.0;

        fir_g1 = 0.0;
        fir_g2 = 0.0;
        fir_g3 = 0.0;
        fir_g4 = 0.0;
        fir_g5 = 0.0;

    }
}
