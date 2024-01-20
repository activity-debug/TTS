package com.rendrapcx.tts.helper

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listUserPreferences
import kotlinx.coroutines.launch

class UserRef {

    fun writeDefaultPreferences(context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            DB.getInstance(context).userPreferences().insertUserPref(
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

    fun getIsSound():Boolean {
        return listUserPreferences[0].isSound
    }

    fun setIsSound(id: String = "0", isSound : Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            listUserPreferences[0].isSound = isSound
            DB.getInstance(context.applicationContext).userPreferences().updateIsSound(
                id = id,
                isSound = isSound
            )
        }
    }

    fun getIntKey():Boolean{
        return listUserPreferences[0].integratedKeyboard
    }

    fun setIntKey(id: String = "0", intKey : Boolean, context: Context, lifecycle: Lifecycle){
        lifecycle.coroutineScope.launch {
            listUserPreferences[0].integratedKeyboard = intKey
            DB.getInstance(context.applicationContext).userPreferences().updateIntegratedKeyboard(
                id = id,
                integratedKeyboard = intKey
            )
        }
    }


    fun loadUserPref(context: Context, lifecycle: Lifecycle){
        lifecycle.coroutineScope.launch {
            val isEmpty =
                DB.getInstance(context)
                    .userPreferences().getAllUserPreferences().isEmpty()

            if (isEmpty) UserRef().writeDefaultPreferences(context, lifecycle)

            listUserPreferences =
                DB.getInstance(context)
                    .userPreferences().getAllUserPreferences()
        }
    }
}