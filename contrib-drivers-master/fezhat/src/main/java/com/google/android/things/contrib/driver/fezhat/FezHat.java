/*
 * Copyright 2016 Google Inc.
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

package com.google.android.things.contrib.driver.fezhat;

import android.support.annotation.StringDef;

import com.google.android.things.contrib.driver.ads7830.ads7830;
import com.google.android.things.contrib.driver.mma8453.mma8453;
import com.google.android.things.contrib.driver.pca9685.pca9685;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Driver factory for the Rainbow Hat.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FezHat {
    private static final String TAG = FezHat.class.getSimpleName();
    /*
    public static final String BUS_SENSOR = "I2C1";
    public static final String BUS_DISPLAY = "I2C1";
    public static final String BUS_LEDSTRIP = "SPI0.0";
    public static final String PWM_PIEZO = "PWM1";
    public static final String PWM_SERVO = "PWM0";
    @StringDef({BUTTON_A, BUTTON_B, BUTTON_C})
    public @interface ButtonPin {}
    public static final String BUTTON_A = "BCM21";
    public static final String BUTTON_B = "BCM20";
    public static final String BUTTON_C = "BCM16";
    public static final Button.LogicState BUTTON_LOGIC_STATE = Button.LogicState.PRESSED_WHEN_LOW;
    @StringDef({LED_RED, LED_GREEN, LED_BLUE})
    public @interface LedPin {}
    public static final String LED_RED = "BCM6";
    public static final String LED_GREEN = "BCM19";
    public static final String LED_BLUE = "BCM26";

    public static Bmx280 openSensor() throws IOException {
        return new Bmx280(BUS_SENSOR);
    }

    public static Bmx280SensorDriver createSensorDriver() throws IOException {
        return new Bmx280SensorDriver(BUS_SENSOR);
    }

    public static AlphanumericDisplay openDisplay() throws IOException {
        return new AlphanumericDisplay(BUS_SENSOR);
    }

    public static Speaker openPiezo() throws IOException {
        return new Speaker(PWM_PIEZO);
    }

    public static Servo openServo() throws IOException {
        return new Servo(PWM_SERVO);
    }

    public static Button openButton(@ButtonPin String pin) throws IOException {
        return new Button(pin, BUTTON_LOGIC_STATE);
    }

    static ButtonInputDriver createButtonInputDriver(@ButtonPin String pin, int keycode) throws IOException {
        return new ButtonInputDriver(pin, BUTTON_LOGIC_STATE, keycode);
    }

    public static Gpio openLed(@LedPin String pin) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        Gpio ledGpio = pioService.openGpio(pin);
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        return ledGpio;
    }

    public static Apa102 openLedStrip() throws IOException {
        return new Apa102(BUS_LEDSTRIP, Apa102.Mode.BGR);
    }
    */
    //Start Driver

    private boolean disposed;
    private pca9685 pwm;
    private ads7830 analog;
    private mma8453 accelerometer;
    private Gpio motorEnable;
    private Gpio dio16;
    private Gpio dio26;
    private Gpio dio24;
    private Gpio dio18;
    private Gpio dio22;

    /// <summary>
    /// The chip select line exposed on the header used for SPI devices.
    /// </summary>
    public static final int SpiChipSelectLine = 0;

    /// <summary>
    /// The SPI device name exposed on the header.
    /// </summary>
    public static final String SpiDeviceName = "SPI0.0";//SPI0

    /// <summary>
    /// The I2C device name exposed on the header.
    /// </summary>
    public static final String I2cDeviceName = "I2C1";

    /// <summary>
    /// The frequency that the onboard PWM controller outputs. All PWM pins use the same frequency, only the duty cycle is controllable.
    /// </summary>
    /// <remarks>
    /// Care needs to be taken when using the exposed PWM pins, motors, or servos. Motors generally require a high frequency while servos require a specific low frequency, usually 50Hz.
    /// If you set the frequency to a certain value, you may impair the ability of another part of the board to function.
    /// </remarks>
    public int getPwmFrequency ()throws IOException {

        return this.pwm.getFrequency();
    }
    public void setPwmFrequency(int PwmVal) throws IOException{
        this.pwm.setFrequency(PwmVal);

    }

    /// <summary>
    /// The object used to control the motor terminal labeled A.
    /// </summary>
    public Motor MotorA;

    /// <summary>
    /// The object used to control the motor terminal labeled A.
    /// </summary>
    public Motor MotorB;

    /// <summary>
    /// The object used to control the RGB led labeled D2.
    /// </summary>
    public RgbLed D2;

    /// <summary>
    /// The object used to control the RGB led labeled D3.
    /// </summary>
    public RgbLed D3;

    /// <summary>
    /// The object used to control the servo header labeled S1.
    /// </summary>
    public Servo S1;

    /// <summary>
    /// The object used to control the servo header labeled S2.
    /// </summary>
    public Servo S2;

    /// <summary>
    /// Whether or not the DIO24 led is on or off.
    /// </summary>
    public boolean getDIO24On() throws IOException{

            return this.dio24.getValue();
        }
    public void setDIO24On(boolean State) throws IOException {
        this.dio24.setValue(State);
    }


    /// <summary>
    /// Whether or not the button labeled DIO18 is pressed.
    /// </summary>
    /// <returns>The pressed state.</returns>
    public boolean IsDIO18Pressed() throws IOException{
       return this.dio18.getValue() == false;
    }

    /// <summary>
    /// Whether or not the button labeled DIO18 is pressed.
    /// </summary>
    /// <returns>The pressed state.</returns>
    public boolean IsDIO22Pressed() throws IOException {
        return this.dio22.getValue() == false;
    }

    /// <summary>
    /// Gets the light level from the onboard sensor.
    /// </summary>
    /// <returns>The light level between 0 (low) and 1 (high).</returns>
    public double GetLightLevel()throws IOException {
        return this.analog.Read(5);
    }

    /// <summary>
    /// Gets the temperature in celsius from the onboard sensor.
    /// </summary>
    /// <returns>The temperature.</returns>
    public double GetTemperature() throws IOException{
        return (this.analog.Read(4) * 3300.0 - 450.0) / 19.5;
    }

    /// <summary>
    /// Gets the acceleration in G's for each axis from the onboard sensor.
    /// </summary>
    /// <param name="x">The current X-axis acceleration.</param>
    /// <param name="y">The current Y-axis acceleration.</param>
    /// <param name="z">The current Z-axis acceleration.</param>
    public Akselerasi GetAcceleration() throws IOException {
        this.accelerometer.GetAcceleration();
        Akselerasi data = new Akselerasi();
        data.X = this.accelerometer.getX();
        data.Y = this.accelerometer.getY();
        data.Z = this.accelerometer.getZ();
        return data;

    }

    /// <summary>
    /// Disposes of the object releasing control the pins.
    /// </summary>
    public void Dispose() throws IOException{
        this.Dispose(true);
    }

    public FezHat() {
        this.disposed = false;
    }

    /// <summary>
    /// Disposes of the object releasing control the pins.
    /// </summary>
    /// <param name="disposing">Whether or not this method is called from Dispose().</param>
    protected void Dispose(boolean disposing)throws IOException  {
        if (!this.disposed) {
            if (disposing) {
                this.pwm.close();
                this.analog.close();
                this.accelerometer.close();
                this.motorEnable.close();
                this.dio16.close();
                this.dio26.close();
                this.dio24.close();
                this.dio18.close();
                this.dio22.close();

                this.MotorA.Dispose();
                this.MotorB.Dispose();
            }

            this.disposed = true;
        }
    }

    /// <summary>
    /// Creates a new instance of the FEZ HAT.
    /// </summary>
    /// <returns>The new instance.</returns>
    public static FezHat Create() {

        try {
            PeripheralManagerService manager = new PeripheralManagerService();

            //var gpioController = GpioController.GetDefault();
            //var i2cController = (await DeviceInformation.FindAllAsync(I2cDevice.GetDeviceSelector(FEZHAT.I2cDeviceName)))[0];
            FezHat hat = new FezHat();

            hat.accelerometer = new mma8453(I2cDeviceName);
            hat.analog = new ads7830(I2cDeviceName);

            hat.pwm = new pca9685(I2cDeviceName, "BCM13");//gpioController.OpenPin(13)
            hat.pwm.setOutputEnabled(true);
            hat.pwm.setFrequency(1500);

            hat.dio16 = manager.openGpio("BCM16");//gpioController.OpenPin(16);
            hat.dio26 = manager.openGpio("BCM26");//gpioController.OpenPin(26);
            hat.dio24 = manager.openGpio("BCM24");//gpioController.OpenPin(24);
            hat.dio18 = manager.openGpio("BCM18");//gpioController.OpenPin(18);xx
            hat.dio22 = manager.openGpio("BCM22");//gpioController.OpenPin(22);

            //hat.dio16.SetDriveMode(GpioPinDriveMode.Input);
            // Initialize the pin as an input
            hat.dio16.setDirection(Gpio.DIRECTION_IN);
            // High voltage is considered active
            hat.dio16.setActiveType(Gpio.ACTIVE_HIGH);

            //hat.dio26.SetDriveMode(GpioPinDriveMode.Input);
            // Initialize the pin as an input
            hat.dio26.setDirection(Gpio.DIRECTION_IN);
            // High voltage is considered active
            hat.dio26.setActiveType(Gpio.ACTIVE_HIGH);

            //hat.dio24.SetDriveMode(GpioPinDriveMode.Output);
            // Initialize the pin as a high output
            hat.dio24.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            // Low voltage is considered active
            hat.dio24.setActiveType(Gpio.ACTIVE_HIGH);

            //hat.dio18.SetDriveMode(GpioPinDriveMode.Input);
            // Initialize the pin as an input
            hat.dio18.setDirection(Gpio.DIRECTION_IN);
            // High voltage is considered active
            hat.dio18.setActiveType(Gpio.ACTIVE_HIGH);

            //hat.dio22.SetDriveMode(GpioPinDriveMode.Input);
            // Initialize the pin as an input
            hat.dio22.setDirection(Gpio.DIRECTION_IN);
            // High voltage is considered active
            hat.dio22.setActiveType(Gpio.ACTIVE_HIGH);

            hat.motorEnable = manager.openGpio("BCM12");//gpioController.OpenPin(12);
            //hat.motorEnable.SetDriveMode(GpioPinDriveMode.Output);
            // Initialize the pin as a high output
            hat.motorEnable.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            // Low voltage is considered active
            hat.motorEnable.setActiveType(Gpio.ACTIVE_HIGH);
            hat.motorEnable.setValue(true);

            //hat.MotorA = new Motor(hat.pwm, 14, 27, 23);
            hat.MotorA = new Motor(hat.pwm, 14, "BCM27", "BCM23");
            //hat.MotorB = new Motor(hat.pwm, 13, 6, 5);
            hat.MotorB = new Motor(hat.pwm, 13, "BCM6", "BCM5");

            hat.D2 = new RgbLed(hat.pwm, 1, 0, 2);
            hat.D3 = new RgbLed(hat.pwm, 4, 3, 15);

            hat.S1 = new Servo(hat.pwm, 9);
            hat.S2 = new Servo(hat.pwm, 10);

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
    public void SetPwmDutyCycle(PwmPin pin, double value) throws IOException{
        if (value < 0.0 || value > 1.0) throw new IllegalArgumentException("invalid value");
        //if (!Enum.IsDefined(typeof(PwmPin), pin)) throw new ArgumentException(nameof(pin));

        this.pwm.SetDutyCycle(pin.getValue(), value);
    }

    /// <summary>
    /// Write the given value to the given pin.
    /// </summary>
    /// <param name="pin">The pin to set.</param>
    /// <param name="state">The new state of the pin.</param>
    public void WriteDigital(DigitalPin pin, boolean state)throws IOException {
        //if (!Enum.IsDefined(typeof(DigitalPin), pin)) throw new ArgumentException(nameof(pin));

        Gpio gpioPin = pin == DigitalPin.DIO16 ? this.dio16 : this.dio26;

        //if (gpioPin. != GpioPinDriveMode.Output)
        //gpioPin.SetDriveMode(GpioPinDriveMode.Output);
        // Initialize the pin as a high output
        gpioPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        gpioPin.setActiveType(Gpio.ACTIVE_HIGH);

        gpioPin.setValue(state ? true : false);
    }

    /// <summary>
    /// Reads the current state of the given pin.
    /// </summary>
    /// <param name="pin">The pin to read.</param>
    /// <returns>True if high, false is low.</returns>
    public boolean ReadDigital(DigitalPin pin) throws IOException {
        //if (!Enum.IsDefined(typeof(DigitalPin), pin)) throw new ArgumentException(nameof(pin));

        Gpio gpioPin = pin == DigitalPin.DIO16 ? this.dio16 : this.dio26;

        //if (gpioPin.GetDriveMode() != GpioPinDriveMode.Input)
        //  gpioPin.SetDriveMode(GpioPinDriveMode.Input);

        // Initialize the pin as an input
        gpioPin.setDirection(Gpio.DIRECTION_IN);
        // High voltage is considered active
        gpioPin.setActiveType(Gpio.ACTIVE_HIGH);

        return gpioPin.getValue() == true;
    }

    /// <summary>
    /// Reads the current voltage on the given pin.
    /// </summary>
    /// <param name="pin">The pin to read.</param>
    /// <returns>The voltage between 0 (0V) and 1 (3.3V).</returns>
    public double ReadAnalog(AnalogPin pin)throws IOException {
        //if (!Enum.IsDefined(typeof(AnalogPin), pin)) throw new ArgumentException(nameof(pin));

        return this.analog.Read(pin.getValue());
    }

    /// <summary>
    /// The possible analog pins.
    /// </summary>
    public enum AnalogPin {
        /// <summary>An analog pin.</summary>
        Ain1 (1),
        /// <summary>An analog pin.</summary>
        Ain2 ( 2),
        /// <summary>An analog pin.</summary>
        Ain3 (3),
        /// <summary>An analog pin.</summary>
        Ain6 (6),
        /// <summary>An analog pin.</summary>
        Ain7 (7);
        private final int value;

        AnalogPin(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    /// <summary>
    /// The possible pwm pins.
    /// </summary>
    public enum PwmPin {
        /// <summary>A pwm pin.</summary>
        Pwm5 (5),
        /// <summary>A pwm pin.</summary>
        Pwm6 (6),
        /// <summary>A pwm pin.</summary>
        Pwm7 (7),
        /// <summary>A pwm pin.</summary>
        Pwm11 (11),
        /// <summary>A pwm pin.</summary>
        Pwm12 (12);

        private final int value;

        PwmPin(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    /// <summary>
    /// The possible digital pins.
    /// </summary>
    public enum DigitalPin {
        /// <summary>A digital pin.</summary>
        DIO16,
        /// <summary>A digital pin.</summary>
        DIO26
    }






}
