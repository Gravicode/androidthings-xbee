/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.myproject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
//fezhat board
import com.google.android.things.contrib.driver.fezhat.FezHat;
import com.google.android.things.contrib.driver.fezhat.Akselerasi;
import com.google.android.things.contrib.driver.fezhat.Color;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import com.google.gson.Gson;
import java.text.DecimalFormat;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidthings.myproject.classifier.Classifier;
import com.example.androidthings.myproject.classifier.TensorFlowImageClassifier;

//bisa dibuang
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 */

public class MainActivity extends Activity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int INTERVAL_BETWEEN_BLINKS_MS = 500;
    private Handler mHandler = new Handler();

    private FezHat hat;
    private boolean next;
    private int i;

    protected TextView TxtStatus;
    protected TextView TxtTemp;
    protected TextView TxtLight;
    protected Button BtnReceive;
    protected TextView TxtAccel;

    //tensorflow app
    private static final int PERMISSIONS_REQUEST = 1;

    private ImagePreprocessor mImagePreprocessor;
    private TextToSpeech mTtsEngine;
    private TtsSpeaker mTtsSpeaker;
    private CameraHandler mCameraHandler;
    private TensorFlowImageClassifier mTensorFlowClassifier;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private ImageView mImage;
    private TextView[] mResultViews;

    private AtomicBoolean mReady = new AtomicBoolean(false);

    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        if (hasPermission()) {
            if (savedInstanceState == null) {
                init();
            }
        } else {
            requestPermission();
        }

    }

    protected void Setup() throws URISyntaxException, IOException {
        TxtAccel = (TextView) findViewById(R.id.txtAccel);
        TxtLight = (TextView) findViewById(R.id.txtLight);
        TxtTemp = (TextView) findViewById(R.id.txtTemp);
        mImage = (ImageView) findViewById(R.id.imageView);
        mResultViews = new TextView[3];
        mResultViews[0] = (TextView) findViewById(R.id.result1);
        mResultViews[1] = (TextView) findViewById(R.id.result2);
        mResultViews[2] = (TextView) findViewById(R.id.result3);

        /*
        TxtStatus = (TextView) findViewById(R.id.txtStatus);
        BtnReceive = (Button) findViewById(R.id.btnReceive);
        BtnReceive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ReceiveData();
                } catch (IOException io) {

                } catch (URISyntaxException es) {

                }
            }
        });
        */
        this.hat = FezHat.Create();
        this.hat.S1.SetLimits(500, 2400, 0, 180);
        this.hat.S2.SetLimits(500, 2400, 0, 180);
        //set as not ready
        this.hat.D2.setColor(Color.Red());
        this.hat.D3.setColor(Color.Red());

    }

    public void SendMessage(SensorData data) throws URISyntaxException, IOException {
        Gson gson = new Gson();
        String msgStr = gson.toJson(data);
        try {
            myRef.setValue(msgStr);

        } catch (Exception e) {
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mBackgroundThread != null) mBackgroundThread.quit();
        } catch (Throwable t) {
            // close quietly
        }
        mBackgroundThread = null;
        mBackgroundHandler = null;

        try {
            if (mCameraHandler != null) mCameraHandler.shutDown();
        } catch (Throwable t) {
            // close quietly
        }
        try {
            if (mTensorFlowClassifier != null) mTensorFlowClassifier.close();
        } catch (Throwable t) {
            // close quietly
        }

        if (mTtsEngine != null) {
            mTtsEngine.stop();
            mTtsEngine.shutdown();
        }
        Log.d(TAG, "onDestroy");
        mHandler.removeCallbacks(mBlinkRunnable);


    }

    // Permission-related methods. This is not needed for Android Things, where permissions are
    // automatically granted. However, it is kept here in case the developer needs to test on a
    // regular Android device.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(CAMERA) ||
                    shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Camera AND storage permission are required for this demo",
                        Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }
    }


    private void init() {
        //init firebase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("telemetry");
        //received data
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //init peripheral
        try {
            PeripheralManagerService service = new PeripheralManagerService();
            Log.d(TAG, "Available GPIO: " + service.getGpioList());
            Log.d(TAG, "onCreate");
            try {
                Setup();
            } catch (URISyntaxException ex) {


            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        mHandler.post(mBlinkRunnable);
        //run background process for TF
        mBackgroundThread = new HandlerThread("BackgroundThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mBackgroundHandler.post(mInitializeOnBackground);
    }


    private Runnable mInitializeOnBackground = new Runnable() {
        @Override
        public void run() {
            mImagePreprocessor = new ImagePreprocessor(CameraHandler.IMAGE_WIDTH,
                    CameraHandler.IMAGE_HEIGHT, TensorFlowImageClassifier.INPUT_SIZE);

            mTtsSpeaker = new TtsSpeaker();
            mTtsSpeaker.setHasSenseOfHumor(true);
            mTtsEngine = new TextToSpeech(MainActivity.this,
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                mTtsEngine.setLanguage(Locale.US);
                                mTtsEngine.setOnUtteranceProgressListener(utteranceListener);
                                mTtsSpeaker.speakReady(mTtsEngine);
                            } else {
                                Log.w(TAG, "Could not open TTS Engine (onInit status=" + status
                                        + "). Ignoring text to speech");
                                mTtsEngine = null;
                            }
                        }
                    });
            mCameraHandler = CameraHandler.getInstance();
            mCameraHandler.initializeCamera(
                    MainActivity.this, mBackgroundHandler,
                    MainActivity.this);

            mTensorFlowClassifier = new TensorFlowImageClassifier(MainActivity.this);

            setReady(true);
        }
    };

    private Runnable mBackgroundClickHandler = new Runnable() {
        @Override
        public void run() {
            if (mTtsEngine != null) {
                mTtsSpeaker.speakShutterSound(mTtsEngine);
            }
            mCameraHandler.takePicture();
        }
    };

    private UtteranceProgressListener utteranceListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            setReady(false);
        }

        @Override
        public void onDone(String utteranceId) {
            setReady(true);
        }

        @Override
        public void onError(String utteranceId) {
            setReady(true);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Received key down: " + keyCode + ". Ready = " + mReady.get());
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setReady(boolean ready) {
        mReady.set(ready);
            try {
                if(ready){
                    hat.D2.setColor(Color.Green());
                    hat.D3.setColor(Color.Green());
                }else{
                    hat.D2.setColor(Color.Yellow());
                    hat.D3.setColor(Color.Yellow());
                }


            } catch (IOException e) {
                Log.w(TAG, "Could not set LED", e);
            }
    }

    private void TakePicture(){
        if (mReady.get()) {
            setReady(false);
            mBackgroundHandler.post(mBackgroundClickHandler);
        } else {
            Log.i(TAG, "Sorry, processing hasn't finished. Try again in a few seconds");
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Bitmap bitmap;
        try (Image image = reader.acquireNextImage()) {
            bitmap = mImagePreprocessor.preprocessImage(image);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImage.setImageBitmap(bitmap);
            }
        });

        final List<Classifier.Recognition> results = mTensorFlowClassifier.recognizeImage(bitmap);

        Log.d(TAG, "Got the following results from Tensorflow: " + results);
        if (mTtsEngine != null) {
            // speak out loud the result of the image recognition
            mTtsSpeaker.speakResults(mTtsEngine, results);
        } else {
            // if theres no TTS, we don't need to wait until the utterance is spoken, so we set
            // to ready right away.
            setReady(true);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mResultViews.length; i++) {
                    if (results.size() > i) {
                        Classifier.Recognition r = results.get(i);
                        mResultViews[i].setText(r.toString());
                    } else {
                        mResultViews[i].setText(null);
                    }
                }
            }
        });
    }

    protected Runnable mBlinkRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
                DecimalFormat formatter = new DecimalFormat("######.000");
                SensorData data = new SensorData();
                Akselerasi accel = hat.GetAcceleration();
                data.Light = hat.GetLightLevel();
                data.Temp = hat.GetTemperature();
                data.Accelleration = "X:" + String.valueOf(accel.X) + " Y: " + String.valueOf(accel.Y) + " Z:" + String.valueOf(accel.Z);
                String lightStr = String.valueOf(data.Light);
                String Temp = formatter.format(data.Temp);
                String AccelStr = data.Accelleration;
                String Btn18Str = String.valueOf(hat.IsDIO18Pressed());
                String Btn22Str = String.valueOf(hat.IsDIO22Pressed());
                String AnalogStr = String.valueOf(hat.ReadAnalog(FezHat.AnalogPin.Ain1));
                TxtLight.setText(lightStr + " Lux");
                TxtAccel.setText(AccelStr);
                TxtTemp.setText(Temp + " C");
                Log.e(TAG, "Light:" + lightStr);
                Log.e(TAG, "Temp:" + Temp);
                Log.e(TAG, "Acceleration:" + AccelStr);
                if(hat.IsDIO18Pressed() || hat.IsDIO22Pressed()){
                    TakePicture();
                }
                try {
                    SendMessage(data);
                }catch (IOException ex){

                }catch (URISyntaxException ex){

                }
                /*
                if ((i++ % 5) == 0) {
                    String LedsTextBox = String.valueOf(next);

                    hat.setDIO24On(next);
                    hat.D2.setColor(next ? Color.Green() : Color.Black());
                    hat.D3.setColor(next ? Color.Green() : Color.Black());

                    hat.WriteDigital(FezHat.DigitalPin.DIO16, next);
                    hat.WriteDigital(FezHat.DigitalPin.DIO26, next);

                    hat.SetPwmDutyCycle(FezHat.PwmPin.Pwm5, next ? 1.0 : 0.0);
                    hat.SetPwmDutyCycle(FezHat.PwmPin.Pwm6, next ? 1.0 : 0.0);
                    hat.SetPwmDutyCycle(FezHat.PwmPin.Pwm7, next ? 1.0 : 0.0);
                    hat.SetPwmDutyCycle(FezHat.PwmPin.Pwm11, next ? 1.0 : 0.0);
                    hat.SetPwmDutyCycle(FezHat.PwmPin.Pwm12, next ? 1.0 : 0.0);

                    next = !next;
                }

                if (hat.IsDIO18Pressed()) {
                    hat.S1.setPosition(hat.S1.getPosition() + 5.0);
                    hat.S2.setPosition(hat.S2.getPosition() + 5.0);

                    if (hat.S1.getPosition() >= 180.0) {
                        hat.S1.setPosition(0.0);
                        hat.S2.setPosition(0.0);
                    }
                }

                if (hat.IsDIO22Pressed()) {
                    if (hat.MotorA.getSpeed() == 0.0) {
                        hat.MotorA.setSpeed(0.7);
                        hat.MotorB.setSpeed(-0.7);
                    }
                } else {
                    if (hat.MotorA.getSpeed() != 0.0) {
                        hat.MotorA.setSpeed(0.0);
                        hat.MotorB.setSpeed(0.0);
                    }
                }
                */
            } catch (IOException e) {
                Log.e(TAG, "Error on Jalan", e);

            }
        }
    };

}

/*
public class ImageClassifierActivity extends Activity implements ImageReader.OnImageAvailableListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}*/