package com.rendrapcx.tts.helper

import android.view.View
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

open class Keypad {
    fun showSoftKeyboard(window: Window, view: View) {
        WindowCompat.getInsetsController(window, view).show(
            WindowInsetsCompat.Type.ime()
        )
    }
}