package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Data

interface IUserAnswerTTS {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: Data.UserAnswerTTS)

    @Update
    suspend fun updateUserAnswer(userAnswer: Data.UserAnswerTTS)

    @Query("SELECT * FROM user_answer_tts")
    suspend fun getAllUserAnswer(): MutableList<Data.UserAnswerTTS>

    @Query("SELECT * FROM user_answer_tts WHERE level_id=:levelId")
    suspend fun getLevelAnswer(levelId: String) : MutableList<Data.UserAnswerTTS>
}
