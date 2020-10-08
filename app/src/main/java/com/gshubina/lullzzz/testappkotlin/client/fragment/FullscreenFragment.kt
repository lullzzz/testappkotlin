package com.gshubina.lullzzz.testappkotlin.client.fragment

import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment

abstract class FullscreenFragment : Fragment() {
    override fun onResume() {
        super.onResume()
        val decorView = requireActivity().window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        if (activity != null && requireActivity().window != null) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    override fun onPause() {
        super.onPause()
        if (requireActivity() != null && requireActivity().window != null) {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // Clear the systemUiVisibility flag
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }
}