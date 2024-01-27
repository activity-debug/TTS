package com.rendrapcx.tts.helper

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.launch

class UserRef {

    fun writeUserStateTTS() {
        //currentlevel
        //
    }

    fun writeUserStateTBK() {
        //currentSoal
        //
    }

    fun writeDefaultPreferences(context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            DB.getInstance(context).userPreferences().insertUserPref(
                Data.UserPreferences(
                    id = "0",
                    currentUser = 0,
                    isLogin = false,
                    activeFilterTab = FilterStatus.ALL,
                    sortOrderByAuthor = false,
                    integratedKeyboard = false,
                    isMusic = true,
                    isSound = true,
                    isEditor = false
                )
            )
        }
    }

    fun getIsEditor():Boolean {
        return userPreferences[0].isEditor
    }

    fun setIsEditor(id: String = "0", isEditor: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].isEditor = isEditor
            DB.getInstance(context.applicationContext).userPreferences().updateIsEditor("0", isEditor)
        }
    }

    fun getCurrentUser():Int {
        return userPreferences[0].currentUser
    }

    fun setCurrentUser(id: String = "0", currentUser: Int, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].currentUser = currentUser
            DB.getInstance(context.applicationContext).userPreferences().updateCurrentUser("0", currentUser)
        }
    }

    fun getActiveTabFilter(): FilterStatus {
        return userPreferences[0].activeFilterTab
    }

    fun setActiveTabFilter(id: String = "0", activeTab: FilterStatus, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].activeFilterTab = activeTab
            DB.getInstance(context.applicationContext).userPreferences().updateActiveFilterTab(
                id, activeTab
            )
        }
    }

    fun getIsSound(): Boolean {
        return userPreferences[0].isSound
    }

    fun setIsSound(id: String = "0", isSound: Boolean, context: Context, lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            userPreferences[0].isSound = isSound
            DB.getInstance(context.applicationContext).userPreferences().updateIsSound(
                id = id,
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
                id = id,
                integratedKeyboard = intKey
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