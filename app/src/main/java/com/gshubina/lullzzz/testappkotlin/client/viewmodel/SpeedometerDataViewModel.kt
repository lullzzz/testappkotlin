package com.gshubina.lullzzz.testappkotlin.client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpeedometerDataViewModel : ViewModel() {
    private val mSpeedometerData : MutableLiveData<Double> = MutableLiveData()

    fun receive(item : Double) {
        mSpeedometerData.value = item
    }

    fun getSpeedometerData() : LiveData<Double> = mSpeedometerData
}