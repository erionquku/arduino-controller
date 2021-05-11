package com.example.arduinocontroller.bluetoothDevicesRv

data class BluetoothDevice (
    var name : String,
    var mac: String
) {
    companion object {
        fun createBtDevices(numDevices: Int): ArrayList<BluetoothDevice> {
            val contacts = ArrayList<BluetoothDevice>()
            for (i in 1..numDevices) {
                contacts += BluetoothDevice("Device $i", "macAddress$i$i$i")
            }
            return contacts
        }
    }
}