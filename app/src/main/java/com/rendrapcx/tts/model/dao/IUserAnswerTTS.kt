package com.rendrapcx.tts.model.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.model.Data

interface IUserAnswerTTS {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: Data.UserAnswerTTS)

    @Update
    suspend fun updateUserAnswer(userAnswer: Data.UserAnswerTTS)

    @Query("DELETE FROM user_answer_tts")
    suspend fun deleteAllUSerAnswer()

    @Query("DELETE FROM user_answer_tts WHERE level_id =:levelId;")
    suspend fun deleteByLevelId(levelId: String)

    @Query("SELECT * FROM user_answer_tts")
    suspend fun getAllUserAnswer(): MutableList<Data.UserAnswerTTS>

    @Query("SELECT * FROM user_answer_tts WHERE status =:status;")
    suspend fun getStatus(status: String) : MutableList<Data.UserAnswerTTS>

    @Query("SELECT * FROM user_answer_tts WHERE level_id=:levelId")
    suspend fun getLevelAnswer(levelId: String) : MutableList<Data.UserAnswerTTS>

    @Query("UPDATE user_answer_tts SET status =:status WHERE id = :id;")
    suspend fun updateStatus(id:String, status : Const.AnswerStatus)

//    -- Auto-generated SQL script #202401280003
//    INSERT INTO "level" (id,category,user_id)
//    VALUES ('zzzzz','zzzz','zzzz');
}
