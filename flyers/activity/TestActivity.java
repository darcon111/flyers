package com.advancehdt.flyers.activity;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.advancehdt.flyers.R;

import java.util.List;

public class TestActivity extends AppCompatActivity implements SensorEventListener{

    TextView x,y,z;
    private  Sensor mAccelerometer;

    ImageView mDrawable;

    public static int x1 = 0;
    public static int y1 = 0;
    public static int z1 = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mDrawable = (ImageView) findViewById(R.id.imageView2);

        x = (TextView)findViewById(R.id.xID);

        y = (TextView)findViewById(R.id.yID);

        z = (TextView)findViewById(R.id.zID);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.x.setText("X = "+ String.format("%.2f", event.values[SensorManager.AXIS_X]) );

        this.y.setText("Y = "+ String.format("%.2f", event.values[SensorManager.AXIS_Y]));
        //this.z.setText("z = "+ String.format("%.2f", event.values[SensorManager.AXIS_Z]));

//        this.z.setText("Z = "+event.values[SensorManager.AXIS_Z]);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            x1 = (int)event.values[SensorManager.AXIS_X];
            y1 = (int)event.values[SensorManager.AXIS_Y];
            //z1 = (int)event.values[SensorManager.AXIS_Z];

            mDrawable.setY(y1);
            mDrawable.setX(x1);
            //mDrawable.setZ(z1);



        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    protected void onResume()

    {

        super.onResume();

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) //dispositivo android tiene acelerometro
        {

            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);

        }

    }

    protected void onPause()

    {

        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.unregisterListener(this, mAccelerometer);

        super.onPause();

    }

    protected void onStop()

    {

        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.unregisterListener(this, mAccelerometer);

        super.onStop();

    }

}
