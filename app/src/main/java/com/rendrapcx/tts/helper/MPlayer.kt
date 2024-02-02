package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.Companion.currentTrack
import com.rendrapcx.tts.constant.Const.Companion.indexPlay
import com.rendrapcx.tts.constant.Const.Companion.isPlay
import com.rendrapcx.tts.constant.Const.Companion.playTitle
import com.rendrapcx.tts.model.Data
import java.util.Timer
import java.util.TimerTask


enum class Sora {
    START_APP,
    INIT_GAME_1, INIT_GAME_2, WINNING,
    SETTING,
    BOXES, ARROW, SHUFFLE, TYPING,
    SUCCESS, BONUS,
    HINT, HINT_RANDOM, NINJA,
    ROBOT, ROBOT_RANDOM
}

class MPlayer {

    private val isSound =
        if (Data.userPreferences.isEmpty()) true else Data.userPreferences[0].isSound
    private val isMusic =
        if (Data.userPreferences.isEmpty()) true else Data.userPreferences[0].isMusic

    private var mp = MediaPlayer()


    val listLaguOffline = mutableMapOf<Int, String>(
        R.raw.andra_komang to "Komang",
        R.raw.andra_manja to "Manja",
        R.raw.andra_kusadari to "Kusadari",
        R.raw.andra_spirit_carries_on to "Spirit carries on",
        R.raw.andra_putri_condor_heroes to "Condor Heroes",
        R.raw.andra_sejak_mengenal_dirimu to "Sejak mengenal dirimu",
        R.raw.andra_putri_sesa_cinta to "Sesa cinta",
        R.raw.andra_cici_yang_terbaik to "Yang terbaik",
    )

    companion object {
        var player = MediaPlayer()
    }

    private var list = arrayListOf<Int>()

    fun playNext(context: Context) {
        val dur = player.duration
        var elapsed: Long = 0
        val INTERVAL: Long = 1000
        val TIMEOUT: Long = dur.toLong()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                elapsed += INTERVAL
                if (elapsed >= TIMEOUT) {
                    this.cancel()
                    if (isPlay) {
                        player.reset()
                        MPlayer().source(context.applicationContext)
                        player.start()
                        playNext(context.applicationContext)
                    }
                    return
                }
                if (!isPlay) {
                    this.cancel()
                    elapsed = TIMEOUT
                    return
                }
            }
        }
        val timer = Timer()
        timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL)
    }

//    fun playNext(context: Context) {
//        val dur = player.duration.toLong()
//        object : CountDownTimer(dur + 100, 1000) {
//            override fun onTick(millisUntilFinished: Long) {}
//            override fun onFinish(
//            ) {
//                player.reset()
//                MPlayer().source(context.applicationContext)
//                player.start()
//                if (isPlay) playNext(context.applicationContext)
//            }
//        }.start()
//    }

    fun source(context: Context) {
        list.clear()
        listLaguOffline.map { it.key }.forEach() {
            list.add(it)
        }
        indexPlay++
        if (indexPlay >= list.size) {
            indexPlay = 0
        }
        currentTrack = list[indexPlay]
        player = MediaPlayer.create(context.applicationContext, currentTrack)
        playTitle = MPlayer().listLaguOffline.getValue(currentTrack)
    }

    fun sound(context: Context, sora: Sora) {

        if (!isSound) return
        when (sora) {
            Sora.START_APP -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.crystal_logo)
            }

            Sora.INIT_GAME_1 -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.launch_sequence)
            }

            Sora.INIT_GAME_2 -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.vozrobotr)
            }

            Sora.WINNING -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.winner_bell)
            }

            Sora.SETTING -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
            }

            Sora.ARROW -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
            }

            Sora.SHUFFLE -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.bit_video_game_points)
            }

            Sora.TYPING -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.mech_keyboard)
            }

            Sora.HINT -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.doorbell)
            }

            Sora.HINT_RANDOM -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.cyber_punk)
            }

            Sora.NINJA -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.error_call)
            }

            Sora.BOXES -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.door_opening)
            }

            Sora.ROBOT -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.robot_voice_let)
            }

            Sora.ROBOT_RANDOM -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.computer_beeping)
            }

            Sora.SUCCESS -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.success)
            }

            Sora.BONUS -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.game_bonus)
            }
        }
        mp.start()
        mp.setOnCompletionListener { mp.release() }
    }


}