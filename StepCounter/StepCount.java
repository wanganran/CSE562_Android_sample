package com.example.mehrdadh.imu;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class StepCount {
    private class BandPassFilter{
        //0.5Hz - 3Hz
        //2s delay
        int N=2;
        float f1;
        float f2;
        float[] kernel;
        public BandPassFilter(int sampRate, float f1, float f2){
            this.f1=f1;
            this.f2=f2;
            kernel=new float[sampRate*N];
            float sum=0f;
            for(int i=0;i<sampRate*N;i++){
                int n=sampRate-i;
                kernel[i]=(float)(Math.sin(2*Math.PI*f2*n/sampRate)/n/Math.PI-Math.sin(2*Math.PI*f1*n/sampRate)/n/Math.PI);

            }
            kernel[sampRate]=2*(f2-f1)/sampRate;
            for(int i=0;i<kernel.length;i++)sum+=kernel[i];
            for(int i=0;i<kernel.length;i++)kernel[i]/=sum;
            buffer=new ArrayList<>();
        }
        private List<Float> buffer;
        public float input(float data){
            buffer.add(data);
            if(buffer.size()>sampRate*N){
                buffer.remove(0);
                float sum=0f;
                for(int i=0;i<buffer.size();i++){
                    sum+=kernel[i]*(buffer.get(i));
                }
                return sum;
            } else return 0f;
        }
    }

    private int sampRate;
    public StepCount(int sampRate){
        this.sampRate=sampRate;
        this.filters=new BandPassFilter[6]; //3 for grav, 3 for acc;
        for(int i=0;i<3;i++){
            filters[i]=new BandPassFilter(sampRate, 0.0f, 0.2f);
            filters[i+3]=new BandPassFilter(sampRate, 0.5f, 3f);
        }
    }

    private BandPassFilter[] filters;
    private float ANGLE_THRES=0.15f;
    private boolean duringPeak=false;
    private int steps=0;
    private int lastStepId=-sampRate;
    private int currentId=0;
    public void input(float[] gravity, float[] acc){
        float[] dc=new float[3];
        for(int i=0;i<3;i++)
            dc[i]=filters[i].input(gravity[i]);

        currentId++;
        Log.d("dc", dc[1]+" "+gravity[1]);
        float fz=dc[0]*gravity[0]+dc[1]*gravity[1]+dc[2]*gravity[2];
        float fm=(float)(Math.sqrt(dc[0]*dc[0]+dc[1]*dc[1]+dc[2]*dc[2])*Math.sqrt(gravity[0]*gravity[0]+gravity[1]*gravity[1]+gravity[2]*gravity[2]));
        float angle=(float)Math.acos(fz/fm);
        Log.d("angle", angle+"");
        if(angle>ANGLE_THRES && !duringPeak){
            duringPeak=true;
            if(currentId-lastStepId>sampRate*0.2) { //2.5Hz
                steps++;
                lastStepId = currentId;
            }
        } else if(angle<ANGLE_THRES && duringPeak){
            duringPeak=false;
        }
    }
    public int getSteps(){return steps/2;}
    public void resetSteps(){steps=0;}
}
