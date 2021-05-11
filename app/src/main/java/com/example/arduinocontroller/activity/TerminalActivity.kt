package com.example.arduinocontroller.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.arduinocontroller.R
import com.example.arduinocontroller.util.BluetoothConnection
import com.example.arduinocontroller.util.BluetoothConnection.Companion.bluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class TerminalActivity() : AppCompatActivity(), CoroutineScope {

    private val TAG: String = TerminalActivity::javaClass.toString()

    lateinit var commandInput: EditText
    lateinit var linearLayout: LinearLayout
    lateinit var scrollView: ScrollView

    override val coroutineContext: CoroutineContext = Dispatchers.IO
//        get() = Dispatchers.IO
    private val uiScope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        launch {
            var response = ""
            while (true) {
                try {
                    val bytesAvailable = bluetoothSocket.inputStream.available()
                    if (bytesAvailable > 0) {
                        val currentBuffer = ByteArray(bytesAvailable)
                        bluetoothSocket.inputStream.read(currentBuffer)
                        response += String(currentBuffer)
                        if (response.contains("\n")) {
                            addCommand(response.substring(0, response.lastIndexOf("\n")), false)
                            response = response.substring(response.lastIndexOf("\n") + 1)
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.message.toString())
                }
            }

        }

        scrollView = findViewById(R.id.sv_terminal)
        linearLayout = findViewById(R.id.ll_terminal_history)
        commandInput = findViewById(R.id.et_terminal_command)
        this.commandInput.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= commandInput.right - 2 * commandInput.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                    val command = commandInput.text.toString().trim()
                    if (command.isNotEmpty()) {
                        BluetoothConnection.send(command)
                        addCommand(command)
                        commandInput.setText("")
                        return@OnTouchListener true
                    }
                }
            }
            false
        })


    }

    private fun addCommand(command: String, outgoing: Boolean = true) {
        val tv = TextView(this)
        uiScope.launch {
            var dir = "<"
            if (outgoing) {
                tv.setTextColor(Color.GREEN)
                tv.setPadding(10, 15, 10, 10)
                dir = ">"
            } else {
                tv.setPadding(10)
            }

            "$dir $command".also { tv.text = it }
            linearLayout.addView(tv)
            scrollView.scrollToBottom()
        }
    }

}

fun ScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, 2 * delta)
}