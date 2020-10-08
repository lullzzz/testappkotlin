package com.gshubina.lullzzz.testappkotlin.client.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gshubina.lullzzz.testappkotlin.R
import com.gshubina.lullzzz.testappkotlin.client.view.GaugeView
import com.gshubina.lullzzz.testappkotlin.client.viewmodel.SpeedometerDataViewModel
import kotlinx.android.synthetic.main.fragment_speedometer.*

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
        mSpeedometerDataViewModel = ViewModelProvider(requireActivity()).get(SpeedometerDataViewModel::class.java)
        mSpeedometerDataViewModel.getSpeedometerData()
            .observe(viewLifecycleOwner, Observer<Double>{ value -> (speedometer_view as GaugeView).setCurrentValue(value)})
    }
}