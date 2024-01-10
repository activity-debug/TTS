package com.rendrapcx.tts.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface Level {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: Data.Level)

    @Update
    suspend fun updateLevel(level: Data.Level)

    @Delete
    suspend fun deleteLevel(level: Data.Level)

    @Query("select * from level")
    fun getAllLevel(): LiveData<List<Data.Level>>
}

@Dao
interface Question {
    @Insert
    suspend fun insertQuestion(question: Data.Question)

    @Update
    suspend fun updateQuestion(question: Data.Question)

    @Delete
    suspend fun deleteQuestion(question: Data.Question)

    @Query("select * from question")
    fun getAllQuestion(): LiveData<List<Data.Question>>
}

@Dao
interface Partial {
    @Insert
    suspend fun insertPartial(partial: Data.Partial)

    @Update
    suspend fun updatePartial(partial: Data.Partial)

    @Delete
    suspend fun deletePartial(partial: Data.Partial)

    @Query("select * from partial")
    fun getAllPartial(): LiveData<List<Data.Partial>>
}

