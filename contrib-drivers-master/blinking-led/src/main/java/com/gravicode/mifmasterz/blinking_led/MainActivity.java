package com.gravicode.mifmasterz.blinking_led;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.things.contrib.driver.fezhat.FezHat;
import com.google.android.things.contrib.driver.fezhat.Akselerasi;
import com.google.android.things.contrib.driver.fezhat.Color;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;


import java.io.IOException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        try {
            BlinkLed();
        }catch (IOException ex){

        }
    }

    void BlinkLed() throws  IOException{
        FezHat hat = FezHat.Create();
        hat.D2.setColor(Color.Yellow());
        hat.D3.setColor(Color.Green());
    }
}
