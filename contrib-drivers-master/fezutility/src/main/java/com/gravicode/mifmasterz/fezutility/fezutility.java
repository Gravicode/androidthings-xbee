package com.gravicode.mifmasterz.fezutility;
import com.google.android.things.contrib.driver.ads7830.ads7830;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.gravicode.mifmasterz.pca9535.pca9535;
import com.google.android.things.contrib.driver.pca9685.pca9685;
import android.util.Log;

import java.io.IOException;
import  java.util.*;

/**
 * Created by mifmasterz on 7/24/17.
 */

public class fezutility implements AutoCloseable {

        private static final String TAG = fezutility.class.getSimpleName();

        private Boolean disposed;
        private pca9685 pwm;
        private pca9535 gpio;
        private ads7830 analog;
        public static final String I2cDeviceName = "I2C1";

        /// <summary>
        /// The frequency that the onboard PWM controller outputs. All PWM pins use the same frequency, only the duty cycle is controllable.
        /// </summary>
        /// <remarks>
        /// The range and granularity of the frequency are limited. The value of this property is closest to the value that the chip is actually capable of generating. It might not be exactly what you set.
        /// </remarks>
        public int getPwmFrequency() {
            try {
                return pwm.getFrequency();
            } catch (IOException ex) {
                Log.w("PWM", "Error get pwm :" + ex.getMessage());
            }
            return 0;
        }

    public void setPwmFrequency(int value) {
        try {
            pwm.setFrequency(value);
        } catch (IOException ex) {
            Log.w("PWM", "Error set pwm :" + ex.getMessage());
        }
    }


        /// <summary>
        /// Disposes of the object releasing control the pins.
        /// </summary>

    @Override
    public void close() throws IOException {
        Dispose(true);
    }

    private fezutility() {
        disposed = false;
    }

    /// <summary>
    /// Disposes of the object releasing control the pins.
    /// </summary>
    /// <param name="disposing">Whether or not this method is called from Dispose().</param>
    protected void Dispose(Boolean disposing) {
        if (!this.disposed) {
            if (disposing) {
                try {
                    this.pwm.close();
                    this.analog.close();
                    this.gpio.close();
                } catch (IOException ex) {
                    Log.w("Closing", "Dispose fail :" + ex.getMessage());
                }
            }

            this.disposed = true;
        }
    }

    /// <summary>
    /// Creates a new instance of the FEZ Utility.
    /// </summary>
    /// <returns>The new instance.</returns>
    public static fezutility CreateAsync() {
            try{
        PeripheralManagerService manager = new PeripheralManagerService();
                fezutility hat = new fezutility();
                hat.analog = new ads7830(I2cDeviceName);
                hat.pwm = new pca9685(I2cDeviceName, "BCM13");//gpioController.OpenPin(13)
                //hat.pwm.setOutputEnabled(true);
                //hat.pwm.setFrequency(1500);


        hat.gpio = new pca9535(I2cDeviceName, "BCM22");//gpioController.OpenPin(22)


        hat.gpio.Write((int)Led.Led1.getId(), false);
        hat.gpio.Write((int)Led.Led2.getId(), false);
        hat.gpio.SetDriveMode((int)Led.Led1.getId(), Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        hat.gpio.SetDriveMode((int)Led.Led2.getId(), Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        return hat;
            } catch (IOException e) {
                Log.w(TAG, "Unable to access GPIO", e);
                return null;
            }
    }

    /// <summary>
    /// Sets the duty cycle of the given pwm pin.
    /// </summary>
    /// <param name="pin">The pin to set the duty cycle for.</param>
    /// <param name="value">The new duty cycle between 0 (off) and 1 (on).</param>
    public void SetPwmDutyCycle(PwmPin pin, double value) throws IOException {
        if (value < 0.0 || value > 1.0) throw new IllegalArgumentException("invalid value");
        if(!getPwmPinEnums().contains(pin.getId())) throw new IllegalArgumentException("pin tidak terdaftar");

        this.pwm.SetDutyCycle((int)pin.getId(), value);
    }

    /// <summary>
    /// Sets the drive mode of the given pin.
    /// </summary>
    /// <param name="pin">The pin to set.</param>
    /// <param name="driveMode">The new drive mode of the pin.</param>
    public void SetDigitalDriveMode(DigitalPin pin, int driveMode) throws IOException {
        //if (!Enum.IsDefined(typeof(DigitalPin), pin)) throw new ArgumentException(nameof(pin));
        if(!getDigitalPinEnums().contains(pin.getId())) throw new IllegalArgumentException("pin tidak terdaftar");
        this.gpio.SetDriveMode((int)pin.getId(), driveMode);
    }

    /// <summary>
    /// Write the given value to the given pin.
    /// </summary>
    /// <param name="pin">The pin to set.</param>
    /// <param name="state">The new state of the pin.</param>
    public void WriteDigital(DigitalPin pin, Boolean state) throws IOException{
        //if (!Enum.IsDefined(typeof(DigitalPin), pin)) throw new ArgumentException(nameof(pin));
        if(!getDigitalPinEnums().contains(pin.getId())) throw new IllegalArgumentException("pin tidak terdaftar");
        this.gpio.Write((int)pin.getId(), state);
    }

    /// <summary>
    /// Reads the current state of the given pin.
    /// </summary>
    /// <param name="pin">The pin to read.</param>
    /// <returns>True if high, false is low.</returns>
    public Boolean ReadDigital(DigitalPin pin) throws IOException{
        //if (!Enum.IsDefined(typeof(DigitalPin), pin)) throw new ArgumentException(nameof(pin));
        if(!getDigitalPinEnums().contains(pin.getId())) throw new IllegalArgumentException("pin tidak terdaftar");
        return this.gpio.Read((int)pin.getId());
    }

    /// <summary>
    /// Sets the state of the given onboard LED.
    /// </summary>
    /// <param name="led">The LED to set.</param>
    /// <param name="state">The new state of the LED.</param>
    public void SetLedState(Led led, Boolean state) throws  IOException{
        //if (!Enum.IsDefined(typeof(Led), led)) throw new ArgumentException(nameof(led));
        if(!getLedEnums().contains(led.getId())) throw new IllegalArgumentException("led tidak terdaftar");
        if (led == Led.Led1 || led == Led.Led2) {
            this.gpio.Write((int)led.getId(), state);
        }
        else {
            this.pwm.SetDutyCycle((int)led.getId(), state ? 1.00 : 0.00);
        }
    }

    /// <summary>
    /// Reads the current voltage on the given pin.
    /// </summary>
    /// <param name="pin">The pin to read.</param>
    /// <returns>The voltage between 0 (0V) and 1 (3.3V).</returns>
    public double ReadAnalog(AnalogPin pin) throws  IOException{
        //if (!Enum.IsDefined(typeof(AnalogPin), pin)) throw new ArgumentException(nameof(pin));
        if(!getAnalogPinEnums().contains(pin.getId())) throw new IllegalArgumentException("pin tidak terdaftar");
        return this.analog.Read((byte)pin.getId());
    }

    /// <summary>
    /// The possible analog pins.
    /// </summary>
    public enum AnalogPin {
        /// <summary>An analog pin.</summary>
        A0 ( 0),
        /// <summary>An analog pin.</summary>
        A1 (1),
        /// <summary>An analog pin.</summary>
        A2 (2),
        /// <summary>An analog pin.</summary>
        A3 (3),
        /// <summary>An analog pin.</summary>
        A4 (4),
        /// <summary>An analog pin.</summary>
        A5 (5),
        /// <summary>An analog pin.</summary>
        A6 (6),
        /// <summary>An analog pin.</summary>
        A7 (7);
        private final int id;

        AnalogPin(int id) {
            this.id =  id;
        }

        public int getId() {
            return this.id;
        }
    }
    public static HashSet<Integer> getAnalogPinEnums() {

        HashSet<Integer> values = new HashSet<Integer>();

        for (AnalogPin c : AnalogPin.values()) {
            values.add(c.getId());
        }

        return values;
    }
    /// <summary>
    /// The possible pwm pins.
    /// </summary>
    public enum PwmPin {
        /// <summary>A pwm pin.</summary>
        P0 (0),
        /// <summary>A pwm pin.</summary>
        P1 (1),
        /// <summary>A pwm pin.</summary>
        P2 (2),
        /// <summary>A pwm pin.</summary>
        P3 (3),
        /// <summary>A pwm pin.</summary>
        P4 (4),
        /// <summary>A pwm pin.</summary>
        P5 (5),
        /// <summary>A pwm pin.</summary>
        P6 (6),
        /// <summary>A pwm pin.</summary>
        P7 (7),
        /// <summary>A pwm pin.</summary>
        P8 (8),
        /// <summary>A pwm pin.</summary>
        P9 (9),
        /// <summary>A pwm pin.</summary>
        P10 (10),
        /// <summary>A pwm pin.</summary>
        P11 (11),
        /// <summary>A pwm pin.</summary>
        P12 (12),
        /// <summary>A pwm pin.</summary>
        P13 (13);
        private final int id;

        PwmPin(int id) {
            this.id =  id;
        }

        public int getId() {
            return this.id;
        }
    }
    public static HashSet<Integer> getPwmPinEnums() {

        HashSet<Integer> values = new HashSet<Integer>();

        for (PwmPin c : PwmPin.values()) {
            values.add(c.getId());
        }

        return values;
    }
    /// <summary>
    /// The possible digital pins.
    /// </summary>
    public enum DigitalPin {
        /// <summary>A digital pin.</summary>
        V00 (0),
        /// <summary>A digital pin.</summary>
        V01 (1),
        /// <summary>A digital pin.</summary>
        V02 (2),
        /// <summary>A digital pin.</summary>
        V03 (3),
        /// <summary>A digital pin.</summary>
        V04 (4),
        /// <summary>A digital pin.</summary>
        V05 (5),
        /// <summary>A digital pin.</summary>
        V06 (6),
        /// <summary>A digital pin.</summary>
        V07 (7),
        /// <summary>A digital pin.</summary>
        V10 (10),
        /// <summary>A digital pin.</summary>
        V11 (11),
        /// <summary>A digital pin.</summary>
        V12 (12),
        /// <summary>A digital pin.</summary>
        V13 (13),
        /// <summary>A digital pin.</summary>
        V14 (14),
        /// <summary>A digital pin.</summary>
        V15  (15);
        private final int id;

        DigitalPin(int id) {
            this.id =  id;
        }

        public int getId() {
            return this.id;
        }
    }
    public static HashSet<Integer> getDigitalPinEnums() {

        HashSet<Integer> values = new HashSet<Integer>();

        for (DigitalPin c : DigitalPin.values()) {
            values.add(c.getId());
        }

        return values;
    }
    /// <summary>
    /// The possible LEDs.
    /// </summary>
    public enum Led {
        /// <summary>A digital pin.</summary>
        Led1 (16),
        /// <summary>A digital pin.</summary>
        Led2 (17),
        /// <summary>A digital pin.</summary>
        Led3 (14),
        /// <summary>A digital pin.</summary>
        Led4 (15);
        private final int id;

        Led(int id) {
            this.id =  id;
        }

        public int getId() {
            return this.id;
        }

    }

    public static HashSet<Integer> getLedEnums() {

        HashSet<Integer> values = new HashSet<Integer>();

        for (Led c : Led.values()) {
            values.add(c.getId());
        }

        return values;
    }
}

