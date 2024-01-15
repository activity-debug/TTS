package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rendrapcx.tts.model.Data

interface IUser {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: Data.User)

    @Query("SELECT * FROM user")
    suspend fun getAllUser(): MutableList<Data.User>

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getCurrentUser(username : String): MutableList<Data.User>
}
