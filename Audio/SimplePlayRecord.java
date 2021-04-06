package com.example.anranw.acousticapp;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//simple activity to simultaneously play and record
public class SimplePlayRecord extends Activity {

    final int SAMPRATE = 48000;
    final int BUFFER=4800;

    int minbuffersize = 0;

    RecorderStereo recorder = null;
    AudioSpeaker speaker = null;

    Handler uiHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        // we need two buttons to start and stop
        final Button btnStop=(Button)findViewById(R.id.btnStop);
        final Button btnPlay=(Button)findViewById(R.id.btnPlay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlay.setEnabled(false);
                startTrack(true, new Runnable() {

                    @Override
                    public void run() {
                        // do nothing
                    }
                });
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTrack(new Runnable() {

                    @Override
                    public void run() {
                        btnPlay.setEnabled(true);
                    }
                });
            }
        });
    }

    void initRecorder(){
        recorder = new RecorderStereo(SAMPRATE, minbuffersize, new RecorderStereo.Callback() {
            @Override
            public void call(short[] data) {
                Log.d("recorder", "input");
                // do something to process the recorded data...
            }
        });
    }

    void initSpeaker() throws IOException {
        speaker = new AudioSpeaker(this, SAMPRATE, BUFFER, new AudioSpeaker.SampleGenerator() {
            double dt=0;

            @Override
            public void skip(int t){
                dt+=t/1000f;
            }
            @Override
            public short[] generate() {
                final short[] samples = new short[BUFFER];
                for (int i=0;i<BUFFER;i++){
                    double t=dt+i/(float)SAMPRATE;
                    // do something to generate signal at time t and fill samples[i]
                }
                dt+=BUFFER/(float)SAMPRATE;

                return samples;

            }
        });

    }

    void startTrack(boolean record, final Runnable onFinish) {
        try {
            initSpeaker();
        } catch(Exception ex){
            ex.printStackTrace();
        }

        speaker.start();
        if(record) {
            initRecorder();
            recorder.start();
        }
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                onFinish.run();
            }
        });
    }

    void stopTrack(final Runnable onFinish) {
        Thread stopThread = new Thread() {
            @Override
            public void run() {
                try {
                    recorder.stopRecord();
                    recorder=null;
                    speaker.pause();
                    speaker=null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        stopThread.start();
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                onFinish.run();
            }
        });
    }
}
