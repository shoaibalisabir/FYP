package com.example.fyp;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MySensorService extends Service implements SensorEventListener {

    float  xAccel,yAecel, zAccel;
    float  xPrevicusAccel, yPreviousAccel, zPreviousAceel;
    boolean  firstUpdate  =  true;
    boolean  shakeInitiated =  false;
    float  shakeThreshold = 12.5f;
    Sensor  accelerometer;
    SensorManager sm;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        sm  = 	(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer 	=   sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        updateAccelParameters(event.values[0],event.values[1],event.values[2]);

        if ((!shakeInitiated) && isAccelerationChanged()){
            shakeInitiated=true;
        }
        else if ((shakeInitiated) && (isAccelerationChanged())){
            executeshakeaction();
        }
        else if ((shakeInitiated) && (!isAccelerationChanged())){
            shakeInitiated=false;
        }

    }

    private void executeshakeaction() {
        Intent intent=new Intent(MySensorService.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isAccelerationChanged() {
        float deltaX=Math.abs(xPrevicusAccel-xAccel);
        float deltaY=Math.abs(yPreviousAccel-yAecel);
        float deltaZ=Math.abs(zPreviousAceel-zAccel);

        return (deltaX>shakeThreshold && deltaY>shakeThreshold)
                || (deltaX>shakeThreshold && deltaZ>shakeThreshold)
                || (deltaY>shakeThreshold && deltaZ>shakeThreshold);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private  void  updateAccelParameters(float  xNewAccel,float yNewAccel,float zNewAccel)
    {
        if(firstUpdate)
        {
            xPrevicusAccel 	=  xNewAccel;
            yPreviousAccel=yNewAccel;
            zPreviousAceel=zNewAccel;
            firstUpdate 	=  false;
        }
        else {

            xPrevicusAccel=xNewAccel;
            yPreviousAccel=yNewAccel;
            zPreviousAceel=zNewAccel;

        }
    }
}
