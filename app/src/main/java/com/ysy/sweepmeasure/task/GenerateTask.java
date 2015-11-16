package com.ysy.sweepmeasure.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.github.mikephil.charting.charts.LineChart;
import com.ysy.sweepmeasure.sweep.Sweep;

/**
 * User: ysy
 * Date: 2015/8/19
 */
public class GenerateTask extends AsyncTask {
    private Sweep sweep;
    private LineChart chart_sweep;
    private ProgressDialog dialog1;

    public GenerateTask(Sweep sweep, ProgressDialog dialog1) {
        this.sweep = sweep;
        //this.chart_sweep = chart_sweep;
        this.dialog1 = dialog1;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        int length = (int) (sweep.getFs() * sweep.getT());
        sweep.GenerateWave(new double[length], new double[length]);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
//        chart_sweep.setScaleX(1);
//        chart_sweep.moveViewToX(0f);
//        chart_sweep.setVisibleXRangeMaximum((float) (sweep.getFs() * sweep.getT()));
        dialog1.dismiss();
        super.onPostExecute(o);
    }

}
