package com.rendrapcx.tts.model.dao

import android.content.SharedPreferences.Editor
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.FilterStatus
import com.rendrapcx.tts.model.Data


interface IUserPreferences{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPref(userPreferences: Data.UserPreferences)

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllUserPreferences(): MutableList<Data.UserPreferences>

    @Query("UPDATE user_preferences " +
            "   SET active_filter_tab = :activeTab " +
            "   WHERE `id` = 0;")
    suspend fun updateActiveFilterTab(activeTab: FilterStatus)
    @Query("UPDATE user_preferences " +
            "   SET integrated_keyboard = :integratedKeyboard " +
            "   WHERE `id` = 0;")
    suspend fun updateIntegratedKeyboard(integratedKeyboard: Boolean)


    @Query("UPDATE user_preferences " +
            "   SET is_music = :isMusic " +
            "   WHERE `id` = 0;")
    suspend fun updateIsMusic(isMusic: Boolean)

    @Query("UPDATE user_preferences " +
            "   SET is_sound = :isSound " +
            "   WHERE `id` = 0;")
    suspend fun updateIsSound(isSound: Boolean)

    @Query("UPDATE user_preferences " +
            "   SET is_editor = :isEditor " +
            "   WHERE `id` = 0")
    suspend fun updateIsEditor(isEditor: Boolean)

    @Query("UPDATE user_preferences " +
            "   SET koin = :koin " +
            "   WHERE `id` = 0")
    suspend fun updateKoin(koin: Int)

}