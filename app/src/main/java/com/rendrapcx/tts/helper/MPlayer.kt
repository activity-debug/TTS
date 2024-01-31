package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.model.Data


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

    //private val isMusic =
    //    if (Data.userPreferences.isEmpty()) true else Data.userPreferences[0].isMusic

    //    fun music(context: Context) {
    //
    //    }

    fun sound(context: Context, sora: Sora) {
        var mp = MediaPlayer()

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