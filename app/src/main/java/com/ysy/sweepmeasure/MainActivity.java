package com.ysy.sweepmeasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ysy.sweepmeasure.helper.SettingHelper;
import com.ysy.sweepmeasure.sweep.Sweep;
import com.ysy.sweepmeasure.task.GenerateTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private Button btn_generate, btn_record, btn_calculate;
    private LineChart chart_sweep, chart_record;
    private AudioTrack mAudioTrack;
    private AudioRecord mAudioRecord;
    private int BuffSize, recBuffSize;
    private short[] PlayBuff;
    private final String FILEDIR = Environment.getExternalStorageDirectory()
            + "/sweep";
    private final String FILEPATH = Environment.getExternalStorageDirectory()
            + "/sweep/test.txt";

    private int fs, f1, f2, A;
    private double T;

    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
    FileOutputStream fos = null;

    private final int CLEARVALUE = 0x01;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEARVALUE:
                    chart_sweep.clearValues();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FindViewById();
        SetListener();
        initChart();

        readData();
    }

    private void readData() {
        fs = SettingHelper.getSharedPreferences(MainActivity.this, "edt_fs", 44100);
        f1 = SettingHelper.getSharedPreferences(MainActivity.this, "edt_f1", 22);
        f2 = SettingHelper.getSharedPreferences(MainActivity.this, "edt_f2", 22000);
        T = SettingHelper.getSharedPreferences(MainActivity.this, "edt_t", 15.0);
        A = SettingHelper.getSharedPreferences(MainActivity.this, "edt_a", 16384);

        BuffSize = AudioTrack.getMinBufferSize(fs,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        PlayBuff = new short[BuffSize / 4];
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                BuffSize, AudioTrack.MODE_STREAM);
        recBuffSize = AudioRecord.getMinBufferSize(fs, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, fs, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recBuffSize);
    }

    private void initChart() {
        chart_sweep.setDescription("");
        chart_sweep.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        chart_sweep.setHighlightEnabled(true);

        // enable touch gestures
        chart_sweep.setTouchEnabled(false);

        // enable scaling and dragging
        chart_sweep.setDragEnabled(false);
        chart_sweep.setScaleEnabled(false);
        chart_sweep.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart_sweep.setPinchZoom(false);

        // set an alternative background color
        chart_sweep.setBackgroundColor(Color.CYAN);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart_sweep.setData(data);

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = chart_sweep.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart_sweep.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(false);

        YAxis leftAxis = chart_sweep.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(32767f);
        leftAxis.setAxisMinValue(-32767f);
        //leftAxis.setAxisMinValue(0f);
        leftAxis.setStartAtZero(false);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart_sweep.getAxisRight();
        rightAxis.setEnabled(false);


        chart_record.setDescription("");
        chart_record.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        chart_record.setHighlightEnabled(true);

        // enable touch gestures
        chart_record.setTouchEnabled(false);

        // enable scaling and dragging
        chart_record.setDragEnabled(false);
        chart_record.setScaleEnabled(false);
        chart_record.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart_record.setPinchZoom(false);

        // set an alternative background color
        chart_record.setBackgroundColor(Color.CYAN);

        LineData data1 = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart_record.setData(data1);

        Typeface tf1 = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l1 = chart_record.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l1.setForm(Legend.LegendForm.LINE);
        l1.setTypeface(tf1);
        l1.setTextColor(Color.WHITE);

        XAxis xl1 = chart_record.getXAxis();
        xl1.setTypeface(tf1);
        xl1.setTextColor(Color.WHITE);
        xl1.setDrawGridLines(false);
        xl1.setAvoidFirstLastClipping(true);
        xl1.setSpaceBetweenLabels(5);
        xl1.setEnabled(false);

        YAxis leftAxis1 = chart_record.getAxisLeft();
        leftAxis1.setTypeface(tf);
        leftAxis1.setTextColor(Color.WHITE);
        leftAxis1.setAxisMaxValue(32767f);
        leftAxis1.setAxisMinValue(-32767f);
        //leftAxis.setAxisMinValue(0f);
        leftAxis1.setStartAtZero(false);
        leftAxis1.setDrawGridLines(true);

        YAxis rightAxis1 = chart_record.getAxisRight();
        rightAxis1.setEnabled(false);
        //chart_sweep.moveViewToX(0);

    }

    private void SetListener() {
        btn_generate.setOnClickListener(new BtnClickListener());
        btn_record.setOnClickListener(new BtnClickListener());
        btn_calculate.setOnClickListener(new BtnClickListener());
    }

    private void FindViewById() {
        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_calculate = (Button) findViewById(R.id.btn_calculate);
        chart_sweep = (LineChart) findViewById(R.id.chart_sweep);
        chart_record = (LineChart) findViewById(R.id.chart_record);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_generate:
                    GenerateWave();
                    break;
                case R.id.btn_record:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PlayAndRecord();
                        }
                    }).start();
                    break;
                case R.id.btn_calculate:
                    break;
            }
        }
    }

    private void addEntry(double input) {

        LineData data = chart_sweep.getData();

        if (data != null) {

            LineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
            data.addXValue(" ");
            data.addEntry(new Entry((float) (input), set.getEntryCount()), 0);

            // let the chart know it's data has changed
            //chart_sweep.notifyDataSetChanged();

            // limit the number of visible entries
            // chart_sweep.setVisibleXRangeMaximum(120);
            // chart_sweep.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            //chart_sweep.moveViewToX(data.getXValCount() - 121);
            //chart_sweep.moveViewToX(0.0f);
            // this automatically refreshes the chart (calls invalidate())
            // chart_sweep.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleSize(0.0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void GenerateWave() {
        chart_sweep.clearValues();
        File dir = new File(FILEDIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(FILEPATH);
        if (file.exists()) {
            file.delete();
        }
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_param, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.parameter_setting)
                .setView(view);
        final EditText edt_fs = (EditText) view.findViewById(R.id.edt_fs);
        final EditText edt_f1 = (EditText) view.findViewById(R.id.edt_f1);
        final EditText edt_f2 = (EditText) view.findViewById(R.id.edt_f2);
        final EditText edt_T = (EditText) view.findViewById(R.id.edt_T);
        final EditText edt_A = (EditText) view.findViewById(R.id.edt_A);

        edt_fs.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_fs", 44100) + "");
        edt_f1.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_f1", 22) + "");
        edt_f2.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_f2", 22000) + "");
        edt_T.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_t", 15.0) + "");
        edt_A.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_a", 16384) + "");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fs = Integer.valueOf(edt_fs.getText().toString());
                f1 = Integer.valueOf(edt_f1.getText().toString());
                f2 = Integer.valueOf(edt_f2.getText().toString());
                T = Double.valueOf(edt_T.getText().toString());
                A = Integer.valueOf(edt_A.getText().toString());
                SettingHelper.setEditor(MainActivity.this, "edt_fs", fs);
                SettingHelper.setEditor(MainActivity.this, "edt_f1", f1);
                SettingHelper.setEditor(MainActivity.this, "edt_f2", f2);
                SettingHelper.setEditor(MainActivity.this, "edt_t", T);
                SettingHelper.setEditor(MainActivity.this, "edt_a", A);

                mAudioTrack = null;
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        BuffSize, AudioTrack.MODE_STREAM);

                mAudioRecord = null;
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, fs, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, recBuffSize);

                final Sweep sweep = new Sweep(fs, f1, f2, T, A);
                sweep.setDataListener(new Sweep.DataListener() {
                    int j = 0;

                    @Override
                    public void dataChange(double data) {

                        //System.out.println("data[" + (j) + "]=" + data);

                        if (j % 200 == 0) {
                            addEntry(data);
                        }
                        if (j % 10000 == 0) {
                            chart_sweep.notifyDataSetChanged();
                            chart_sweep.setVisibleXRangeMaximum((float)
                                    ((sweep.getFs() * sweep.getT()) / 200));
                            chart_sweep.moveViewToX(0f);
                        }

                        j++;
                        byte[] temp = new byte[2];
                        short temp1 = (short) data;
                        temp[0] = (byte) (temp1 >> 8);
                        temp[1] = (byte) (temp1);
                        if (fos != null) {
                            try {
                                fos.write(temp, 0, 2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                GenerateTask task = new GenerateTask(sweep, chart_sweep);
                task.execute();

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void PlayAndRecord() {
        File file = new File(FILEPATH);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, R.string.please_generate, Toast.LENGTH_SHORT).show();
            return;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mAudioTrack.play();
        byte[] playbyte = new byte[(BuffSize / 4) * 2];
        if (fis != null) {
            try {
                while (fis.read(playbyte, 0, playbyte.length) != -1) {

                    for (int i = 0; i < playbyte.length / 2; i++) {
                        PlayBuff[i] = (short) (((playbyte[2 * i] & 0xff) << 8) | (playbyte[i * 2 + 1] & 0xff));
                    }
                    mAudioTrack.write(PlayBuff, 0, PlayBuff.length);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //mAudioTrack.pause();
    }

    public static byte[] DoubleToArray(double Value) {
        long accum = Double.doubleToRawLongBits(Value);
        byte[] byteRet = new byte[8];
        byteRet[0] = (byte) (accum & 0xFF);
        byteRet[1] = (byte) ((accum >> 8) & 0xFF);
        byteRet[2] = (byte) ((accum >> 16) & 0xFF);
        byteRet[3] = (byte) ((accum >> 24) & 0xFF);
        byteRet[4] = (byte) ((accum >> 32) & 0xFF);
        byteRet[5] = (byte) ((accum >> 40) & 0xFF);
        byteRet[6] = (byte) ((accum >> 48) & 0xFF);
        byteRet[7] = (byte) ((accum >> 56) & 0xFF);
        return byteRet;
    }

    private static double ArryToDouble(byte[] Array, int Pos) {
        long accum = 0;
        accum = Array[Pos + 0] & 0xFF;
        accum |= (long) (Array[Pos + 1] & 0xFF) << 8;
        accum |= (long) (Array[Pos + 2] & 0xFF) << 16;
        accum |= (long) (Array[Pos + 3] & 0xFF) << 24;
        accum |= (long) (Array[Pos + 4] & 0xFF) << 32;
        accum |= (long) (Array[Pos + 5] & 0xFF) << 40;
        accum |= (long) (Array[Pos + 6] & 0xFF) << 48;
        accum |= (long) (Array[Pos + 7] & 0xFF) << 56;
        return Double.longBitsToDouble(accum);
    }

    @Override
    protected void onDestroy() {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        super.onDestroy();
    }

    private abstract class AddRunnable implements Runnable {
        public double data;

        public AddRunnable(double data) {
            this.data = data;
        }
    }
}
