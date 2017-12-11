/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.androidthings.myproject

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.view.View

import java.text.SimpleDateFormat
import java.util.Calendar
import java.io.IOException
import java.net.URISyntaxException
import java.util.Scanner

//fezhat board
import com.google.android.things.contrib.driver.fezhat.FezHat
import com.google.android.things.contrib.driver.fezhat.Akselerasi
import com.google.android.things.contrib.driver.fezhat.Color
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback

//azure lib
import com.microsoft.azure.iothub.DeviceClient
import com.microsoft.azure.iothub.IotHubClientProtocol
import com.microsoft.azure.iothub.IotHubEventCallback
import com.microsoft.azure.iothub.IotHubMessageResult
import com.microsoft.azure.iothub.IotHubStatusCode
import com.microsoft.azure.iothub.Message
import com.microsoft.azure.iothub.MessageCallback

//mqtt

import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.DecimalFormat

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSyntaxException


/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>`PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
`</pre> *
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 */
class MainActivity : Activity() {
    internal var connString = "HostName=FreeDeviceHub.azure-devices.net;DeviceId=AndroidThingsDevice;SharedAccessKey=U5mH0B3POECuZlTCmMA9xEIql1+S+ASQBWOPG1d3iBY="

    private var client: DeviceClient? = null
    private val mHandler = Handler()

    private var hat: FezHat? = null
    private var next: Boolean = false
    private var i: Int = 0

    protected var TxtStatus: TextView? = null
    protected var TxtTemp: TextView? = null
    protected var TxtLight: TextView? = null
    protected var BtnReceive: Button? = null
    protected var TxtAccel: TextView? = null

    // UART Device Name
    private var UART_DEVICE_NAME : String = "USB1-1.4:1.0"//"USB1-1.5:1.0"
    private var mDevice : UartDevice? = null
    var datastr : String = ""

    val publishTopic = "mifmasterz/gdgbogor/data"
    private var mqttAndroidClient : MqttClient? = null;

    protected var mBlinkRunnable: Runnable = object : Runnable {

        override fun run() {
            try {
                mHandler.postDelayed(this, INTERVAL_BETWEEN_BLINKS_MS.toLong())
                val formatter = DecimalFormat("######.000")
                val data = SensorData()
                val accel = hat!!.GetAcceleration()
                data.Light = hat!!.GetLightLevel()
                data.Temp = hat!!.GetTemperature()
                data.Accelleration = "X:" + accel.X.toString() + " Y: " + accel.Y.toString() + " Z:" + accel.Z.toString()
                val lightStr = data.Light.toString()
                val Temp = formatter.format(data.Temp)
                val AccelStr = data.Accelleration
                val Btn18Str = hat!!.IsDIO18Pressed().toString()
                val Btn22Str = hat!!.IsDIO22Pressed().toString()
                val AnalogStr = hat!!.ReadAnalog(FezHat.AnalogPin.Ain1).toString()
                TxtLight?.text = "Light: " + lightStr
                TxtAccel?.text = "Acceleration: " + AccelStr
                TxtTemp?.text = "Temp: $Temp C"
                Log.e(TAG, "Light:" + lightStr)
                Log.e(TAG, "Temp:" + Temp)
                Log.e(TAG, "Acceleration:" + AccelStr)
                Log.e(TAG, "Counter:" + i)
                Log.e(TAG, "Next:" + next)



                if (i++ % 5 == 0) {
                    val LedsTextBox = next.toString()

                    hat!!.diO24On = next
                    hat!!.D2.color = if (next) Color.Green() else Color.Black()
                    hat!!.D3.color = if (next) Color.Green() else Color.Black()

                    hat!!.WriteDigital(FezHat.DigitalPin.DIO16, next)
                    hat!!.WriteDigital(FezHat.DigitalPin.DIO26, next)

                    hat!!.SetPwmDutyCycle(FezHat.PwmPin.Pwm5, if (next) 1.0 else 0.0)
                    hat!!.SetPwmDutyCycle(FezHat.PwmPin.Pwm6, if (next) 1.0 else 0.0)
                    hat!!.SetPwmDutyCycle(FezHat.PwmPin.Pwm7, if (next) 1.0 else 0.0)
                    hat!!.SetPwmDutyCycle(FezHat.PwmPin.Pwm11, if (next) 1.0 else 0.0)
                    hat!!.SetPwmDutyCycle(FezHat.PwmPin.Pwm12, if (next) 1.0 else 0.0)

                    next = !next
                }

                if (hat!!.IsDIO18Pressed()) {
                    hat!!.S1.position = hat!!.S1.position + 5.0
                    hat!!.S2.position = hat!!.S2.position + 5.0

                    if (hat!!.S1.position >= 180.0) {
                        hat!!.S1.position = 0.0
                        hat!!.S2.position = 0.0
                    }
                }

                if (hat!!.IsDIO22Pressed()) {
                    if (hat!!.MotorA.speed == 0.0) {
                        hat!!.MotorA.speed = 0.7
                        hat!!.MotorB.speed = -0.7
                    }
                } else {
                    if (hat!!.MotorA.speed != 0.0) {
                        hat!!.MotorA.speed = 0.0
                        hat!!.MotorB.speed = 0.0
                    }
                }
                Log.d(TAG, "jalan lagi...")
                //sending data to azure

                try {
                    SendMessage(data)
                    Log.d(TAG, "data has been pushed to azure...")
                    val timeStamp = SimpleDateFormat("dd MM yy HH:mm:ss").format(Calendar.getInstance().time)
                    TxtStatus?.text = "Data has been pushed to azure at " + timeStamp
                } catch (ex: URISyntaxException) {

                } finally {

                }
            } catch (e: IOException) {
                Log.e(TAG, "Error on Jalan", e)

            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        try {
            val service = PeripheralManagerService()
            Log.d(TAG, "Available GPIO: " + service.gpioList)
            Log.d(TAG, "onCreate")
            try {
                Setup()
            } catch (ex: URISyntaxException) {

            }

            mHandler.post(mBlinkRunnable)
        } catch (e: IOException) {
            Log.d(TAG, e.message)
        }

    }

    @Throws(URISyntaxException::class, IOException::class)
    protected fun Setup() {
        TxtAccel = findViewById(R.id.txtAccel) as TextView
        TxtLight = findViewById(R.id.txtLight) as TextView
        TxtTemp = findViewById(R.id.txtTemp) as TextView
        TxtStatus = findViewById(R.id.txtStatus) as TextView
        BtnReceive = findViewById(R.id.btnReceive) as Button
        BtnReceive?.setOnClickListener {
            try {
                ReceiveData()
            } catch (io: IOException) {

            } catch (es: URISyntaxException) {

            }
        }
        this.hat = FezHat.Create()
        this.hat!!.S1.SetLimits(500, 2400, 0.0, 180.0)
        this.hat!!.S2.SetLimits(500, 2400, 0.0, 180.0)
        // Comment/uncomment from lines below to use HTTPS or MQTT protocol
        // IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        val protocol = IotHubClientProtocol.MQTT

        client = DeviceClient(connString, protocol)

        try {
            client!!.open()
        } catch (e1: IOException) {
            println("Exception while opening IoTHub connection: " + e1.toString())
        } catch (e2: Exception) {
            println("Exception while opening IoTHub connection: " + e2.toString())
        }

        //USB1-1.5:1.0
        try {
            val manager = PeripheralManagerService()


            var deviceList : List<String> = manager.getUartDeviceList()
            if (deviceList.isEmpty()) {
                Log.i(TAG, "No UART port available on this device.");
            } else {
                Log.i(TAG, "List of available devices: " + deviceList);
            }


            mDevice = manager.openUartDevice(UART_DEVICE_NAME)
            setFlowControlEnabled(mDevice!!,false)
            configureUartFrame(mDevice!!)
            mDevice?.registerUartDeviceCallback(mUartCallback)
        } catch (e: IOException) {
            Log.w(TAG, "Unable to access UART device", e)
        }
        try {
            mqttAndroidClient = MqttClient("tcp://cloud.makestro.com:1883", "androidthing-device",MemoryPersistence())
            mqttAndroidClient?.setCallback(object : MqttCallback {

                override fun connectionLost(cause: Throwable) {
                    Log.w(TAG, "The Connection was lost.")
                }

                @Throws(Exception::class)
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.w(TAG,"Incoming message: " + String(message.payload))
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {

                }
            })
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.userName = "mifmasterz"
            mqttConnectOptions.password = "123qweasd".toCharArray()

            mqttAndroidClient?.connect(mqttConnectOptions);

            mqttAndroidClient?.subscribe(publishTopic)


        } catch ( e:MqttException) {
            e.printStackTrace();
        }

    }

    fun publishMessage(msg:String) {

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            mqttAndroidClient?.publish(publishTopic, message)
            Log.w(TAG, "Message Published")
            /*
            if (mqttAndroidClient?.isConnected()==false) {
                addToHistory(mqttAndroidClient?.getBufferedMessageCount().toString() + " messages in buffer.")
            }*/
        } catch (e: MqttException) {
            System.err.println("Error Publishing: " + e.message)
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun writeUartData(uart:UartDevice,messagestr:String) :Unit {
        val newline = System.getProperty("line.separator")
        var message=messagestr+newline
        var buffer:ByteArray = message.toByteArray()
        var count = uart.write(buffer, buffer.size);
        Log.d(TAG, "Wrote " + count + " bytes to peripheral");
    }

    private val mUartCallback = object : UartDeviceCallback() {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Read available data from the UART device
            try {
                readUartBuffer(uart)
            } catch (e: IOException) {
                Log.w(TAG, "Unable to access UART device", e)
            }

            // Continue listening for more interrupts
            return true
        }

        override fun onUartDeviceError(uart: UartDevice, error: Int) {
            Log.w(TAG, uart.toString() + ": Error event " + error)
        }
    }

    @Throws(IOException::class)
    fun setFlowControlEnabled(uart: UartDevice, enable: Boolean) {
        if (enable) {
            // Enable hardware flow control
            uart.setHardwareFlowControl(UartDevice.HW_FLOW_CONTROL_AUTO_RTSCTS)
        } else {
            // Disable flow control
            uart.setHardwareFlowControl(UartDevice.HW_FLOW_CONTROL_NONE)
        }
    }

    @Throws(IOException::class)
    fun readUartBuffer(uart:UartDevice ) {

        val newline = System.getProperty("line.separator")

        // Maximum amount of data to read at one time
        var maxCount : Int = 1000
        var buffer : ByteArray = ByteArray(maxCount)

        var count:Int
        count = uart.read(buffer, buffer.size)
        while (count  > 0) {
            Log.d(TAG, "Read " + count + " bytes from peripheral");
            var reconstitutedString = String(buffer, 0, count);
            reconstitutedString = reconstitutedString
            val hasNewline = reconstitutedString.contains(newline)
            datastr += reconstitutedString
            if(hasNewline) {
                if(datastr.length > 0) {
                    Log.d(TAG, "DATA REC: " + datastr)
                    publishMessage(datastr)
                    try {
                        var gson: Gson = Gson()
                        val data = gson.fromJson(datastr, SensorXbee::class.java)
                        if (data.light < 600) {
                            writeUartData(mDevice!!, "{\"alarm\":\"true\"}");
                        } else {
                            writeUartData(mDevice!!, "{\"alarm\":\"false\"}");
                        }
                    } catch (ex: JsonSyntaxException) {
                        Log.d(TAG,ex.message)
                    }
                }
                datastr = ""
            }
            count = uart.read(buffer, buffer.size)
        }

    }

    @Throws(IOException::class)
    fun configureUartFrame(uart: UartDevice) {
        // Configure the UART port
        uart.setBaudrate(9600)
        uart.setDataSize(8)
        uart.setParity(UartDevice.PARITY_NONE)
        uart.setStopBits(1)
    }

    @Throws(URISyntaxException::class, IOException::class)
    fun ReceiveData() {


        // Comment/uncomment from lines below to use HTTPS or MQTT protocol
        // IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        val protocol = IotHubClientProtocol.MQTT

        val client = DeviceClient(connString, protocol)

        if (protocol == IotHubClientProtocol.MQTT) {
            val callback = MessageCallbackMqtt()
            val counter = Counter(0)
            client.setMessageCallback(callback, counter)
        } else {
            val callback = MessageCallback()
            val counter = Counter(0)
            client.setMessageCallback(callback, counter)
        }

        try {
            client.open()
        } catch (e1: IOException) {
            println("Exception while opening IoTHub connection: " + e1.toString())
        } catch (e2: Exception) {
            println("Exception while opening IoTHub connection: " + e2.toString())
        }

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        client.close()
    }

    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    protected class MessageCallbackMqtt : com.microsoft.azure.iothub.MessageCallback {
        override fun execute(msg: Message, context: Any): IotHubMessageResult {
            val counter = context as Counter
            println(
                    "Received message " + counter.toString()
                            + " with content: " + String(msg.bytes, Message.DEFAULT_IOTHUB_MESSAGE_CHARSET))

            counter.increment()

            return IotHubMessageResult.COMPLETE
        }
    }

    protected class MessageCallback : com.microsoft.azure.iothub.MessageCallback {
        override fun execute(msg: Message, context: Any): IotHubMessageResult {
            val counter = context as Counter
            println(
                    "Received message " + counter.toString()
                            + " with content: " + String(msg.bytes, Message.DEFAULT_IOTHUB_MESSAGE_CHARSET))

            val switchVal = counter.get() % 3
            val res: IotHubMessageResult
            when (switchVal) {
                0 -> res = IotHubMessageResult.COMPLETE
                1 -> res = IotHubMessageResult.ABANDON
                2 -> res = IotHubMessageResult.REJECT
                else ->
                    // should never happen.
                    throw IllegalStateException("Invalid message result specified.")
            }

            println("Responding to message " + counter.toString() + " with " + res.name)

            counter.increment()

            return res
        }
    }

    @Throws(URISyntaxException::class, IOException::class)
    fun SendMessage(data: SensorData) {
        val gson = Gson()
        val msgStr = gson.toJson(data)
        try {
            val msg = Message(msgStr)
            msg.setProperty("messageCount", Integer.toString(i))
            println(msgStr)
            val eventCallback = EventCallback()
            client!!.sendEventAsync(msg, eventCallback, i)
        } catch (e: Exception) {
        }

    }

    protected class EventCallback : IotHubEventCallback {
        override fun execute(status: IotHubStatusCode, context: Any) {
            val i = context as Int
            println("IoT Hub responded to message " + i.toString()
                    + " with status " + status.name)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mHandler.removeCallbacks(mBlinkRunnable)
        if (mDevice != null) {
            try {
                mDevice?.unregisterUartDeviceCallback(mUartCallback)
                mDevice?.close()
                mDevice = null
            } catch (e: IOException) {
                Log.w(TAG, "Unable to close UART device", e)
            }

        }
        try {
            client!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    protected class Counter(protected var num: Int) {

        fun get(): Int {
            return this.num
        }

        fun increment() {
            this.num++
        }

        override fun toString(): String {
            return Integer.toString(this.num)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()

        private val INTERVAL_BETWEEN_BLINKS_MS = 2000
    }
}
