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

package com.google.android.things.contrib.driver.pca9685;

import android.support.annotation.VisibleForTesting;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import android.util.Log;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class pca9685 implements AutoCloseable {

    private static final String TAG = pca9685.class.getSimpleName();

    /* Start Driver */
    private I2cDevice device;
    private Gpio outputEnable;
    private byte[] write5;
    private byte[] write2;
    private byte[] write1;
    private byte[] read1;
    private boolean disposed;

    public static int I2C_ADDRESS = GetAddress(true, true, true, true, true, true);

    enum Register {
        Mode1 (0x00),
        Mode2 (0x01),
        Led0OnLow (0x06),
        Prescale (0xFE);

        private final int id;

        Register(int id) {
            this.id =  id;
        }

        public int getId() {
            return this.id;
        }
    }

    public static int GetAddress (boolean a0, boolean a1, boolean a2, boolean a3, boolean a4, boolean a5) {
     return (int) (0x40 | (a0 ? 1 : 0) | (a1 ? 2 : 0) | (a2 ? 4 : 0) | (a3 ? 8 : 0) | (a4 ? 16 : 0) | (a5 ? 32 : 0));
    }

    public pca9685(String I2CBus) throws IOException{
        //PeripheralManagerService pioService = new PeripheralManagerService();
        //I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS);
        this(I2CBus, null);
    }

    public pca9685(String I2CBus, String GPIO_NAME) throws IOException{

        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice _device = pioService.openI2cDevice(I2CBus, I2C_ADDRESS);

        this.write5 = new byte[5];
        this.write2 = new byte[2];
        this.write1 = new byte[1];
        this.read1 = new byte[1];
        this.disposed = false;

        this.device = _device;
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            this.outputEnable = manager.openGpio(GPIO_NAME);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }


        if (this.outputEnable != null) {
            // Initialize the pin as a high output
            this.outputEnable.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            // Low voltage is considered active
            this.outputEnable.setActiveType(Gpio.ACTIVE_LOW);
            // Toggle the value to be LOW
            this.outputEnable.setValue(false);
            //this.outputEnable.SetDriveMode(GpioPinDriveMode.Output);
            //this.outputEnable.Write(GpioPinValue.Low);
        }

        this.WriteRegister(Register.Mode1, (byte)0x20);
        this.WriteRegister(Register.Mode2, (byte)0x06);
    }

    /**
     * Close the device and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (device != null) {
            try {
                if (!this.disposed) {
                    device.close();

                    //this.device.Dispose();
                    //this.outputEnable?.Dispose();
                }

            } finally {
                device = null;

                this.disposed = true;
            }
        }

    }

    private int _Frequency;
    public int getFrequency ()throws IOException{

        if (this.disposed) throw new IllegalStateException("Device sudah di dispose.");

        int _Val = (int) (25000000 / (4096 * (this.ReadRegister(Register.Prescale) + 1)) / 0.9);
        return _Val;
    }
    public void setFrequency (int Freq)throws IOException {
        if (this.disposed) throw new IllegalStateException("Device PCA9685 sudah di dispose.");
        if (Freq < 40 || Freq > 1500)
            throw new IllegalArgumentException("Valid range is 40 to 1500.");

        Freq *= 10;
        Freq /= 9;

        byte mode = this.ReadRegister(Register.Mode1);

        this.WriteRegister(Register.Mode1, (byte) (mode | 0x10));

        this.WriteRegister(Register.Prescale, (byte) (25000000 / (4096 * Freq) - 1));

        this.WriteRegister(Register.Mode1, mode);

        this.WriteRegister(Register.Mode1, (byte) (mode | 0x80));
    }

    private boolean _OutputEnabled;
    public boolean getOutputEnabled()throws IOException{

            return this.outputEnable.getValue(); //== false;
        }
    public void setOutputEnabled (boolean ValOutput) throws IOException{
        if (this.disposed) throw new IllegalStateException("Sudah di dispose PCA9685");
        if (this.outputEnable == null) throw new IllegalAccessError("Pin Output belum di set");

        this.outputEnable.setValue(ValOutput); //? false : true);

    }

    public void TurnOn(int channel) throws IOException{
        this.SetChannel(channel, (short) 0x1000, (short) 0x0000);
    }

    public void TurnOff(int channel) throws IOException{
        this.SetChannel(channel, (short) 0x0000, (short) 0x1000);
    }

    public void TurnAllOn() throws IOException {
        for (int i = 0; i < 16; i++)
            this.TurnOn(i);
    }

    public void TurnAllOff() throws IOException{
        for (int i = 0; i < 16; i++)
            this.TurnOff(i);
    }

    public void SetDutyCycle(int channel, double dutyCycle) throws IOException{
        if (dutyCycle < 0.0 || dutyCycle > 1.0) throw new IllegalStateException("Out of Range dutyCycle");

        if (dutyCycle == 1.0) {
            this.TurnOn(channel);
        }
        else if (dutyCycle == 0.0) {
            this.TurnOff(channel);
        }
        else {
            this.SetChannel(channel, (short) 0x0000, (short) (4096 * dutyCycle));
        }
    }

    public void SetChannel(int channel, short on, short off)throws IOException {
        if (this.disposed) throw new IllegalStateException("Sudah di dispose PCA9685");
        if (channel < 0 || channel > 15) throw new IllegalStateException("Out of range channel");
        if (on > 4096) throw new IllegalStateException("Out of Range on");
        if (off > 4096) throw new IllegalStateException("Out of Range off");

        this.write5[0] = (byte)(Register.Led0OnLow.getId() + (byte)channel * 4);
        this.write5[1] = (byte)on;
        this.write5[2] = (byte)(on >> 8);
        this.write5[3] = (byte)off;
        this.write5[4] = (byte)(off >> 8);

        this.device.write(this.write5,this.write5.length);
    }

    private void WriteRegister(Register register, byte value)throws IOException {
        //this.write2[0] = register.getId();
        //this.write2[1] = value;

        this.device.writeRegByte(register.getId(),value);
    }

    private byte ReadRegister(Register register) throws IOException{
        //this.write1[0] = register.getId();

        this.device.readRegBuffer(register.getId(), this.read1, this.read1.length);

        return this.read1[0];
    }
}
