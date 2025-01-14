package com.rendrapcx.tts.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Data


interface ILevel {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: Data.Level)

    @Update
    suspend fun updateLevel(level: Data.Level)

    @Delete()
    suspend fun deleteLevel(level: Data.Level)

    @Query("select * from level")
    suspend fun getAllLevel(): MutableList<Data.Level>

    @Query("SELECT * FROM level WHERE category = :category ORDER BY category ASC;")
    suspend fun getAllByCategory(category: String): MutableList<Data.Level>

    @Query("SELECT * FROM level WHERE `id` = :id;")
    suspend fun getLevel(id: String): MutableList<Data.Level>

    @Query("DELETE FROM level WHERE `id` = :id;")
    suspend fun deleteLevelById(id: String)

    @Query(value =  "UPDATE level " +
            "   SET status =:status " +
            "       WHERE id = :id;")
    suspend fun updateStatus(id: String, status: String)

    @Query(value =  "UPDATE level " +
            "   SET id =:newId " +
            "       WHERE id = :id;")
    suspend fun updateIdManual(id: String, newId:String)
}