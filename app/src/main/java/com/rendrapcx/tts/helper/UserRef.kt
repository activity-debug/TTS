package com.rendrapcx.tts.helper

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch

class UserRef {
    fun saveAllPref(context: Context, lifecycle: Lifecycle){
        lifecycle.coroutineScope.launch {
            DB.getInstance(context.applicationContext).userPreferences().insertUserPref(
                Data.UserPreferences(
                    id = "0",
                    isLogin = false,
                    showFinished = false,
                    sortOrderByAuthor = false,
                    integratedKeyboard = false,
                    isMusic = true,
                    isSound = true,
                )
            )
        }
    }
}