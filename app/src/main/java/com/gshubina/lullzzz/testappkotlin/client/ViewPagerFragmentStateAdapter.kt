package com.gshubina.lullzzz.testappkotlin.client

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerFragmentStateAdapter: FragmentStateAdapter {
    private val FRAGMENTS_NUM = 2

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)

    override fun getItemCount(): Int {
        return FRAGMENTS_NUM
    }

    override fun createFragment(position: Int): Fragment {
        return Fragment()
    }

    enum class ViewType {
        SPEEDOMETER, TACHOMETER
    }
}