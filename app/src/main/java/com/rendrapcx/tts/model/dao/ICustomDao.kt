package com.rendrapcx.tts.model.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Data

interface ICustomDao {
    @Query("select * from level")
    suspend fun getAllLevel(): MutableList<Data.Level>

}