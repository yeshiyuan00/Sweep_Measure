package com.ysy.sweepmeasure;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ysy.sweepmeasure.helper.SettingHelper;
import com.ysy.sweepmeasure.sweep.Sweep;
import com.ysy.sweepmeasure.task.CalcuRunnable;
import com.ysy.sweepmeasure.task.GenerateTask;
import com.ysy.sweepmeasure.task.PlayRunnable;
import com.ysy.sweepmeasure.task.RecordRunnable;
import com.ysy.sweepmeasure.task.RecordTask;
import com.ysy.sweepmeasure.util.ByteUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button btn_generate, btn_record, btn_calculate;
    //private LineChart chart_sweep, chart_record;
    private AudioTrack mAudioTrack;
    private AudioRecord mAudioRecord;
    private int BuffSize, recBuffSize;
    private short[] PlayBuff;
    RecordTask recordTask;
    private RecordRunnable recordRunnable;
    private final String FILEDIR = Environment.getExternalStorageDirectory()
            + "/sweep";
    private final String FILEPATH = Environment.getExternalStorageDirectory()
            + "/sweep/test.txt";
    private final String FILEDCPATH = Environment.getExternalStorageDirectory()
            + "/sweep/deconv.txt";

    private int fs, f1, f2, A;
    private double T;

    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
    FileOutputStream fos = null;     //存储生成的扫频信号
    FileOutputStream fosDc = null;  //存储反卷积信号


    private final int CLEARVALUE = 0x01;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FindViewById();
        SetListener();
        //initChart();

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
        recBuffSize = AudioRecord.getMinBufferSize(fs, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, fs, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recBuffSize);
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
//        chart_sweep = (LineChart) findViewById(R.id.chart_sweep);
//        chart_record = (LineChart) findViewById(R.id.chart_record);
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
                    ProgressDialog dialog1 = new ProgressDialog(MainActivity.this);
                    dialog1.setMessage(getResources().getString(R.string.Recording));
                    dialog1.setCanceledOnTouchOutside(false);
                    dialog1.setCancelable(false);
                    dialog1.show();
                    recordRunnable = new RecordRunnable(mAudioRecord, recBuffSize, dialog1);
                    scheduledThreadPool.schedule(recordRunnable, 0, TimeUnit.MILLISECONDS);
                    //playbackTask.execute();
                    scheduledThreadPool.schedule(new PlayRunnable(MainActivity.this, mAudioTrack,
                            FILEPATH, BuffSize, recordRunnable), 2, TimeUnit.SECONDS);
                    //
                    break;
                case R.id.btn_calculate:
                    //recordRunnable.stopRecord();
                    ProgressDialog dialog2 = new ProgressDialog(MainActivity.this);
                    dialog2.setMessage(getResources().getString(R.string.Calculating));
                    dialog2.setCanceledOnTouchOutside(false);
                    dialog2.setCancelable(false);
                    dialog2.show();
                    scheduledThreadPool.schedule(new CalcuRunnable(dialog2), 0, TimeUnit.MILLISECONDS);
                    break;
            }
        }
    }

//    private void addEntry(double input) {
//
//        LineData data = chart_sweep.getData();
//
//
//        if (data != null) {
//
//            LineDataSet set = data.getDataSetByIndex(0);
//            // set.addEntry(...); // can be called as well
//
//            if (set == null) {
//                set = createSet();
//                data.addDataSet(set);
//            }
//
//            // add a new x-value first
//            data.addXValue(" ");
//            data.addEntry(new Entry((float) (input), set.getEntryCount()), 0);
//
//            // let the chart know it's data has changed
//            //chart_sweep.notifyDataSetChanged();
//
//            // limit the number of visible entries
//            // chart_sweep.setVisibleXRangeMaximum(120);
//            // chart_sweep.setVisibleYRange(30, AxisDependency.LEFT);
//
//            // move to the latest entry
//            //chart_sweep.moveViewToX(data.getXValCount() - 121);
//            //chart_sweep.moveViewToX(0.0f);
//            // this automatically refreshes the chart (calls invalidate())
//            // chart_sweep.moveViewTo(data.getXValCount()-7, 55f,
//            // AxisDependency.LEFT);
//        }
//    }

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
        //chart_sweep.clearValues();
        File dir = new File(FILEDIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(FILEPATH);
        if (file.exists()) {
            file.delete();
        }

        File fileDc = new File(FILEDCPATH);
        if (fileDc.exists()) {
            fileDc.delete();
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

        if (fosDc != null) {
            try {
                fosDc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fosDc = new FileOutputStream(fileDc);// 建立一个可存取字节的文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_param, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, fs, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, recBuffSize);

                final Sweep sweep = new Sweep(fs, f1, f2, T, A);
                sweep.setDataListener(new Sweep.DataListener() {

                    @Override
                    public void dataChange(double data) {

                        //System.out.println("data[" + (j) + "]=" + data);
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

                    @Override
                    public void deconvData(double data) {

                        byte[] temp = new byte[8];
                        temp = ByteUtil.doubleToBytes(data);
                        if (fosDc != null) {
                            try {
                                fosDc.write(temp, 0, 8);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                ProgressDialog dialog1 = new ProgressDialog(MainActivity.this);
                dialog1.setMessage(getResources().getString(R.string.generating));
                dialog1.setCanceledOnTouchOutside(false);
                dialog1.setCancelable(false);

                dialog1.show();

                GenerateTask task = new GenerateTask(sweep,dialog1);
                task.execute();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

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

        if (fosDc != null) {
            try {
                fosDc.close();
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
