package com.gshubina.lullzzz.testappkotlin.client

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class SwipePageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        if (position < -1) {
            view.alpha = 0f
            view.translationX = 0f
            view.translationZ = -1f
        } else if (position >= -1 && position <= -0.5) {
            view.x = -(position + 1) * pageWidth
            val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
            view.translationZ = -1f
            view.alpha = 1 + position
        } else if (position >= -0.5 && position <= 0) {
            view.translationX = position + 0.5f
            val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
            view.translationZ = 0f
            view.alpha = 1f
        } else if (position > 0 && position < 0.5) {
            view.translationX = position - 0.5f
            val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
            view.translationZ = -1f
            view.alpha = 1f
        } else if (position >= 0.5 && position <= 1) {
            view.x = (1 - position) * pageWidth
            val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
            view.translationZ = -1f
            view.alpha = 1 - position
        } else if (position > 1) {
            view.alpha = 0f
            view.translationZ = -1f
        }
    }

    companion object {
        private const val MIN_SCALE = 0.75f
    }
}
