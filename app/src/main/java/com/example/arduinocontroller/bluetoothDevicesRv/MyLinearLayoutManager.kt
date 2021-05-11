package com.example.arduinocontroller.bluetoothDevicesRv

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class MyLinearLayoutManager(context: Context) : LinearLayoutManager(context){
    override fun supportsPredictiveItemAnimations() = true
}