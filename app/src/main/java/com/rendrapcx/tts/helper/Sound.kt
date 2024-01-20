package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R

class Sound {
    fun doorOpen(context: Context){
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }
}