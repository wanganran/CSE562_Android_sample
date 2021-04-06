package com.example.anranw.acousticapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

// class to access the stereo microphones
public class RecorderStereo extends Thread {

    public static interface Callback{
        void call(short[] data);
    }

    public boolean recording;
    int samplingfrequency;
    public short[] temp;
    int count;
    AudioRecord rec;
    int minbuffersize;
    Callback sink;

    // sink: define a callback instance to process the received data
    public RecorderStereo(int samplingfreq, int minbuffer, Callback sink)
    {
        Log.d("recorder", samplingfreq+" "+minbuffer);
        samplingfrequency = samplingfreq;
        minbuffersize=minbuffer;
        this.sink=sink;
        count = 0;

        rec = new AudioRecord(MediaRecorder.AudioSource.MIC,samplingfrequency,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT,minbuffersize);
        temp = new short[minbuffersize/2];
    }

    public void stopRecord() throws InterruptedException {
        this.recording=false;
        this.join();
    }

    public void run()
    {
        int bytesread=0;
        rec.startRecording();
        recording = true;
        try {
            while (recording) {
                while (bytesread < minbuffersize/2) {
                    bytesread += rec.read(temp, bytesread, minbuffersize/2-bytesread);
                }
                bytesread=0;
                sink.call(temp);
            }
            rec.stop();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}