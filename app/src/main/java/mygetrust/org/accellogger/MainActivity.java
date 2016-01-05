package mygetrust.org.accellogger;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String LOG_TAG = "AccelLogger";

    private WakeLock wakeLock;

    private String filename;
    // String for filename
    public static String dateTimeForFilenameAsString;

    private SensorManager sensorManager;
    //private Sensor sensor;
    private Sensor sensorAcc;
    private Sensor sensorGyro;
    private Sensor sensorLight;
    private Sensor sensorPressure;
    private Sensor sensorGravity;
    private Sensor sensorLinearAcc;
    private Sensor sensorMagneticField;
    private Sensor sensorRotation;
    private Sensor sensorOrientation;
    private Sensor sensorGyroCorrected;

    private StringBuilder sb = new StringBuilder();

    private long timestamp = 0;

    private int count = 0;
    private int totalCount = 0;

    private float accX, accY, accZ;
    private float gyroX, gyroY, gyroZ;
    private float gyroCorrectedX, gyroCorrectedY, gyroCorrectedZ;
    private float rotationX, rotationY, rotationZ; //rotationTheta;
    private float linearAccX, linearAccY, linearAccZ;
    private float gravityX, gravityY, gravityZ;
    private float magneticFieldX, magneticFieldY, magneticFieldZ;
    private float orientationAzimuth, orientationPitch, orientationRoll;
    private float light, pressure;

    // UI elements
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Acquire a wake lock
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        //Sensor light = sensors.get(0);
        //Sensor pressure = sensors.get(2);
        //List<Sensor> temp = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        List<Sensor> gyroSensors = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        List<Sensor> accSensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> lightSensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        List<Sensor> pressureSensors = sensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        List<Sensor> gravitySensors = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        List<Sensor> linearAccSensors = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        List<Sensor> magneticFieldSensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        List<Sensor> relHumiditySensors = sensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        List<Sensor> rotationSensors = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        List<Sensor> orientationSensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //Sensor gyro = sensors.get(3);
        sensorAcc = accSensors.get(0);
        sensorGyro = gyroSensors.get(0);
        sensorLight = lightSensors.get(0);
        sensorPressure = pressureSensors.get(0);
        sensorGravity = gravitySensors.get(0);
        sensorLinearAcc = linearAccSensors.get(0);
        sensorMagneticField = magneticFieldSensors.get(0);
        sensorRotation = rotationSensors.get(0);
        sensorOrientation = orientationSensors.get(0);
        sensorGyroCorrected = gyroSensors.get(0);


        SimpleDateFormat dateTimeForFilename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        // Store start time as string for filename
        long startTime = System.currentTimeMillis();
        dateTimeForFilenameAsString = dateTimeForFilename.format(startTime);

        FileIO.setAccFile(dateTimeForFilenameAsString);
        filename = FileIO.getAccFile().toString();



        result = (TextView) findViewById(R.id.result);
        result.setText("No result yet");
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

    private void refreshDisplay() {

        String output =
                String.format("Time: %d\n", timeInMillis) +
                        String.format("Acc: x: %f / y: %f / z: %f\n", accX, accY, accZ) +
                        String.format("AccNG: x: %f / y: %f / z: %f\n", linearAccX, linearAccY, linearAccZ) +
                        String.format("Grav: x: %f / y: %f / z: %f\n", gravityX, gravityY, gravityZ) +
                        String.format("Gyro: x: %f / y: %f / z: %f\n", gyroX, gyroY, gyroZ) +
                        String.format("GyroC: x: %f / y: %f / z: %f\n", gyroCorrectedX, gyroCorrectedY, gyroCorrectedZ) +
                        String.format("Rot: x: %f / y: %f / z: %f\n", rotationX, rotationY, rotationZ) +
                        String.format("Orien: A: %f / R: %f / P: %f\n", orientationAzimuth, orientationRoll, orientationPitch) +
                        String.format("Light: %f lux\n", light) +
                        String.format("Atm Pressure: %f hPa\n", pressure) +
                        String.format("File line count: %d lines", totalCount);

        result.setText(output);
    }

    private long timeInMillis;
    private SensorEventListener sensorsListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensorAcc, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {

            long sensorReadingTimeMillis = System.currentTimeMillis();

            int type = event.sensor.getType();
            if (type == Sensor.TYPE_ACCELEROMETER){
                accX = event.values[0];
                accY = event.values[1];
                accZ = event.values[2];

                sb.append(sensorReadingTimeMillis + ", " + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ",\n");
                count++;

                if (count > 100){
                    totalCount = totalCount + 100;

                    if (FileIO.getAccFile() != null) {
                        Log.d(LOG_TAG, FileIO.getAccFile().toString());
                        FileIO.writeToAccFile(sb);
                        Log.d(LOG_TAG, "wrote to file " + filename.toString());
                    }
                    else {
                        File file = new File(FileIO.newDir("AccelLogger"), filename + "_acc.txt");
                        FileIO.writeToFile(sb, file);
                        Log.d(LOG_TAG, "wrote to new file " + file.toString());
                    }

                    sb.setLength(0);
                    count = 0;
                }
            }
            if (type == Sensor.TYPE_GYROSCOPE){
                if (event.sensor.getName().equals("Corrected Gyroscope Sensor")){
                    gyroCorrectedX = event.values[0];
                    gyroCorrectedY = event.values[1];
                    gyroCorrectedZ = event.values[2];
                }
                else {
                    gyroX = event.values[0];
                    gyroY = event.values[1];
                    gyroZ = event.values[2];
                }
            }
            if (type == Sensor.TYPE_LIGHT){
                light = event.values[0]; //Ambient light level in SI lux units
            }
            if (type == Sensor.TYPE_PRESSURE){
                pressure = event.values[0]; //Atmospheric pressure in hPa (millibar)
            }
            if (type == Sensor.TYPE_GRAVITY){
                gravityX = event.values[0];
                gravityY = event.values[1];
                gravityZ = event.values[2];
            }
            if (type == Sensor.TYPE_LINEAR_ACCELERATION){
                linearAccX = event.values[0];
                linearAccY = event.values[1];
                linearAccZ = event.values[2];
            }
            if (type == Sensor.TYPE_MAGNETIC_FIELD){
                magneticFieldX = event.values[0];
                magneticFieldY = event.values[1];
                magneticFieldZ = event.values[2];
            }
            if (type == Sensor.TYPE_ROTATION_VECTOR){
                rotationX = event.values[0];
                rotationY = event.values[1];
                rotationZ = event.values[2];
                //rotationTheta = event.values[3];
            }
            if (type == Sensor.TYPE_ORIENTATION){
                orientationAzimuth = event.values[0];
                orientationPitch = event.values[1];
                orientationRoll = event.values[2];
            }
            if (event.timestamp - timestamp > 100000000){
                timeInMillis = (new Date()).getTime()
                        + (event.timestamp - System.nanoTime()) / 1000000L;
                timestamp = event.timestamp;
                refreshDisplay();
            }
        }

    };


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorsListener, sensorAcc,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorGyro,0
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorLight,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorPressure,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorGravity,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorLinearAcc,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorMagneticField,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorRotation,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorOrientation,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorsListener, sensorGyroCorrected,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(sensorsListener);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        // Scan for new files so they will be visible

        FileIO.scanNewFiles(FileIO.getAccFile(), getApplicationContext());

        wakeLock.release();
    }


}
