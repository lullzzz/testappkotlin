package com.gshubina.lullzzz.testappkotlin.client.fragment

import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment

abstract class FullscreenFragment : Fragment() {
    override fun onResume() {
        super.onResume()
        val decorView = activity!!.window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        if (activity != null && activity!!.window != null) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity != null && activity!!.window != null) {
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // Clear the systemUiVisibility flag
            activity!!.window.decorView.systemUiVisibility = 0
        }
    }
}