package com.gshubina.lullzzz.testappkotlin.client.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gshubina.lullzzz.testappkotlin.R
import com.gshubina.lullzzz.testappkotlin.client.viewmodel.SpeedometerDataViewModel

class SpeedometerFragment : FullscreenFragment() {
    private lateinit var mSpeedometerDataViewModel : SpeedometerDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speedometer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO()
    }
}