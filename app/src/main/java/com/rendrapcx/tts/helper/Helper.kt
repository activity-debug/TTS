package com.rendrapcx.tts.helper

import android.app.Activity
import android.content.Context
import android.icu.text.DecimalFormat
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rendrapcx.tts.model.Data.Companion.listLevel

class Helper {

    fun generateLevelId(size: Int): String {
        var result = ""
        if (size == 0) {
            result = formatLevelId(1)
        }

        val curId = listLevel.map { it.id }
        if (size > 0) {
            for (i in 0 until size + 1) {
                val newId = formatLevelId(i + 1)
                if (!curId.contains(newId)) {
                    result = newId
                    break
                }
            }
        }
        return result
    }

    fun generateQuestionId(currentLevelId: String, number: Int, subDir: String): String {
        return currentLevelId + "-" + subDir + "-" + formatQuestionId(number+1)
    }

    fun formatLevelId(amount: Int): String {
        val numberFormat = DecimalFormat("00000")
        return numberFormat.format(amount)
    }

    fun formatQuestionId(amount: Int): String {
        val numberFormat = DecimalFormat("00")
        return numberFormat.format(amount)
    }

    fun formatTigaDigit(amount: Int): String {
        val numberFormat = DecimalFormat("000")
        return numberFormat.format(amount)
    }

    fun abjadKapital(): List<String> {
        var c: Char = 'A'
        var abjad = arrayListOf<String>()
        while (c <= 'Z') {
            abjad.add(c.toString())
            c++
        }
        return abjad
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun Activity.hideSystemUI() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }
    }

    fun Context.showToast(message: String? = null, short: Boolean = true) {
        Toast.makeText(
            this,
            message,
            when (short) {
                true -> Toast.LENGTH_SHORT
                else -> Toast.LENGTH_SHORT
            }
        ).show()
    }


}