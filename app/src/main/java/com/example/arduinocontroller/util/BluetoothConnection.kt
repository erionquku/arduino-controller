package com.example.arduinocontroller.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.util.*

class BluetoothConnection {

    companion object {
        private val TAG = BluetoothConnection::class.qualifiedName

        lateinit var bluetoothSocket: BluetoothSocket
        private lateinit var bluetoothAdapter: BluetoothAdapter

        fun connectTo(MAC: String): Boolean {
            return try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val btDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC)
                bluetoothSocket = btDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                bluetoothAdapter.cancelDiscovery()
                bluetoothSocket.connect()
                true
            } catch (e: Exception) {
                false
            }
        }

        fun send(message: String) {
            if (this::bluetoothAdapter.isInitialized)
                bluetoothSocket.outputStream.write(message.toByteArray())
            else
                Log.e(TAG, "Couldn't send $message")
        }

    }
}