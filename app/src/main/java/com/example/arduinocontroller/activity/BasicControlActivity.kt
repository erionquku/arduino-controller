package com.example.arduinocontroller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.arduinocontroller.R
import com.example.arduinocontroller.util.BluetoothConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class BasicControlActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope {

    private val TAG = BasicControlActivity::class.qualifiedName
    private var activeButton: Int = R.id.iv_basic_stop

    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var speedText: TextView
    private lateinit var speedBar: SeekBar
    private lateinit var distanceText: TextView

    private final val ARRAY_SIZE = 10
    private var distances = IntArray(ARRAY_SIZE) {-1}
    private var counter = 0
    private val numbersRegex = Regex("[0-9]+")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_control)

        findViewById<ImageView>(R.id.iv_basic_up).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_basic_down).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_basic_left).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_basic_right).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_basic_stop).setOnClickListener(this)

        speedText = findViewById(R.id.tv_basic_speed_desc)
        speedBar = findViewById(R.id.sb_basic_speed)
        distanceText = findViewById(R.id.tv_basic_distance)

        speedBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedText.text = "Speed: ${(progress+1) * 25 }%"
                BluetoothConnection.send(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        launch {
            var response = ""
            while (true) {
                try {
                    val bytesAvailable = BluetoothConnection.bluetoothSocket.inputStream.available()
                    if (bytesAvailable > 0) {
                        val currentBuffer = ByteArray(bytesAvailable)
                        BluetoothConnection.bluetoothSocket.inputStream.read(currentBuffer)

                        response += String(currentBuffer)
                        if (response.contains("\n")) {
                            val input = response.substring(0, response.lastIndexOf("\n"))
                            if (input.trim().matches(numbersRegex) && response.trim() != "" ) {
                                val avg = distances.average()
                                val dis = Integer.parseInt(response.trim())
                                Log.i(TAG, distances.joinToString(", ") + " - " + avg)
                                distances[counter++ % ARRAY_SIZE] = dis
                                uiScope.launch { distanceText.text = "${dis}cm" }
                            }
                            response = response.substring(response.lastIndexOf("\n") + 1)
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.message.toString())
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.basic_control_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bc_terminal -> {
                startActivity(Intent(this, TerminalActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        changeButtonImage(this.activeButton, false)
        changeButtonImage(v.id, true)
        this.activeButton = v.id

        BluetoothConnection.send(v.contentDescription.toString())
    }

    private fun changeButtonImage(buttonId: Int, active: Boolean) {
        when (buttonId) {
            R.id.iv_basic_stop -> {
                if (active)
                    findViewById<ImageView>(buttonId).setImageResource(R.drawable.ic_round_stop_circle_24)
                else
                    findViewById<ImageView>(buttonId).setImageResource(R.drawable.ic_outline_stop_circle_24)
            }

            else -> {
                if (active)
                    findViewById<ImageView>(buttonId).setImageResource(R.drawable.ic_round_arrow_drop_down_circle_24)
                else
                    findViewById<ImageView>(buttonId).setImageResource(R.drawable.ic_outline_arrow_drop_down_circle_24)
            }
        }
    }

}