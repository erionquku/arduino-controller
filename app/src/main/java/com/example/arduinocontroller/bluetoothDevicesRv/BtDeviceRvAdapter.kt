package com.example.arduinocontroller.bluetoothDevicesRv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arduinocontroller.R

class BtDeviceRvAdapter(
    private val mDevices: ArrayList<BluetoothDevice>,
    private val onClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BtDeviceRvAdapter.ViewHolder>() {

    inner class ViewHolder(listItemView: View, private val onClick: (BluetoothDevice) -> Unit) :
        RecyclerView.ViewHolder(listItemView) {
        val btDeviceName = listItemView.findViewById<TextView>(R.id.btDeviceName)
        val btDeviceMac = listItemView.findViewById<TextView>(R.id.btDeviceMac)

        init {
            listItemView.setOnClickListener() {
                mDevices[adapterPosition].let(onClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context;
        val inflater = LayoutInflater.from(context)

        val btDeviceView = inflater.inflate(R.layout.bluetooth_device_row, parent, false)
        return ViewHolder(btDeviceView) {
            onClick(it)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = mDevices[position]
        holder.btDeviceName.text = device.name
        holder.btDeviceMac.text = device.mac
    }

    override fun getItemCount() = mDevices.size

    override fun getItemId(position: Int) = mDevices[position].hashCode().toLong();

}