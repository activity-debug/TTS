package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data

class Sound {

    var isSound = Data.listUserPreferences[0].isSound
    fun doorOpen(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }

    fun tepukTangan(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crowd_applause)
        mp.start()
    }

    fun logo(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crystal_logo)
        mp.start()
    }

    fun dingDong(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.ding_dong)
        mp.start()
    }
}