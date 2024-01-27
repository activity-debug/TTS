package com.rendrapcx.tts.helper

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch

class Progress {
    fun updateUserAnswer(status: Const.AnswerStatus, context:Context, lifecycle:LifecycleCoroutineScope) {
        lifecycle.launch() {
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

    fun getUserProgress(context: Context, lifecycle: LifecycleCoroutineScope): ArrayList<String> {
        val arr = arrayListOf<String>()
        lifecycle.launch {
            Data.userAnswerTTS = DB.getInstance(context.applicationContext).userAnswerTTS().getAllUserAnswer()
            Data.userAnswerTTS.filter { it.status == Const.AnswerStatus.DONE }.forEach {
                arr.add(it.levelId)
            }
        }
        return arr
    }
}