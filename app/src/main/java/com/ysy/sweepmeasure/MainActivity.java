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
import android.widget.Toast;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ysy.sweepmeasure.file.FilePath;
import com.ysy.sweepmeasure.file.GlobalData;
import com.ysy.sweepmeasure.helper.SettingHelper;
import com.ysy.sweepmeasure.sweep.Sweep;
import com.ysy.sweepmeasure.task.CalcuRunnable;
import com.ysy.sweepmeasure.task.GenerateTask;
import com.ysy.sweepmeasure.task.PlayRunnable;
import com.ysy.sweepmeasure.task.RecordRunnable;
import com.ysy.sweepmeasure.task.RecordTask;
import com.ysy.sweepmeasure.util.AppManager;
import com.ysy.sweepmeasure.util.ByteUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button btn_generate, btn_record, btn_calculate, btn_open_player;
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

    private final String HINV0PATH = Environment.getExternalStorageDirectory()
            + "/sweep/hinv0.txt";
    private static final String HINVMIC_NAME = "hinvmic.txt";
    private static final String IMPD_NAME = "hdesrblk_a.txt";
    private static final String WINBLK_NAME = "winblk.txt";

    private int fs, f1, f2, A;
    private double T;

    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
    FileOutputStream fos = null;     //存储生成的扫频信号
    FileOutputStream fosDc = null;  //存储反卷积信号
    FileOutputStream foshiv = null;  //存储hinv0

    private final int CLEARVALUE = 0x01;
    private double[] impd, winblk;


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
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
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
        btn_open_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.doStartApplicationWithPackageName(MainActivity.this, "com.exp.ysy.wav_process_pks");
            }
        });
    }

    private void FindViewById() {
        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_calculate = (Button) findViewById(R.id.btn_calculate);
        btn_open_player = (Button) findViewById(R.id.btn_open_player);
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
                    calulateFir();
                    break;
            }
        }
    }

    private void calulateFir() {

        File file = new File(FilePath.RECORDPATH);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, R.string.please_record_first, Toast.LENGTH_SHORT).show();
            return;
        }

        init_params();
        //recordRunnable.stopRecord();
    }

    private void init_params() {
        View view = getLayoutInflater().inflate(R.layout.dialog_calparam, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.parameter_setting)
                .setView(view);
        final EditText edt_rang0 = (EditText) view.findViewById(R.id.edt_rang0);
        final EditText edt_rang1 = (EditText) view.findViewById(R.id.edt_rang1);
        final EditText edt_reg0 = (EditText) view.findViewById(R.id.edt_reg0);
        final EditText edt_reg1 = (EditText) view.findViewById(R.id.edt_reg1);
        final EditText edt_reg2 = (EditText) view.findViewById(R.id.edt_reg2);

        edt_rang0.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_rang0", 300.0) + "");
        edt_rang1.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_rang1", 16000.0) + "");
        edt_reg0.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_reg0", 40.0) + "");
        edt_reg1.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_reg1", 20.0) + "");
        edt_reg2.setText(SettingHelper.getSharedPreferences(MainActivity.this, "edt_reg2", -3.0) + "");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GlobalData.range[0] = Double.valueOf(edt_rang0.getText().toString());
                GlobalData.range[1] = Double.valueOf(edt_rang1.getText().toString());
                GlobalData.reg[0] = Double.valueOf(edt_reg0.getText().toString());
                GlobalData.reg[1] = Double.valueOf(edt_reg1.getText().toString());
                GlobalData.reg[2] = Double.valueOf(edt_reg2.getText().toString());

                SettingHelper.setEditor(MainActivity.this, "edt_rang0", GlobalData.range[0]);
                SettingHelper.setEditor(MainActivity.this, "edt_rang1", GlobalData.range[1]);
                SettingHelper.setEditor(MainActivity.this, "edt_reg0", GlobalData.reg[0]);
                SettingHelper.setEditor(MainActivity.this, "edt_reg1", GlobalData.reg[1]);
                SettingHelper.setEditor(MainActivity.this, "edt_reg2", GlobalData.reg[2]);

                ProgressDialog dialog2 = new ProgressDialog(MainActivity.this);
                dialog2.setMessage(getResources().getString(R.string.Calculating));
                dialog2.setCanceledOnTouchOutside(false);
                dialog2.setCancelable(false);
                dialog2.show();
                impd = Read_Accesset(512, IMPD_NAME);
                winblk = Read_Accesset(512, WINBLK_NAME);
                scheduledThreadPool.schedule(new CalcuRunnable(dialog2, impd, winblk), 0, TimeUnit.MILLISECONDS);

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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

        File filehiv0 = new File(HINV0PATH);
        if (filehiv0.exists()) {
            filehiv0.delete();
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

        if (foshiv != null) {
            try {
                foshiv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            foshiv = new FileOutputStream(filehiv0);// 建立一个可存取字节的文件
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
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                        BuffSize, AudioTrack.MODE_STREAM);

                mAudioRecord = null;
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, fs, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, recBuffSize);
                double[] hinvmic = new double[1024];
                hinvmic = Read_Accesset(1024, HINVMIC_NAME);
                final Sweep sweep = new Sweep(fs, f1, f2, T, A, hinvmic);
                sweep.setDataListener(new Sweep.DataListener() {

                    @Override
                    public void dataChange(double[] data) {

                        //System.out.println("data[" + (j) + "]=" + data);
                        byte[] temp = new byte[2];
                        byte[] data_byte = new byte[data.length * 4];
                        for (int i = 0, lh = data.length; i < lh; i++) {
                            short temp1 = (short) data[i];
                            temp[0] = (byte) (temp1 >> 8);
                            temp[1] = (byte) (temp1);
                            data_byte[4 * i] = temp[0];
                            data_byte[4 * i + 1] = temp[1];
                            data_byte[4 * i + 2] = temp[0];
                            data_byte[4 * i + 3] = temp[1];
                        }

                        if (fos != null) {
                            try {
                                fos.write(data_byte, 0, data_byte.length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void deconvData(double[] data) {
                        byte[] temp = new byte[8];
                        byte[] data_byte = new byte[data.length * 8];
                        for (int i = 0, lh = data.length; i < lh; i++) {
                            temp = ByteUtil.doubleToBytes(data[i]);
                            System.arraycopy(temp, 0, data_byte, 8 * i, 8);
                        }
                        if (fosDc != null) {
                            try {
                                fosDc.write(data_byte, 0, data_byte.length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void hinv0Data(double[] data) {
                        byte[] data_byte = new byte[data.length * 8];
                        byte[] temp = new byte[8];
                        for (int i = 0, lh = data.length; i < lh; i++) {
                            temp = ByteUtil.doubleToBytes(data[i]);
                            System.arraycopy(temp, 0, data_byte, 8 * i, 8);
                        }
                        if (foshiv != null) {
                            try {
                                foshiv.write(data_byte, 0, data_byte.length);
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

                GenerateTask task = new GenerateTask(sweep, dialog1);
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

        if (foshiv != null) {
            try {
                foshiv.close();
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

    public double[] Read_Accesset(int length, String file_name) {
        byte[] rbyte_fir = new byte[length * 8];
        double[] rdouble_fir = new double[length];
        InputStream fis = null;
        try {
            fis = this.getAssets().open(file_name);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fis != null) {
            try {
                fis.read(rbyte_fir, 0, length * 8);
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

        byte[] temp = new byte[8];
        for (int i = 0; i < length; i++) {

            for (int j = 0; j < 8; j++) {
                temp[j] = rbyte_fir[i * 8 + j];
            }
            rdouble_fir[i] = bytesToDouble(temp);
        }

        double[] fir_result = new double[length];
        System.arraycopy(rdouble_fir, 0, fir_result, 0, length);
        return fir_result;
    }


    //字节到浮点转换
    public static double bytesToDouble(byte[] readBuffer) {
        return Double.longBitsToDouble((((long) readBuffer[7] << 56) +
                        ((long) (readBuffer[6] & 255) << 48) +
                        ((long) (readBuffer[5] & 255) << 40) +
                        ((long) (readBuffer[4] & 255) << 32) +
                        ((long) (readBuffer[3] & 255) << 24) +
                        ((readBuffer[2] & 255) << 16) +
                        ((readBuffer[1] & 255) << 8) +
                        ((readBuffer[0] & 255) << 0))

        );
    }
}
