package com.rendrapcx.tts.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rendrapcx.tts.model.Data


interface IUserPreferences{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPref(userPreferences: Data.UserPreferences)

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllUserPreferences(): MutableList<Data.UserPreferences>

    @Query("UPDATE user_preferences " +
            "   SET show_finished = :showFinished " +
            "   WHERE `id` = :id;")
    suspend fun updateShowFinished(id: String, showFinished: Boolean)

    @Query("UPDATE user_preferences " +
            "   SET sort_order_by_author = :sortOrderByAuthor " +
            "   WHERE `id` = :id;")
    suspend fun updateSortOrderByAuthor(id: String, sortOrderByAuthor: Boolean)

    @Query("UPDATE user_preferences " +
            "   SET integrated_keyboard = :integratedKeyboard " +
            "   WHERE `id` = :id;")
    suspend fun updateIntegratedKeyboard(id: String, integratedKeyboard: Boolean)
}