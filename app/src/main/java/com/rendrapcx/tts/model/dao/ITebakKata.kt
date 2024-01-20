package com.rendrapcx.tts.model.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Data

interface ITebakKata {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTbk(tbk: Data.TebakKata)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTbk(tbk: Data.TebakKata)

    @Delete()
    suspend fun deleteTbk(tbk: Data.TebakKata)

    @Query("SELECT * FROM tebak_kata")
    suspend fun getAllTbk(): MutableList<Data.TebakKata>

    @Query("DELETE FROM tebak_kata WHERE id = :id;")
    suspend fun deleteTbkById(id: String)

    @Query("SELECT * FROM tebak_kata WHERE id = :id ORDER BY id ASC;")
    suspend fun getTbkById(id : String): MutableList<Data.TebakKata>


}