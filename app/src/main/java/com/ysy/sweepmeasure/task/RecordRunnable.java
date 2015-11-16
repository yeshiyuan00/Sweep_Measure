package com.ysy.sweepmeasure.task;

import android.app.ProgressDialog;
import android.media.AudioRecord;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: ysy
 * Date: 2015/8/24
 */
public class RecordRunnable implements Runnable {

    private final String RECORDPATH = Environment.getExternalStorageDirectory() + "/sweep/record.txt";
    private final String RECORDWAV = Environment.getExternalStorageDirectory() + "/sweep/record.wav";
    private AudioRecord mAudioRecord;
    private int recBuffsize;
    private boolean isRecording;
    private ProgressDialog dialog;

    public RecordRunnable(AudioRecord mAudioRecord, int recBuffsize, ProgressDialog dialog) {
        this.mAudioRecord = mAudioRecord;
        this.recBuffsize = recBuffsize;
        isRecording = true;
        this.dialog = dialog;
    }

    public void stopRecord() {
        isRecording = false;
        dialog.dismiss();
    }

    @Override
    public void run() {
        isRecording = true;
        mAudioRecord.startRecording();

        byte[] buffer = new byte[recBuffsize];
        short[] buffer1 = new short[recBuffsize / 2];

        FileOutputStream fos = null;

        try {
            File file = new File(RECORDPATH);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (isRecording) {
            int bufferReadResult = mAudioRecord.read(buffer, 0, recBuffsize);
            if (fos != null) {
                try {
                    fos.write(buffer, 0, bufferReadResult);   //写入文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            if (fos != null)
                fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
        }

        copyWaveFile(RECORDPATH, RECORDWAV, recBuffsize);
        return;

    }

    private void copyWaveFile(String inFilename, String outFilename, int recBufSize) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = 44100;
        int channels = 1;
        long byteRate = 16 * 44100 * channels / 8;
        byte[] data = new byte[recBufSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate,
                                     int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
