package com.gravicode.mifmasterz.pca9535;

import android.support.annotation.VisibleForTesting;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import android.util.Log;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class pca9535 implements AutoCloseable {

    private static final String TAG = pca9535.class.getSimpleName();
    /*
        public class PinChangedEventArgs : EventArgs {
            public int Pin { get; }
            public Boolean NewState { get; }

            public PinChangedEventArgs(int pin, Boolean newState) {
                this.Pin = pin;
                this.NewState = newState;
            }
        }
    */
        private I2cDevice device;
        private Gpio interrupt;
        private byte[] write2;
        private byte[] write1;
        private byte[] read1;
        private Boolean disposed;
        private byte in0;
        private byte in1;
        private byte out0;
        private byte out1;
        private byte config0;
        private byte config1;

        enum Register {
            InputPort0 (0x00),
            InputPort1 (0x01),
            OutputPort0 (0x02),
            OutputPort1 (0x03),
            ConfigurationPort0 (0x06),
            ConfigurationPort1 (0x07);

            private final int id;

            Register(int id) {
                this.id =  id;
            }

            public int getId() {
                return this.id;
            }
        }
        public static final int I2C_ADDRESS = GetAddress(true,true ,false);
        public static byte GetAddress(Boolean a0, Boolean a1, Boolean a2) {
            return (byte)(0x20 | (a0 ? 1 : 0) | (a1 ? 2 : 0) | (a2 ? 4 : 0));
        }

        //public event TypedEventHandler<pca9535, PinChangedEventArgs> PinChanged;



        public pca9535(String I2CBus)  throws IOException { this(I2CBus, null);}

        public pca9535(String I2CBus, String GPIO_NAME) throws IOException{
            PeripheralManagerService pioService = new PeripheralManagerService();
            I2cDevice _device = pioService.openI2cDevice(I2CBus, I2C_ADDRESS);

            this.device = _device;
            this.write2 = new byte[2];
            this.write1 = new byte[1];
            this.read1 = new byte[1];
            this.disposed = false;

            this.out0 = (byte)0xFF;
            this.out1 = (byte)0xFF;
            this.config0 = (byte)0xFF;
            this.config1 = (byte)0xFF;

            this.device = device;
            try {
                this.interrupt = pioService.openGpio(GPIO_NAME);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access GPIO", e);
            }

            if (interrupt != null) {
                this.interrupt = interrupt;
                this.interrupt.setDirection(Gpio.DIRECTION_IN);
                this.interrupt.registerGpioCallback(OnInterruptValueChanged);
            }
        }
        private GpioCallback OnInterruptValueChanged = new GpioCallback() {
            @Override
            public boolean onGpioEdge(Gpio gpio) {
                Log.i(TAG, "GPIO changed");
                try {
                    byte in0 = ReadRegister(Register.InputPort0);
                    byte in1 = ReadRegister(Register.InputPort1);

                    for (int i = 0; i < 8; i++)
                        if ((in0 & (1 << i)) != (in0 & (1 << i)))
                            OnPinValueChanged(i, (in0 & (1 << i)) != 0);

                    for (int i = 0; i < 8; i++)
                        if ((in1 & (1 << i)) != (in1 & (1 << i)))
                            OnPinValueChanged(i + 10, (in1 & (1 << i)) != 0);

                    in0 = in0;
                    in1 = in1;

                    // Step 5. Return true to keep callback active.
                    return true;
                }catch (IOException ex){
                    Log.w(TAG, "Unable to access GPIO", ex);
                    return false;
                }
            }
        };


    @Override
    public void close() throws IOException {
        if (!this.disposed) {

            this.device.close();
            this.interrupt.close();

            this.disposed = true;
        }
    }

        private void OnPinValueChanged(int pin, Boolean newState) {
            //call event handler
            //PinChanged.Invoke(this, new PinChangedEventArgs(pin, newState));
        }

        public Boolean Read(int pin) throws IOException {
            if (this.disposed) throw new IllegalStateException("Device sudah di dispose.");
            if (!((pin >= 0 && pin <= 7) || (pin >= 10 && pin <= 17))) throw new IllegalStateException("PIN:"+pin);

            if (pin < 8) {
                return (this.ReadRegister(Register.InputPort0) & (1 << pin)) != 0;
            }
            else {
                return (this.ReadRegister(Register.InputPort1) & (1 << (pin - 10))) != 0;
            }
        }

        public void Write(int pin, Boolean state) throws IOException {
            if (this.disposed) throw new IllegalStateException("Device sudah di dispose.");
            if (!((pin >= 0 && pin <= 7) || (pin >= 10 && pin <= 17))) throw new IllegalStateException("Out of range :"+pin);

            if (state) {
                if (pin < 8) {
                    this.out0 |= (byte)(1 << pin);

                    this.WriteRegister(Register.OutputPort0, out0);
                }
                else {
                    this.out1 |= (byte)(1 << (pin - 10));

                    this.WriteRegister(Register.OutputPort1, out1);
                }
            }
            else {
                if (pin < 8) {
                    this.out0 &= (byte)~(1 << pin);

                    this.WriteRegister(Register.OutputPort0, this.out0);
                }
                else {
                    this.out1 &= (byte)~(1 << (pin - 10));

                    this.WriteRegister(Register.OutputPort1, this.out1);
                }
            }
        }

        public void SetDriveMode(int pin, int driveMode) throws  IOException {
            if (this.disposed) throw new IllegalAccessError("object sudah di dispose");
            if (!((pin >= 0 && pin <= 7) || (pin >= 10 && pin <= 17))) throw new IllegalAccessError("pin out of range : "+pin);

            if (driveMode == Gpio.DIRECTION_IN) {
                if (pin < 8) {
                    this.config0 |= (byte)(1 << pin);

                    this.WriteRegister(Register.ConfigurationPort0, this.config0);
                }
                else {
                    this.config1 |= (byte)(1 << (pin - 10));

                    this.WriteRegister(Register.ConfigurationPort1, this.config1);
                }
            }
            else {
                if (pin < 8) {
                    this.config0 &= (byte)~(1 << pin);

                    this.WriteRegister(Register.ConfigurationPort0, this.config0);
                }
                else {
                    this.config1 &= (byte)~(1 << (pin - 10));

                    this.WriteRegister(Register.ConfigurationPort1, this.config1);
                }
            }
        }

        private void WriteRegister(Register register, byte value) throws  IOException {
            //this.write2[0] = (byte)register;
            //this.write2[1] = (byte)value;

            //this.device.Write(this.write2);

            this.device.writeRegByte(register.getId(),value);
        }

        private byte ReadRegister(Register register) throws IOException {
            //this.write1[0] = (byte)register;

            //this.device.wr(this.write1, this.read1);

            //return this.read1[0];

            this.device.readRegBuffer(register.getId(), this.read1, this.read1.length);

            return this.read1[0];

        }

}
