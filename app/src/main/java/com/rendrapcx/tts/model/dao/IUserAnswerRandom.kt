package com.rendrapcx.tts.model.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.model.Data

interface IUserAnswerRandom {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnswer(userAnswerRandom: Data.UserAnswerRandom)

    @Query("DELETE FROM user_answer_random")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_answer_random")
    suspend fun getAllAnswer(): MutableList<Data.UserAnswerRandom>

    @Query("SELECT * FROM user_answer_random WHERE level_id=:levelId;")
    suspend fun getAnswer(levelId : String): MutableList<Data.UserAnswerRandom>

    @Query(value =  "DELETE FROM user_answer_random WHERE level_id = :levelId;")
    suspend fun deleteAnswerById(levelId: String)

}