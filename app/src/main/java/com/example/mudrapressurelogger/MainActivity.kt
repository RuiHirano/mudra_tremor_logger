package com.example.mudrapressurelogger

import MudraAndroidSDK.Mudra
import MudraAndroidSDK.Mudra.*
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var mMudra: Mudra? = null
    private var mAirMousePosX = 0f
    private var mAirMousePosY = 0f
    private var mScreenWidth = 0
    private  var mScreenHeight = 0
    private  var focusKey = mutableListOf(0, 0) // focus
    private var labelText: TextView? = null
    private var inputText: TextView? = null
    private var resetButton: Button? = null
    private var mPressureVisualizer: WaveVisualizer? = null
    private var mQuaternionVisualizer: WaveVisualizer? = null
    private var mAirMouseVisualizer: WaveVisualizer? = null
    private var mPressureText: TextView? = null
    private var mQuaternionText: TextView? = null
    private var mAirMouseText: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init Mudra
        initAirmouse()
        requestPermissions()
        initializeMudraConnection()

        mPressureText = findViewById(R.id.pressureText) as TextView
        mQuaternionText = findViewById(R.id.quaternionText) as TextView
        mAirMouseText = findViewById(R.id.airMouseText) as TextView

        val pressureSurface = findViewById(R.id.pressure_visualizer) as SurfaceView
        mPressureVisualizer = WaveVisualizer(this, pressureSurface)
        val quaternionSurface = findViewById(R.id.quaternion_visualizer) as SurfaceView
        mQuaternionVisualizer = WaveVisualizer(this, quaternionSurface)
        val airMouseSurface = findViewById(R.id.air_mouse_visualizer) as SurfaceView
        mAirMouseVisualizer = WaveVisualizer(this, airMouseSurface)

    }

    private fun initAirmouse() {
        mScreenWidth = Resources.getSystem().displayMetrics.widthPixels
        mScreenHeight = Resources.getSystem().displayMetrics.heightPixels
        mAirMousePosX = mScreenWidth / 2.0f
        mAirMousePosY = mScreenHeight / 2.0f
    }
    private fun requestPermissions() {
        Mudra.requestAccessLocationPermissions(this)
        // Required permission for Mudra - note we do not access any of your files/locationj!
        // Location is required for bluetooth,
        // Storage is required for reading gesture calibration file saved during callibration
        requestPermissions(
            arrayOf(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "RECORD_AUDIO"
            ),
            1
        )
    }


    private fun initializeMudraConnection() {
        mMudra = Mudra.autoConnectPaired(this)
        Log.d("MainActivity", "connect time")
        if (mMudra != null) {
            mMudra?.setOnFingertipPressureReady(onFingertipPressureReady)
            mMudra?.setOnImuQuaternionReady(onImuQuaternionReady)
            mMudra?.setOnDeviceStatusChanged(onDeviceStatusChanged)
            mMudra?.setOnAirMousePositionChanged(OnAirMousePositionChanged)
            mMudra?.setOnBatteryLevelChanged(onBatteryLevelChanged)
        }
    }

    //Mudra callbacks
    var onImuQuaternionReady =
        OnImuQuaternionReady { l, floats ->
            runOnUiThread {
                val aa = quaternion2AxisAngle(floats)
                if(aa != null) {
                    val distance =
                        Math.sqrt((aa[0] * aa[0]).toDouble() + (aa[1] * aa[1]).toDouble() + (aa[2] * aa[2]).toDouble() + (aa[3] * aa[3]).toDouble()).toFloat()
                    Log.d("MainActivity", "Quaternion: ${floats} ${aa[0]} ${aa[1]} ${distance} ")
                    mQuaternionText?.setText("Quaternion: "  + (distance).toString())
                    mQuaternionVisualizer?.add(distance/2)
                }
            }
        }

    private fun quaternion2AxisAngle(q: FloatArray): FloatArray? {
        // 四方位を計算する関数
        val aa = FloatArray(4)
        val angle_rad = Math.acos(q[0].toDouble()).toFloat() * 2
        aa[0] = angle_rad * 180.0f / Math.PI.toFloat()
        aa[1] = q[1] / Math.sin(angle_rad / 2.toDouble()).toFloat()
        aa[2] = q[2] / Math.sin(angle_rad / 2.toDouble()).toFloat()
        aa[3] = q[3] / Math.sin(angle_rad / 2.toDouble()).toFloat()
        Log.d(
            "quaternion to axis",
            java.lang.Float.toString(aa[0]) + " " + java.lang.Float.toString(aa[1]) + " " + java.lang.Float.toString(
                aa[2]
            ) + " " + java.lang.Float.toString(aa[3])
        )
        return aa
    }

    var onFingertipPressureReady =
        OnFingertipPressureReady { v: Float ->
            runOnUiThread {
                Log.d("MainActivity", "Pressure: ${(v * 1000).toInt()} " + v)
                mPressureText?.setText("Pressure: "  + (v * 1000).toString())
                mPressureVisualizer?.add(v*100)
            }
        }

    var OnAirMousePositionChanged =
        OnAirMousePositionChanged { floats ->
            /*val HSPEED = 0.7f
            val VSPEED = 1.0f
            mAirMousePosX += floats[0] * mScreenWidth * HSPEED
            mAirMousePosY += floats[1] * mScreenHeight * VSPEED
            mAirMousePosX = if (mAirMousePosX <= 0) 0.toFloat() else Math.min(
                mAirMousePosX,
                mScreenWidth.toFloat()
            )
            mAirMousePosY = if (mAirMousePosY <= 0) 0.toFloat() else Math.min(
                mAirMousePosY,
                mScreenHeight.toFloat()
            )
            //Log.d("MainActivity", "AirMouse: X: ${mAirMousePosX} Y: ${mAirMousePosY} ")*/
            var x = floats[0]
            var y = floats[1]
            val distance = Math.sqrt((x * x).toDouble() + (y * y).toDouble()).toFloat()
            mAirMouseText?.setText("AirMouse: Dist: ${distance*5000} ")
            mAirMouseVisualizer?.add(distance*5000)
        }


    var onDeviceStatusChanged =
        OnDeviceStatusChanged { b ->
            if (b) {
                runOnUiThread(Thread(Runnable {
                    Log.d("MainActivity", "BLE Address: ${mMudra!!.bluetoothDevice.address} %")
                }))
            }
        }

    var onBatteryLevelChanged = OnBatteryLevelChanged {
        runOnUiThread(Thread(Runnable {
            Log.d("MainActivity", "バッテリー値: ${mMudra?.getBatteryLevel()} %")
        }))
    }

}
