package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R

class Sound {
    fun doorOpen(context: Context){
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }

    fun tepukTangan(context: Context){
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crowd_applause)
        mp.start()
    }

    fun logo(context: Context){
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crystal_logo)
        mp.start()
    }

    fun dingDong(context: Context){
        val mp = MediaPlayer.create(context.applicationContext, R.raw.ding_dong)
        mp.start()
    }
}