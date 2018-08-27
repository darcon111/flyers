package com.advancehdt.flyers.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.advancehdt.flyers.R;
import com.advancehdt.flyers.clases.Position;
import com.example.panorama.NativePanorama;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends Activity implements SensorEventListener {
    static{
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    private Button captureBtn, saveBtn; // used to interact with capture and save Button in UI
    private SurfaceView mSurfaceView, mSurfaceViewOnTop; // used to display the camera frame in UI
    private Camera mCam;
    private boolean isPreview; // Is the camera frame displaying?
    private boolean safeToTakePicture = true; // Is it safe to capture a picture?
    private int camId = 1;
    private List<Mat> listImage = new ArrayList<>();
    public int position = 0;
    public ArrayList<Position> mList = new ArrayList<Position>();


    private SensorManager mSensorManager;
    // Accelerometer and magnetometer sensors, as retrieved from the
    // sensor manager.
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    // Current data from accelerometer & magnetometer.  The arrays hold values
    // for X, Y, and Z.
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];
    private ImageButton mCentro,mArrow;

    // System display. Need this for determining rotation.
    private Display mDisplay;
    private static final float VALUE_DRIFT = 0.05f;
    private int countPhoto=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        isPreview = false;
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        mSurfaceViewOnTop = (SurfaceView)findViewById(R.id.surfaceViewOnTop);
        mSurfaceViewOnTop.setZOrderOnTop(true);    // necessary
        mSurfaceViewOnTop.getHolder().setFormat(PixelFormat.TRANSPARENT);

        captureBtn = (Button) findViewById(R.id.capture);
        captureBtn.setOnClickListener(captureOnClickListener);

        captureBtn.setVisibility(View.INVISIBLE);

        saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(saveOnClickListener);

        saveBtn.setEnabled(false);

        mCentro = (ImageButton) findViewById(R.id.imgCentro);
        mCentro.setVisibility(View.INVISIBLE);

        mArrow = (ImageButton) findViewById(R.id.imgArrow);
        mArrow.setVisibility(View.INVISIBLE);

        // Get accelerometer and magnetometer sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        // Get the display from the window manager (for rotation).
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = wm.getDefaultDisplay();






        mList.add(new Position(9.7,1.4));



    }



    View.OnClickListener captureOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCam != null && safeToTakePicture){
                // set the flag to false so we don't take two picture at a same time
                safeToTakePicture = false;
                mCam.takePicture(null, null, jpegCallback);
                countPhoto++;
            }
        }
    };
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the byte array to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            // Rotate the picture to fit portrait mode
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);


            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            listImage.add(mat);

            Log.d("Vision", "Height " + mat.rows() + " Width: " + mat.cols());

            Canvas canvas = null;
            try {
                canvas = mSurfaceViewOnTop.getHolder().lockCanvas(null);
                synchronized (mSurfaceViewOnTop.getHolder()) {
                    // Clear canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    // Scale the image to fit the SurfaceView
                    float scale = 1.0f * mSurfaceView.getHeight() / bitmap.getHeight();
                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap, (int)(scale * bitmap.getWidth()), mSurfaceView.getHeight() , false);
                    Paint paint = new Paint();
                    // Set the opacity of the image
                    paint.setAlpha(200);
                    // Draw the image with an offset so we only see one third of image.
                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    mSurfaceViewOnTop.getHolder().unlockCanvasAndPost(canvas);
                }
            }
            // Start preview the camera again and set the take picture flag to true
            mCam.startPreview();
            safeToTakePicture = true;
        }
    };

    View.OnClickListener saveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCentro.setVisibility(View.GONE);
            mArrow.setVisibility(View.GONE);

            Thread thread = new Thread(imageProcessingRunnable);
            thread.start();
        }
    };

    ProgressDialog ringProgressDialog;

    private Runnable imageProcessingRunnable = new Runnable() {
        @Override
        public void run() {


            showProcessingDialog();

            try {
                // Create a long array to store all image address
                int elems=  listImage.size();
                long[] tempobjadr = new long[elems];
                for (int i=0;i<elems;i++){
                    tempobjadr[i]=  listImage.get(i).getNativeObjAddr();
                }
                // Create a Mat to store the final panorama image
                Mat result = new Mat();

                // Call the Open CV C++ Code to perform stitching process
                //processPanorama(tempobjadr, result.getNativeObjAddr());

                NativePanorama.processPanorama(tempobjadr, result.getNativeObjAddr());



                Log.d("Vision", "Height " + result.rows() + " Width: " + result.cols());
                // Save the image to internal storage
                File sdcard = Environment.getExternalStorageDirectory();
                final String fileName = sdcard.getAbsolutePath() + "/opencv_" +  System.currentTimeMillis() + ".png";
                Imgcodecs.imwrite(fileName, result);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "File saved at: " + fileName, Toast.LENGTH_LONG).show();
                    }
                });

                listImage.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

            closeProcessingDialog();
        }
    };

    private void showProcessingDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCam.stopPreview();
                ringProgressDialog = ProgressDialog.show(Main2Activity.this, "",	"Panorama", true);
                ringProgressDialog.setCancelable(false);
            }
        });
    }
    private void closeProcessingDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCam.startPreview();
                ringProgressDialog.dismiss();
            }
        });
    }

    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback(){
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                // Tell the camera to display the frame on this surfaceview
                mCam.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Get the default parameters for camera
            Camera.Parameters myParameters = mCam.getParameters();
            // Select the best preview size
            Camera.Size myBestSize = getBestPreviewSize( myParameters );

            if(myBestSize != null){
                // Set the preview Size
                myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
                // Set the parameters to the camera
                mCam.setParameters(myParameters);
                // Rotate the display frame 90 degree to view in portrait mode
                mCam.setDisplayOrientation(90);
                // Start the preview
                mCam.startPreview();
                isPreview = true;

                /*Toast.makeText(getApplicationContext(),
                        "Best Size:\n" +
                                String.valueOf(myBestSize.width) + " : " + String.valueOf(myBestSize.height),
                        Toast.LENGTH_LONG).show();*/
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    private void saveBitmap(Bitmap bmp){
        String filename = "/sdcard/testPano.bmp";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void releaseCameraAndPreview() {
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onStop().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL).
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        mSensorManager.unregisterListener(this);
    }



    @Override
    protected void onResume() {
        super.onResume();

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) //dispositivo android tiene acelerometro
        {

            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);

        }

        try {
            releaseCameraAndPreview();
            if (camId == 0) {
                mCam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            else {
                mCam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();


        if(isPreview){
            mCam.stopPreview();
        }
        mCam.release();
        mCam = null;
        isPreview = false;
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        // The sensor type (as defined in the Sensor class).
        int sensorType = event.sensor.getType();

        // The sensorEvent object is reused across calls to onSensorChanged().
        // clone() gets a copy so the data doesn't change out from under us
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = event.values.clone();
                break;
            default:
                return;
        }
        // Compute the rotation matrix: merges and translates the data
        // from the accelerometer and magnetometer, in the device coordinate
        // system, into a matrix in the world's coordinate system.
        //
        // The second argument is an inclination matrix, which isn't
        // used in this example.
        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        // Remap the matrix based on current device/activity rotation.
        float[] rotationMatrixAdjusted = new float[9];
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                rotationMatrixAdjusted = rotationMatrix.clone();
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                        rotationMatrixAdjusted);
                break;
            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                        rotationMatrixAdjusted);
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                        rotationMatrixAdjusted);
                break;



        }

        // Get the orientation of the device (azimuth, pitch, roll) based
        // on the rotation matrix. Output units are radians.
        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrixAdjusted,
                    orientationValues);
        }

        // Pull out the individual values from the array.
        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

        // Pitch and roll values that are close to but not 0 cause the
        // animation to flash a lot. Adjust pitch and roll to 0 for very
        // small values (as defined by VALUE_DRIFT).
        if (Math.abs(pitch) < VALUE_DRIFT) {
            pitch = 0;
        }
        if (Math.abs(roll) < VALUE_DRIFT) {
            roll = 0;
        }



        // Set spot color (alpha/opacity) equal to pitch/roll.
        // this is not a precise grade (pitch/roll can be greater than 1)
        // but it's close enough for the animation effect.
        if (pitch > 0) {

        } else {

            if (Math.abs(pitch)>=1.49 &&  Math.abs(pitch)<=1.57) {

                mCentro.setVisibility(View.VISIBLE);
                captureBtn.setVisibility(View.VISIBLE);
                if(countPhoto>0)
                {
                    mArrow.setVisibility(View.VISIBLE);
                    saveBtn.setEnabled(true);
                }

            }else
            {

                captureBtn.setVisibility(View.INVISIBLE);
                mCentro.setVisibility(View.INVISIBLE);

            }

        }








/*

        x1 = event.values[SensorManager.AXIS_X];
        y1 = event.values[SensorManager.AXIS_Y];


        DecimalFormat twoDForm = new DecimalFormat("#.#");

        if(position<mList.size()) {


            if (mList.get(position).getX() == Double.valueOf(twoDForm.format(x1)) && mList.get(position).getY() == Double.valueOf(twoDForm.format(y1))) {
                position++;
                if (mCam != null && safeToTakePicture) {
                    // set the flag to false so we don't take two picture at a same time
                    safeToTakePicture = false;
                    mCam.takePicture(null, null, jpegCallback);
                    Toast.makeText(getApplicationContext(), "foto", Toast.LENGTH_LONG).show();
                }


            }

        }else
        {
            SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);

            mSensorManager.unregisterListener(this, mAccelerometer);
        }

        */

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}

