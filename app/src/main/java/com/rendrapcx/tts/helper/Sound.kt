package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore.Audio.Media
import com.rendrapcx.tts.R
import com.rendrapcx.tts.model.Data

class Sound {

    private val isSound = if (Data.userPreferences.isEmpty()) true else Data.userPreferences[0].isSound
    fun soundWinning(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.winner_bell)
        mp.start()
    }

    fun soundClickSetting(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }

    fun soundNextQuestion(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }
    fun soundShuffle(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.bit_video_game_points)
        mp.start()
    }

    fun soundTyping(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.mech_keyboard)
        mp.start()
    }

    fun soundOpeningApp(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.crystal_logo) //.crystal_logo
        mp.start()
    }

    fun soundCheckBoxPass(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.error_call)
        mp.start()
    }

    fun soundOnClickBox(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
        mp.start()
    }

    fun soundOnRandomFill(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.robot_voice_let)
        mp.start()
    }

    fun soundOnRandom(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.computer_beeping)
        mp.start()
    }

    fun soundOnGetRandomValue(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.cyber_punk)
        mp.start()
    }

    fun soundSuccess(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.success)
        mp.start()
    }

    fun soundOnFinger(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.game_bonus)
        mp.start()
    }

    fun soundDingDong(context: Context) {
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.doorbell)
        mp.start()
    }

    fun soundStartGame(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.vozrobotr) //.hybrid_logo //.crystal_logo
        mp.start()
    }

    fun soundFirstLaunch(context: Context){
        if (!isSound) return
        val mp = MediaPlayer.create(context.applicationContext, R.raw.launch_sequence) //.hybrid_logo //.crystal_logo
        mp.start()
    }

}