package corpglory.android.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.jetbrains.anko.button
import org.jetbrains.anko.editText
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout


class MainActivity : AppCompatActivity(), AccelerometerEventListener {
    lateinit var sensorManager: SensorManager
    var accelerometerEventListener: AccelerometerHandler? = null
    var database: DatabaseConnection? = null

    lateinit var sensor: Sensor
    lateinit var outView: TextView
    lateinit var addrText: EditText
    lateinit var loginText: EditText
    lateinit var passwordText: EditText
    lateinit var dbNameText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            outView = textView("") {
                id = View.generateViewId()
                textSize = 26f
            }
            button("Start") {
                textSize = 26f
                onClick {
                    startListen()
                }
            }
            textView("InfluxDB settings") {
                textSize = 26f
            }
            addrText = editText("") {
                hint = "address"
                textSize = 24f
            }
            loginText = editText("") {
                hint = "login"
                textSize = 24f
            }
            passwordText = editText("") {
                hint = "password"
                textSize = 24f
            }
            dbNameText = editText("") {
                hint = "database"
                textSize = 24f
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    private fun startListen() {
        var address = addrText.text.toString()
        var login = loginText.text.toString()
        var password = passwordText.text.toString()
        var dbName = dbNameText.text.toString()

        accelerometerEventListener = AccelerometerHandler(this)

        try {
            database = DatabaseConnection(address, login, password, dbName)


        } catch (e:Exception ) {
            Log.i("db", e.toString())
        }

        database?.start()

        if (accelerometerEventListener != null) {
            sensorManager.unregisterListener(accelerometerEventListener)
        }
        sensorManager.registerListener(accelerometerEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(accelerometerEventListener)
    }

    override fun onStateReceived(state: AccelerometerState) {
        val result = "vals " +
                "k0=${state.x.format(2).replaceFirst(",", ".")}" +
                ",k1=${state.y.format(2).replaceFirst(",", ".")}" +
                ",k2=${state.z.format(2).replaceFirst(",", ".")}" +
                " ${state.time}\n"

        outView.text = result
        database?.sendMsg(result)
    }

    fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
}