package com.example.arduinocontroller

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.arduinocontroller.activity.BasicControlActivity
import com.example.arduinocontroller.bluetoothDevicesRv.BluetoothDevice
import com.example.arduinocontroller.bluetoothDevicesRv.BtDeviceRvAdapter
import com.example.arduinocontroller.bluetoothDevicesRv.MyLinearLayoutManager
import com.example.arduinocontroller.util.BluetoothConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    lateinit var devices: ArrayList<BluetoothDevice>
    lateinit var myBluetooth: BluetoothAdapter
    lateinit var devicesRv: RecyclerView
    lateinit var btRvAdapter: BtDeviceRvAdapter
    lateinit var progressBar: ProgressBar
    lateinit var emptyText: TextView

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.pb_main)
        emptyText = findViewById(R.id.tv_main_no_devices)
        devicesRv = findViewById<RecyclerView>(R.id.rv_main_device_list)
        devices = BluetoothDevice.createBtDevices(3);
        btRvAdapter = BtDeviceRvAdapter(devices) {
            bluetoothDevice -> deviceSelected(bluetoothDevice)
        }

        btRvAdapter.setHasStableIds(true)

        devicesRv.adapter = btRvAdapter
        devicesRv.layoutManager = MyLinearLayoutManager(this)

        myBluetooth = BluetoothAdapter.getDefaultAdapter()
        if (!myBluetooth.isEnabled) {
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTon, 1)
            Toast.makeText(
                this,
                "Please click refresh to see your paired devices, after bluetooth has turned on.",
                Toast.LENGTH_LONG
            ).show()
        } else if (myBluetooth.isEnabled) {
            refreshDevicesList()
        }

    }


    private fun refreshDevicesList() {
        val pairedDevices = myBluetooth.bondedDevices

        devices.clear()
        if (pairedDevices.size > 0)
            for (bt in pairedDevices)
                devices.add(BluetoothDevice(bt.name, bt.address))
        else Toast.makeText(
            applicationContext,
            "No Paired Bluetooth Devices Found. Please pair with your device and then click Refresh",
            Toast.LENGTH_LONG
        ).show()

        btRvAdapter.notifyDataSetChanged()

        if (devices.size > 0 && emptyText.visibility == VISIBLE) {
            emptyText.visibility = INVISIBLE
        } else if (devices.size == 0 && emptyText.visibility == INVISIBLE) {
            emptyText.visibility = VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ma_refresh -> {
                refreshDevicesList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deviceSelected(device: BluetoothDevice) {
        progressBar.visibility = VISIBLE
        devicesRv.visibility = INVISIBLE

        launch {
            val connected = BluetoothConnection.connectTo(device.mac)

            uiScope.launch {
                progressBar.visibility = INVISIBLE
                devicesRv.visibility = VISIBLE
            }

            if (connected) {
                startActivity(Intent(this@MainActivity, BasicControlActivity::class.java))
            } else {
                uiScope.launch {
                    Toast
                        .makeText(this@MainActivity, "Couldn't connect to ${device.name}!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

    }

}