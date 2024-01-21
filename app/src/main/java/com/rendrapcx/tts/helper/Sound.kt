package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.model.Data

class Sound {

    private val isSound = if (Data.listUserPreferences.isEmpty()) true else Data.listUserPreferences[0].isSound

    fun soundWinning(context: Context) {
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crowd_applause)
        mp.start()
    }

    fun soundClickSetting(context: Context) {
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }

    fun soundOpeningApp(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crystal_logo)
        mp.start()
    }

    fun soundCheckBoxPass(context: Context) {
        val mp = MediaPlayer.create(context.applicationContext, R.raw.ding_dong)
        mp.start()
    }
}