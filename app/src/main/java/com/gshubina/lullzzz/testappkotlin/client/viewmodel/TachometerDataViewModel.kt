package com.gshubina.lullzzz.testappkotlin.client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TachometerDataViewModel : ViewModel() {
    private val mTachometerData : MutableLiveData<Double> = MutableLiveData()

    fun receive(item : Double) {
        mTachometerData.value = item
    }

    fun getTachometerData() : LiveData<Double> = mTachometerData
}