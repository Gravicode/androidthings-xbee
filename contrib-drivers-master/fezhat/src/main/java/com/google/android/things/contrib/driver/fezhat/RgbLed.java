package com.google.android.things.contrib.driver.fezhat;

/**
 * Created by mifmasterz on 12/29/16.
 */

import android.support.annotation.StringDef;

import com.google.android.things.contrib.driver.pca9685.pca9685;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
/// <summary>
/// Represents an onboard RGB led.
/// </summary>
public class RgbLed {
    private pca9685 pwm;
    private Color color;
    private int redChannel;
    private int greenChannel;
    private int blueChannel;

    /// <summary>
    /// The current color of the LED.
    /// </summary>
    public Color getColor() {

        return this.color;
    }

    public void setColor(Color ColorVal) throws IOException {
        this.color = ColorVal;

        this.pwm.SetDutyCycle(this.redChannel, (double) ColorVal.R / 255.0);
        this.pwm.SetDutyCycle(this.greenChannel,(double) ColorVal.G / 255.0);
        this.pwm.SetDutyCycle(this.blueChannel, (double) ColorVal.B / 255.0);

    }

    public RgbLed(pca9685 pwm, int redChannel, int greenChannel, int blueChannel) {
        this.color = Color.Black();
        this.pwm = pwm;
        this.redChannel = redChannel;
        this.greenChannel = greenChannel;
        this.blueChannel = blueChannel;
    }

    /// <summary>
    /// Turns the LED off.
    /// </summary>
    public void TurnOff() throws IOException {
        this.pwm.SetDutyCycle(this.redChannel, 0.0);
        this.pwm.SetDutyCycle(this.greenChannel, 0.0);
        this.pwm.SetDutyCycle(this.blueChannel, 0.0);
    }
}