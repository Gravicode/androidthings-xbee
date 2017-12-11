package com.example.androidthings.myproject
import com.google.gson.annotations.SerializedName
/**
 * Created by mifmasterz on 12/9/17.
 */
data class SensorXbee(
        @SerializedName("temp") val temp: Double,
        @SerializedName("humid") val humid: Double,
        @SerializedName("light") val light: Double
)