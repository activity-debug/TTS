package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.model.Data

interface IUserAnswer {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: Data.UserAnswer)

    @Update
    suspend fun updateUserAnswer(userAnswer: Data.UserAnswer)

    @Query("SELECT * FROM user_answer")
    suspend fun getAllUserAnswer(): MutableList<Data.UserAnswer>

    @Query("SELECT * FROM user_answer WHERE level_id=:levelId")
    suspend fun getLevelAnswer(levelId: String) : MutableList<Data.UserAnswer>
}
