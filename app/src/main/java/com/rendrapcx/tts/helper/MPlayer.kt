package com.rendrapcx.tts.helper

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.Companion.currentTrackOffline
import com.rendrapcx.tts.constant.Const.Companion.currentTrackOnline
import com.rendrapcx.tts.constant.Const.Companion.indexPlayOffline
import com.rendrapcx.tts.constant.Const.Companion.indexPlayOnline
import com.rendrapcx.tts.constant.Const.Companion.isPlay
import com.rendrapcx.tts.constant.Const.Companion.playTitleOffline
import com.rendrapcx.tts.constant.Const.Companion.playTitleOnline
import com.rendrapcx.tts.constant.Const.Companion.player
import com.rendrapcx.tts.model.Data
import java.util.Timer
import java.util.TimerTask


enum class Sora {
    START_APP,
    INIT_GAME_1, INIT_GAME_2, WINNING,
    SETTING, DING,
    BOXES, ARROW, SHUFFLE, TYPING,
    SUCCESS, BONUS,
    HINT, HINT_RANDOM, NINJA,
    ROBOT, ROBOT_RANDOM,
    HAMMER, TOOLBOX,
    COIN_1, COIN_2, COIN_3
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

    val listLaguOnline = mutableMapOf<Int, String>(
        0 to "https://rendrapcx.github.io/assets/mp3/andra-aku-lelakimu.mp3",
        1 to "https://rendrapcx.github.io/assets/mp3/andra-cici-aku-mau.mp3",
        2 to "https://rendrapcx.github.io/assets/mp3/andra-cici-yang-terbaik.mp3",
        3 to "https://rendrapcx.github.io/assets/mp3/andra-cinta-ini-membunuhku.mp3",
        4 to "https://rendrapcx.github.io/assets/mp3/andra-cinta-luar-biasa.mp3",
        5 to "https://rendrapcx.github.io/assets/mp3/andra-dealova.mp3",
        6 to "https://rendrapcx.github.io/assets/mp3/andra-dia-milikku.mp3",
        7 to "https://rendrapcx.github.io/assets/mp3/andra-gelora-remix.mp3",
        8 to "https://rendrapcx.github.io/assets/mp3/andra-high-hopes.mp3",
        9 to "https://rendrapcx.github.io/assets/mp3/andra-just-for-my-mom.mp3",
    )

    fun playNextOnline(context: Context) {
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
                        sourceOnline()
                        player.prepare()
                        player.start()
                        playNextOnline(context.applicationContext)
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
                        sourceOffline(context.applicationContext)
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

    fun sourceOffline(context: Context) {
        val list = arrayListOf<Int>()
        listLaguOffline.map { it.key }.forEach() {
            list.add(it)
        }
        ++indexPlayOffline
        if (indexPlayOffline >= list.size) {
            indexPlayOffline = 0
        }
        currentTrackOffline = list[indexPlayOffline]
        playTitleOffline = MPlayer().listLaguOffline.getValue(currentTrackOffline)
        player = MediaPlayer.create(context.applicationContext, currentTrackOffline)
    }

    fun sourceOnline() {
        val list = arrayListOf<Int>()
        listLaguOnline.map { it.key }.forEach() {
            list.add(it)
        }
        ++indexPlayOnline
        if (indexPlayOnline >= list.size) {
            indexPlayOnline = 0
        }
        currentTrackOnline = list[indexPlayOnline]
        val s = MPlayer().listLaguOnline.getValue(currentTrackOnline)
        playTitleOnline = s.substring(39, s.length)
        player.setDataSource(listLaguOnline.getValue(currentTrackOnline))
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
            Sora.TOOLBOX -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.toolbox_select)
            }
            Sora.HAMMER -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.hammer_hit)
            }
            Sora.COIN_1 -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.coin_drop_1)
            }
            Sora.COIN_2 -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.coin_drop_2)
            }
            Sora.COIN_3 -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.coin_drop_3)
            }
            Sora.DING -> {
                mp = MediaPlayer.create(context.applicationContext, R.raw.new_ding)
            }
        }
        mp.start()
        mp.setOnCompletionListener { mp.release() }
    }


}