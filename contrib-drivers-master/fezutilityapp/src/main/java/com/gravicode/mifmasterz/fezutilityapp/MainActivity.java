package com.gravicode.mifmasterz.fezutilityapp;

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

import java.io.IOException;
import java.net.URISyntaxException;

//fezhat board
import com.gravicode.mifmasterz.fezutility.fezutility;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.text.DecimalFormat;
import java.util.Set;

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
    private static final String TAG = MainActivity.class.getSimpleName();
    fezutility utility;
    Boolean next=true;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 500;
    private Handler mHandler = new Handler();

    protected TextView TxtLed;

    protected TextView TxtAnalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PeripheralManagerService service = new PeripheralManagerService();
            Log.d(TAG, "Available GPIO: " + service.getGpioList());
            Log.d(TAG, "onCreate");
            Setup();
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

    }



    private  void Setup() throws  IOException {
        TxtLed = (TextView) findViewById(R.id.txtLed);
        TxtAnalog = (TextView) findViewById(R.id.txtAnalog);

        this.utility = fezutility.CreateAsync();
        this.utility.SetDigitalDriveMode(fezutility.DigitalPin.V00, Gpio.DIRECTION_OUT_INITIALLY_HIGH);

    }


    protected Runnable mBlinkRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
                DecimalFormat formatter = new DecimalFormat("######.000");


                TxtLed.setText("Led : "+next);
                TxtAnalog.setText("A0 : "+ String.valueOf(utility.ReadAnalog(fezutility.AnalogPin.A0)));
                Log.e(TAG, "LED:" + TxtLed.getText());
                Log.e(TAG, "ANALOG:" + TxtAnalog.getText());

                utility.WriteDigital(fezutility.DigitalPin.V00, next);
                utility.SetPwmDutyCycle(fezutility.PwmPin.P0, next ? 1.0 : 0.0);
                utility.SetLedState(fezutility.Led.Led1, next);

                next = !next;


            } catch (IOException e) {
                Log.e(TAG, "Error on Jalan", e);

            }
        }
    };
}
