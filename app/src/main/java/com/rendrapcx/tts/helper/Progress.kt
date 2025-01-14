package com.rendrapcx.tts.helper

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch

class Progress {

    fun getUserProgress(context: Context, lifecycle: Lifecycle): ArrayList<String> {
        val arr = arrayListOf<String>()
        lifecycle.coroutineScope.launch {
            Data.userAnswerTTS =
                DB.getInstance(context.applicationContext).userAnswerTTS().getAllUserAnswer()
            Data.userAnswerTTS.filter { it.status == Const.AnswerStatus.PROGRESS }.forEach {
                arr.add(it.levelId)
            }
        }
        return arr
    }

    fun getUserSelesai(context: Context, lifecycle: Lifecycle): ArrayList<String> {
        val arr = arrayListOf<String>()
        lifecycle.coroutineScope.launch {
            Data.userAnswerTTS =
                DB.getInstance(context.applicationContext).userAnswerTTS().getAllUserAnswer()
            Data.userAnswerTTS.filter { it.status == Const.AnswerStatus.DONE }.forEach {
                arr.add(it.levelId)
            }
        }
        return arr
    }

    fun updateUserAnswer(status: Const.AnswerStatus, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            DB.getInstance(context.applicationContext).userAnswerTTS().insertUserAnswer(
                Data.UserAnswerTTS(
                    id = Const.currentLevel,
                    userId = "Andra",
                    levelId = Const.currentLevel,
                    status = status
                )
            )
        }
    }
}