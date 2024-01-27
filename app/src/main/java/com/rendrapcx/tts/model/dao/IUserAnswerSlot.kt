package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.model.Data

interface IUserAnswerSlot {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(userAnswerSlot: Data.UserAnswerSlot)

    @Query("SELECT * FROM user_answer_slot")
    suspend fun getAllAnswerSlot(): MutableList<Data.UserAnswerSlot>
    @Query(value =  "UPDATE user_answer_slot " +
            "   SET answer_slot = :answerSlot " +
            "       WHERE id = :id;")
    suspend fun updateSlot(id: String, answerSlot: MutableMap<Int, String>)


}