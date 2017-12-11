package com.google.android.things.contrib.driver.fezhat;

/**
 * Created by mifmasterz on 12/29/16.
 */
import android.support.annotation.StringDef;

import com.google.android.things.contrib.driver.pca9685.pca9685;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class Servo {
    private pca9685 pwm;
    private int channel;
    private double position;
    private double minAngle;
    private double maxAngle;
    private double scale;
    private double offset;
    private boolean limitsSet;

    /// <summary>
    /// The current position of the servo between the minimumAngle and maximumAngle passed to SetLimits.
    /// </summary>
    public double getPosition() {

            return this.position;
        }
    public void setPosition(double PositionVal) throws IOException{
        if (!this.limitsSet)
            throw new IllegalStateException("You must call this.SetLimits first.");
        if (PositionVal < this.minAngle || PositionVal > this.maxAngle)
            throw new IllegalArgumentException("Out of Range Position");

        this.position = PositionVal;

        this.pwm.SetChannel(this.channel, (short) 0x0000, (short) (this.scale * PositionVal + this.offset));

    }

    public Servo(pca9685 pwm, int channel) {
        this.pwm = pwm;
        this.channel = channel;
        this.position = 0.0;
        this.limitsSet = false;
    }

    /// <summary>
    /// Sets the limits of the servo.
    /// </summary>
    /// <param name="minimumPulseWidth">The minimum pulse width in milliseconds.</param>
    /// <param name="maximumPulseWidth">The maximum pulse width in milliseconds.</param>
    /// <param name="minimumAngle">The minimum angle of input passed to Position.</param>
    /// <param name="maximumAngle">The maximum angle of input passed to Position.</param>
    public void SetLimits(int minimumPulseWidth, int maximumPulseWidth, double minimumAngle, double maximumAngle)throws IOException {
        if (minimumPulseWidth < 0) throw new IllegalArgumentException("Wrong minimumPulseWidth");
        if (maximumPulseWidth < 0) throw new IllegalArgumentException("Wrong maximumPulseWidth");
        if (minimumAngle < 0) throw new IllegalArgumentException("Wrong minimumAngle");
        if (maximumAngle < 0) throw new IllegalArgumentException("Wrong maximumAngle");
        if (minimumPulseWidth >= maximumPulseWidth) throw new IllegalArgumentException("Wrong minimumPulseWidth");
        if (minimumAngle >= maximumAngle) throw new IllegalArgumentException("Wrong minimumAngle");

        if (this.pwm.getFrequency() != 50)
            this.pwm.setFrequency(50);

        this.minAngle = minimumAngle;
        this.maxAngle = maximumAngle;

        double period = 1000000.0 / this.pwm.getFrequency();

        minimumPulseWidth = (int)(minimumPulseWidth / period * 4096.0);
        maximumPulseWidth = (int)(maximumPulseWidth / period * 4096.0);

        this.scale = ((maximumPulseWidth - minimumPulseWidth) / (maximumAngle - minimumAngle));
        this.offset = minimumPulseWidth;

        this.limitsSet = true;
    }
}