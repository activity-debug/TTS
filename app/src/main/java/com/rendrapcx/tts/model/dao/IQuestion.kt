package com.rendrapcx.tts.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rendrapcx.tts.model.Data

interface IQuestion {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Data.Question)

    @Query("select * from question")
    fun getAllQuestion(): LiveData<List<Data.Question>>

    @Query("SELECT * FROM question WHERE level_id = :levelId;")
    suspend fun getQuestion(levelId: String): MutableList<Data.Question>

    @Query("DELETE FROM question WHERE level_id = :levelId;")
    suspend fun deleteQuestionByLevelId(levelId: String)
}