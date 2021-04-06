package com.example.mehrdadh.imu;

import android.graphics.Color;
import android.hardware.camera2.CameraMetadata;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;




public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView xTV, yTV, zTV;

    private TextView txtSteps;
    private Sensor linearSensor;
    private Sensor gravitySensor;
    private SensorManager SM;
    private Button startB;
    private boolean isPushed;

    private BufferedWriter buf_gra;
    private BufferedWriter buf_acc;
    private File file_acc;
    private File file_gra;
    private String filePath;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isPushed = false;

        // Create our sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        linearSensor = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravitySensor = SM.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // Register sensor listener
        SM.registerListener(this, linearSensor, SensorManager.SENSOR_DELAY_FASTEST);
        SM.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Assign text view
        xTV = (TextView) findViewById(R.id.xTV);
        yTV = (TextView) findViewById(R.id.yTV);
        zTV = (TextView) findViewById(R.id.zTV);
        txtSteps=(TextView)findViewById(R.id.txtSteps);

        // Creating Directory and file
        filePath = Environment.getExternalStorageDirectory() + File.separator + "IMU";
        File directory = new File(filePath);
        boolean isPresent = true;
        if (!directory.exists())
        {
            try{
                isPresent = directory.mkdirs();
            }
            catch(Exception e){
                Log.i("Exception", e.toString());
        }
        }
        if (isPresent)
        {
            Log.i("Directory", directory.getAbsolutePath());
            file_gra = new File(directory.getAbsolutePath(), "gra.bin");
            file_acc = new File(directory.getAbsolutePath(), "acc.bin");
            if (file_gra.exists())
            {
                file_gra.delete();
            }
            if (file_acc.exists())
            {
                file_acc.delete();
            }
        }
        else
        {
            Log.i("Error", "Directory did not create!");
        }

        //button wire up
        startB = (Button) findViewById(R.id.startB);
        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MyApp", "Button pushed!");
                isPushed = !isPushed;
                if (isPushed) {
                    startB.setBackgroundColor(Color.GREEN);
                    startB.setText("Recording!");

                    try {
                        buf_gra = new BufferedWriter(new FileWriter(file_gra, true));
                        buf_acc = new BufferedWriter(new FileWriter(file_acc, true));
                    } catch (IOException e) {
                        Log.i("Exception", e.toString());
                    }

                    count.resetSteps();

                }
                else {
                    startB.setBackgroundColor(Color.BLACK);
                    startB.setText("START!");

                    try {
                        buf_gra.close();
                        buf_acc.close();

                    } catch (IOException e) {
                        Log.i("Exception", e.toString());
                    }

                }
            }
        });
    }

    StepCount count=new StepCount(200);

    float[] lastGravity=new float[3];
    float[] lastAcc=new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (isPushed) {
                try {
                    buf_acc.append("" + x);
                    buf_acc.append("\t" + y);
                    buf_acc.append("\t" + z);
                    buf_acc.newLine();

                    buf_acc.flush();
                } catch (IOException e) {
                    Log.i("File", e.toString());
                }

                lastAcc[0]=x;
                lastAcc[1]=y;
                lastAcc[2]=z;

            }
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
        {
            float x=event.values[0];
            float y=event.values[1];
            float z=event.values[2];

            if(isPushed){
                try {
                    buf_gra.append("" + x);
                    buf_gra.append("\t" + y);
                    buf_gra.append("\t" + z);
                    buf_gra.newLine();

                    Log.d("gra", x+"\t"+y+"\t"+z);
                    buf_gra.flush();
                } catch (IOException e) {
                    Log.i("File", e.toString());
                }

                lastGravity[0]=x;
                lastGravity[1]=y;
                lastGravity[2]=z;

                count.input(lastGravity, lastAcc);
                txtSteps.setText(count.getSteps()+"");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not int use
    }
}
