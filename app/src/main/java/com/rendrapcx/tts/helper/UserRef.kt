package com.rendrapcx.tts.helper

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.launch

class UserRef {

    fun writeDefaultPreferences(context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            DB.getInstance(context).userPreferences().insertUserPref(
                Data.UserPreferences(
                    id = 0,
                    activeFilterTab = FilterStatus.ALL,
                    integratedKeyboard = false,
                    isMusic = true,
                    isSound = true,
                    isEditor = false,
                    koin = 1000
                )
            )
        }
    }

    fun getActiveTabFilter(): FilterStatus {
        return userPreferences[0].activeFilterTab
    }

    fun setActiveTabFilter(activeTab: FilterStatus, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].activeFilterTab = activeTab
            DB.getInstance(context.applicationContext).userPreferences()
                .updateActiveFilterTab(activeTab)
        }
    }


    fun getKoin():Int{
        return userPreferences[0].koin
    }

    fun setKoin(koin: Int, context: Context, lifecycle: Lifecycle){
        lifecycle.coroutineScope.launch {
            userPreferences[0].koin = koin
            DB.getInstance(context.applicationContext).userPreferences().updateKoin(koin)
        }
    }

    fun getIsMusic(): Boolean {
        return userPreferences[0].isMusic
    }

    fun setIsMusic(isMusic: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].isMusic = isMusic
            DB.getInstance(context.applicationContext).userPreferences().updateIsMusic(
                isMusic = isMusic
            )
        }
    }

    fun getIsSound(): Boolean {
        return userPreferences[0].isSound
    }

    fun setIsSound(isSound: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].isSound = isSound
            DB.getInstance(context.applicationContext).userPreferences().updateIsSound(
                isSound = isSound
            )
        }
    }

    fun getIntKey(): Boolean {
        return userPreferences[0].integratedKeyboard
    }

    fun setIntKey(id: String = "0", intKey: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].integratedKeyboard = intKey
            DB.getInstance(context.applicationContext).userPreferences().updateIntegratedKeyboard(
                integratedKeyboard = intKey
            )
        }
    }

    fun getIsEditor(): Boolean {
        return userPreferences[0].isEditor
    }

    fun setIsEditor(isEditor: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].isEditor = isEditor
            DB.getInstance(context.applicationContext).userPreferences().updateIsEditor(
                isEditor = isEditor
            )
        }
    }

    fun checkUserPref(context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            val isEmpty =
                DB.getInstance(context)
                    .userPreferences().getAllUserPreferences().isEmpty()

            if (isEmpty) UserRef().writeDefaultPreferences(context, lifecycle)

            userPreferences =
                DB.getInstance(context.applicationContext).userPreferences().getAllUserPreferences()
        }
    }


}