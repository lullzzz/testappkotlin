package com.gshubina.lullzzz.testappkotlin.client.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gshubina.lullzzz.testappkotlin.R
import com.gshubina.lullzzz.testappkotlin.client.view.GaugeView
import com.gshubina.lullzzz.testappkotlin.client.viewmodel.TachometerDataViewModel
import kotlinx.android.synthetic.main.fragment_tachometer.*

class TachometerFragment : FullscreenFragment() {
    private lateinit var mTachometerDataViewModel : TachometerDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tachometer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTachometerDataViewModel = ViewModelProvider(requireActivity()).get(TachometerDataViewModel::class.java)
        mTachometerDataViewModel.getTachometerData()
            .observe(viewLifecycleOwner, Observer<Double>{ (tachometer_view as GaugeView).setCurrentValue(it)})
    }
}