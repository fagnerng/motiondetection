package com.istoc.motiondetection

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.istoc.motiondetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private var mSensorManager: SensorManager? = null
    private var mOrientation: Sensor? = null
    private var mOrientationUncalibrated: Sensor? = null
    private var mMagnetometer: Sensor? = null
    private var mMagnetometerUncalibrated: Sensor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Register sensor listener
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mOrientation = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mOrientationUncalibrated =
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)
        mMagnetometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mMagnetometerUncalibrated = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
    }
    var sensorReady = false
    var mGravity = FloatArray(3)
    var mGeomagnetic = FloatArray(3)
    @SuppressLint("SetTextI18n")
    override fun onSensorChanged( event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> {
                for (i in 0..2) {
                    mGravity[i] = event.values[i]
                }
                if (mGeomagnetic[0] != 0f) sensorReady = true


                binding.g1.text = "g1: ${mGravity[0].toInt()} "
                binding.g2.text = "g2: ${mGravity[1].toInt()} "
                binding.g3.text = "g3: ${mGravity[2].toInt()} "
            }
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> {
                for (i in 0..2) {
                    mGeomagnetic[i] = event.values[i]
                }
                if (mGravity[2] != 0f) sensorReady = true
                binding.m1.text = "m1: ${mGeomagnetic[0].toInt()} "
                binding.m2.text = "m2: ${mGeomagnetic[1].toInt()} "
                binding.m3.text = "m3: ${mGeomagnetic [2].toInt()} "
            }
        }
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER)
        {
            binding.gc.text = "gc: true"
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD)
        {
            binding.mc.text = "mc: true"
        }
        if (!sensorReady) return

        binding.ready.text = "ready: true"
        val R = FloatArray(9)
        val I = FloatArray(9)

        val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            // Roll angle needs to be in degrees
            val convertFactor = 180 / Math.PI
            var converted = orientation[2] * convertFactor.toFloat()
            val landscape = converted.toInt()
            converted = orientation[1] * convertFactor.toFloat()
            val portrait = -converted.toInt()

            binding.angle.text = "angle portrait ${portrait}º landscape ${landscape}º"
            binding.seekBar.progress = landscape
            binding.seekBar2.progress = portrait

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged $sensor accuracy $accuracy")
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_UI)
        mSensorManager!!.registerListener(
            this,
            mOrientationUncalibrated,
            SensorManager.SENSOR_DELAY_UI
        )
        mSensorManager!!.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI)
        mSensorManager!!.registerListener(this, mMagnetometerUncalibrated, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }
}