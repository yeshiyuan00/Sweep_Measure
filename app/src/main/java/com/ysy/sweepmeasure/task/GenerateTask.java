package com.ysy.sweepmeasure.task;

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

    public GenerateTask(Sweep sweep, LineChart chart_sweep) {
        this.sweep = sweep;
        this.chart_sweep = chart_sweep;
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
        super.onPostExecute(o);
    }

}
