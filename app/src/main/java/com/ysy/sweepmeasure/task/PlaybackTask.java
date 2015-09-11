package com.ysy.sweepmeasure.task;

import android.content.Context;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import com.ysy.sweepmeasure.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * User: ysy
 * Date: 2015/8/24
 */
public class PlaybackTask extends AsyncTask {
    private Context mContext;
    private AudioTrack mAudioTrack;
    private String FILEPATH;
    private int BuffSize;
    private short[] PlayBuff;
    private RecordTask recordTask;

    public PlaybackTask(Context mContext, AudioTrack mAudioTrack, String FILEPATH,
                        int BuffSize, RecordTask recordTask) {
        this.mContext = mContext;
        this.mAudioTrack = mAudioTrack;
        this.recordTask = recordTask;
        this.FILEPATH = FILEPATH;
        this.BuffSize = BuffSize;
        PlayBuff = new short[BuffSize / 4];
    }

    @Override
    protected void onPreExecute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                recordTask.execute();
            }
        }).start();

        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        File file = new File(FILEPATH);
        if (!file.exists()) {
            Looper.prepare();
            Looper.loop();
            Toast.makeText(mContext, R.string.please_generate, Toast.LENGTH_SHORT).show();
            return null;
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
                while (fis.read(playbyte, 0, playbyte.length) > 0) {

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
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        recordTask.stopRecord();
    }
}
