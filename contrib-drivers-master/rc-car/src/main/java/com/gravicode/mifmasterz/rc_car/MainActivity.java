package com.gravicode.mifmasterz.rc_car;

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

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


import com.google.gson.Gson;

import java.text.DecimalFormat;
/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static String Arah="S";

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int INTERVAL_BETWEEN_BLINKS_MS = 50;

    private Handler mHandler = new Handler();

    private FezHat hat;

    protected TextView TxtStatus;

    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PeripheralManagerService service = new PeripheralManagerService();
            Log.d(TAG, "Available GPIO: " + service.getGpioList());
            Log.d(TAG, "onCreate");
            try {
                Setup();
            } catch (URISyntaxException ex) {

            }
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

    }

    protected void Setup() throws URISyntaxException, IOException {
        //init firebase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("robot");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                Arah = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        TxtStatus = (TextView) findViewById(R.id.txtStatus);
        this.hat = FezHat.Create();
        this.hat.S1.SetLimits(500, 2400, 0, 180);
        this.hat.S2.SetLimits(500, 2400, 0, 180);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mHandler.removeCallbacks(mBlinkRunnable);


    }

    protected Runnable mBlinkRunnable = new Runnable() {

        @Override
        public void run() {


            try {


                switch (Arah){
                    case "S":
                        hat.MotorA.setSpeed(0.0);
                        hat.MotorB.setSpeed(0.0);
                        hat.D2.setColor(Color.Red());
                        hat.D3.setColor(Color.Red());
                    break;
                    case "L":
                        hat.MotorA.setSpeed(-0.7);
                        hat.MotorB.setSpeed(0.7);
                        hat.D2.setColor(Color.Cyan());
                        hat.D3.setColor(Color.Cyan());
                        break;
                    case "R":
                        hat.MotorA.setSpeed(0.7);
                        hat.MotorB.setSpeed(-0.7);
                        hat.D2.setColor(Color.Magneta());
                        hat.D3.setColor(Color.Magneta());
                        break;
                    case "F":
                        hat.MotorA.setSpeed(0.7);
                        hat.MotorB.setSpeed(0.7);
                        hat.D2.setColor(Color.Green());
                        hat.D3.setColor(Color.Green());

                        break;
                    case "B":
                        hat.MotorA.setSpeed(-0.7);
                        hat.MotorB.setSpeed(-0.7);
                        hat.D2.setColor(Color.Yellow());
                        hat.D3.setColor(Color.Yellow());
                        break;


                }
                Log.d(TAG, "ARAH:"+Arah);
                TxtStatus.setText("Arah : "+Arah);
                //sending data to azure
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);


            } catch (IOException e) {
                Log.e(TAG, "Error on Jalan", e);

            }

        }
    };


}
