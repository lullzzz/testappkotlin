package com.gshubina.lullzzz.testappkotlin.client

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gshubina.lullzzz.testappkotlin.client.fragment.SpeedometerFragment
import com.gshubina.lullzzz.testappkotlin.client.fragment.TachometerFragment

class ViewPagerFragmentStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val FRAGMENTS_NUM: Int = 2

    override fun getItemCount(): Int = FRAGMENTS_NUM

    override fun createFragment(position: Int): Fragment {
        return if (ViewType.TACHOMETER == ViewType.values()[position]){
            TachometerFragment()
        }else{
            SpeedometerFragment()
        }
    }

    enum class ViewType {
        SPEEDOMETER, TACHOMETER
    }
}