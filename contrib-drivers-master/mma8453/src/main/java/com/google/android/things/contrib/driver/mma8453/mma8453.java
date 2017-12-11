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

package com.google.android.things.contrib.driver.mma8453;

import android.support.annotation.VisibleForTesting;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class mma8453 implements AutoCloseable {

    private static final String TAG = mma8453.class.getSimpleName();

    /**
     * I2C slave address.
     */
    public static final int I2C_ADDRESS = GetAddress(false);


    //start driver
    private I2cDevice device;
    private byte[] write;
    private byte[] read;
    private boolean disposed;
    private double x=0.0;
    private double y=0.0;
    private double z=0.0;

    public static int GetAddress(boolean a0) {

        return (int) (0x1C | (a0 ? 1 : 0));
    }

    public void Dispose() throws IOException{
        this.close();
    }

    public mma8453(String I2CBus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice _device = pioService.openI2cDevice(I2CBus, I2C_ADDRESS);

        this.device = _device;
        this.write = new byte[1];
        this.write[0] =  0x01;
        this.read = new byte[6];
        this.disposed = false;
        byte[] initdata = new byte[] { 0x2A, 0x01 };
        this.device.write(initdata,initdata.length);
    }

    /**
     * Close the device and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (this.device != null) {
            try {
                if (!this.disposed) {

                    this.device.close();


                    this.disposed = true;
                }
            } finally {
                this.device = null;
            }
        }
    }


    public void GetAcceleration()throws IOException {
        if (this.disposed) throw new IllegalAccessError("Sudah di dispose MMA8453");

        this.device.readRegBuffer(this.write[0], this.read,this.read.length);

        x = this.Normalize(0);
        y = this.Normalize(2);
        z = this.Normalize(4);
    }

    private double Normalize(int offset) {
        double value = (this.read[offset] << 2) | (this.read[offset + 1] >> 6);

        if (value > 511.0)
            value = value - 1024.0;

        value /= 256.0;

        return value;
    }

    public double getX(){return x;}
    public double getY(){return y;}
    public double getZ(){return z;}

}
