package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Dao
import com.rendrapcx.tts.model.Data

interface IHelperCounter {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(helperCounter: Data.HelperCounter)

    @Query("DELETE FROM helper_counter")
    suspend fun deleteAll()

    @Query("DELETE FROM helper_counter WHERE id =:id;")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM helper_counter")
    suspend fun getAll(): MutableList<Data.HelperCounter>

    @Query("SELECT * FROM helper_counter WHERE id=:id;")
    suspend fun getById(id:String): MutableList<Data.HelperCounter>

}