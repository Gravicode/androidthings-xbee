package com.google.android.things.contrib.driver.fezhat;

/**
 * Created by mifmasterz on 12/29/16.
 */

        import android.support.annotation.StringDef;

        import com.google.android.things.contrib.driver.pca9685.pca9685;

        import com.google.android.things.pio.Gpio;
        import com.google.android.things.pio.PeripheralManagerService;

        import java.io.IOException;

public class Motor {
    private double speed;
    private boolean disposed;
    private pca9685 pwm;
    private Gpio direction1;
    private Gpio direction2;
    private int pwmChannel;

    /// <summary>
/// The speed of the motor. The sign controls the direction while the magnitude controls the speed (0 is off, 1 is full speed).
/// </summary>
    public double getSpeed()

    {
        return this.speed;
    }

    public void setSpeed(double SpeedVal) throws IOException {

        this.pwm.SetDutyCycle(this.pwmChannel, 0);

        this.direction1.setValue(SpeedVal > 0 ? true : false);
        this.direction2.setValue(SpeedVal > 0 ? false : true);

        this.pwm.SetDutyCycle(this.pwmChannel, Math.abs(SpeedVal));

        this.speed = SpeedVal;

    }

    /// <summary>
/// Disposes of the object releasing control the pins.
/// </summary>
    public void Dispose() {

        Dispose(true);
    }

    public Motor(pca9685 pwm, int pwmChannel, String direction1Pin, String direction2Pin) throws IOException {
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            this.direction1 = manager.openGpio(direction1Pin);
            this.direction2 = manager.openGpio(direction2Pin);

        } catch (IOException e) {
            //Log.w(TAG, "Unable to access GPIO", e);
        }

        this.speed = 0.0;
        this.pwm = pwm;
        this.disposed = false;

        this.pwmChannel = pwmChannel;
        // Initialize the pin as a high output
        this.direction1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        this.direction1.setActiveType(Gpio.ACTIVE_HIGH);

        // Initialize the pin as a high output
        this.direction2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        this.direction2.setActiveType(Gpio.ACTIVE_HIGH);
    }

    /// <summary>
/// Stops the motor.
/// </summary>
    public void Stop() throws IOException {
        this.pwm.SetDutyCycle(this.pwmChannel, 0.0);
    }

/// <summary>
/// Disposes of the object releasing control the pins.
/// </summary>
/// <param name="disposing">Whether or not this method is called from Dispose().</param>

    void Dispose(boolean disposing) {
        if (!this.disposed) {
            if (disposing) {
                if (this.direction1 != null) {
                    try {
                        this.direction1.close();
                        this.direction2.close();
                        this.direction1 = null;
                        this.direction2 = null;
                    } catch (IOException e) {
                        //Log.w(TAG, "Unable to close GPIO", e);
                    }
                }
            }

            this.disposed = true;
        }
    }
}