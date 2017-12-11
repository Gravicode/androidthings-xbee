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

package com.google.android.things.contrib.driver.ads7830;

import android.support.annotation.VisibleForTesting;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class ads7830 implements AutoCloseable {

    private static final String TAG = ads7830.class.getSimpleName();

    /**
     * I2C slave address.
     */
    public static final int I2C_ADDRESS = GetAddress(false, false);

    //Start Driver

    private I2cDevice device;
    private boolean disposed;
    private byte[] read;
    private byte[] write;

    public static int GetAddress(boolean a0, boolean a1) {
        return (int)(0x48 | (a0 ? 1 : 0) | (a1 ? 2 : 0));
    }

    public void Dispose()throws IOException {
        this.close();
    }

    public ads7830(String I2CBus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice _device = pioService.openI2cDevice(I2CBus, I2C_ADDRESS);

        this.device = _device;
        this.disposed = false;
        this.read = new byte[1];
        this.write = new byte[1];
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

    public int ReadRaw(int channel)throws IOException  {
        if (this.disposed) throw new IllegalAccessError("Sudah di dispose ADS7830");
        if (channel > 8 || channel < 0) throw new IllegalAccessError("Out of Range channel");
        //this.write[0]
        int addr = (0x84 | ((channel % 2 == 0 ? channel / 2 : (channel - 1) / 2 + 4) << 4));

        this.device.readRegBuffer(addr, this.read,this.read.length);

        return this.read[0];
    }

    public double Read(int channel) throws IOException{
       return this.ReadRaw(channel) / 255.0;
    }
}
